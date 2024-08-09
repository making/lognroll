package am.ik.lognroll.syslog;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import am.ik.lognroll.logs.Log;
import am.ik.lognroll.logs.LogBuilder;
import jakarta.annotation.Nullable;

import org.springframework.integration.syslog.RFC5424SyslogParser;
import org.springframework.integration.syslog.SyslogHeaders;

public class SyslogPayload {

	private static final RFC5424SyslogParser parser = new RFC5424SyslogParser();

	final Map<String, ?> payload;

	static final Pattern keyValuePattern = Pattern.compile("(\\w+)=\"([^\"]*)\"");

	static final Pattern traceparentPattern = Pattern
		.compile("traceparent:\"([0-9]+)-([0-9-a-z]+)-([0-9-a-z]+)-([0-9]+)\"");

	static final Pattern traceIdPattern = Pattern.compile("\"(traceId|trace_id)\":\"([0-9-a-z]+)\"",
			Pattern.CASE_INSENSITIVE);

	static final Pattern spanIdPattern = Pattern.compile("\"(spanId|span_id)\":\"([0-9-a-z]+)\"",
			Pattern.CASE_INSENSITIVE);

	public SyslogPayload(String line) {
		this.payload = parser.parse(line.endsWith("]") ? line + " " : line, 0, false);
	}

	@Nullable
	public final Integer facility() {
		return (Integer) this.payload.get(SyslogHeaders.FACILITY);
	}

	@Nullable
	public final Integer severity() {
		return (Integer) this.payload.get(SyslogHeaders.SEVERITY);
	}

	@Nullable
	public final String severityText() {
		return (String) this.payload.get(SyslogHeaders.SEVERITY_TEXT);
	}

	public final String timestamp() {
		return Objects.toString(this.payload.get(SyslogHeaders.TIMESTAMP), "");
	}

	@Nullable
	public final String host() {
		return (String) this.payload.get(SyslogHeaders.HOST);
	}

	@Nullable
	public final String tag() {
		return (String) this.payload.get(SyslogHeaders.TAG);
	}

	public final String message() {
		return Objects.toString(this.payload.get(SyslogHeaders.MESSAGE), "");
	}

	@Nullable
	public final String appName() {
		return (String) this.payload.get(SyslogHeaders.APP_NAME);
	}

	@Nullable
	public final String procId() {
		return (String) this.payload.get(SyslogHeaders.PROCID);
	}

	@Nullable
	public final String msgId() {
		return (String) this.payload.get(SyslogHeaders.MSGID);
	}

	@SuppressWarnings("unchecked")
	public final Map<String, Object> structuredData() {
		List<String> data = (List<String>) this.payload.get(SyslogHeaders.STRUCTURED_DATA);
		if (data == null) {
			return new LinkedHashMap<>();
		}
		String first = data.getFirst();
		return parseLog(first);
	}

	public static Map<String, Object> parseLog(String log) {
		Map<String, Object> map = new LinkedHashMap<>();
		Matcher matcher = keyValuePattern.matcher(log);
		while (matcher.find()) {
			map.put(matcher.group(1), matcher.group(2));
		}
		return map;
	}

	@Nullable
	public final Integer version() {
		return (Integer) this.payload.get(SyslogHeaders.VERSION);
	}

	public final Optional<String> errors() {
		String errors = (String) this.payload.get(SyslogHeaders.ERRORS);
		return Optional.ofNullable(errors);
	}

	@Nullable
	public final String undecoded() {
		return (String) this.payload.get(SyslogHeaders.UNDECODED);

	}

	public Log toLog() {
		Instant timestamp = Instant.parse(this.timestamp());
		String message = this.message();
		Map<String, Object> resourceAttributes = this.structuredData();
		resourceAttributes.put("hostname", this.host());
		LogBuilder body = LogBuilder.log()
			.observedTimestamp(timestamp)
			.timestamp(timestamp)
			.resourceAttributes(resourceAttributes)
			.body(message);
		if (this.appName() != null) {
			body.serviceName(this.appName());
		}
		if (resourceAttributes.containsKey("app_name")) {
			body.serviceName(resourceAttributes.get("app_name").toString());
		}
		if (this.severityText() != null) {
			body.severityText(this.severityText());

		}
		if (this.severity() != null) {
			body.severityNumber(this.severity());
		}
		Matcher traceparentMatcher = traceparentPattern.matcher(message);
		if (traceparentMatcher.find()) {
			body.traceId(traceparentMatcher.group(2));
			body.spanId(traceparentMatcher.group(3));
			body.traceFlags(Integer.parseInt(traceparentMatcher.group(4)));
		}
		Matcher traceIdMatcher = traceIdPattern.matcher(message);
		if (traceIdMatcher.find()) {
			body.traceId(traceIdMatcher.group(2));
			body.traceFlags(1);
		}
		Matcher spanIdMatcher = spanIdPattern.matcher(message);
		if (spanIdMatcher.find()) {
			body.spanId(spanIdMatcher.group(2));
		}
		return body.build();
	}

	@Override
	public String toString() {
		Map<String, Object> copy = new LinkedHashMap<>();
		if (this.payload != null) {
			copy.putAll(this.payload);
		}
		copy.remove(SyslogHeaders.UNDECODED);
		return String.valueOf(copy);
	}

}