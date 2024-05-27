package am.ik.lognroll.logs;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.logs.v1.LogsData;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.logs.v1.ScopeLogs;
import io.opentelemetry.proto.resource.v1.Resource;

public class Logs {

	private static final String SERVICE_NAME_ATTR = "service.name";

	public static List<Log> from(LogsData logs) {
		List<Log> result = new ArrayList<>();
		for (int i = 0; i < logs.getResourceLogsCount(); i++) {
			ResourceLogs resourceLogs = logs.getResourceLogs(i);
			Map<String, Object> resourceAttributes = new HashMap<>();
			String serviceName = "";
			Resource resource = resourceLogs.getResource();
			if (resource.getAttributesCount() > 0) {
				for (KeyValue attribute : resource.getAttributesList()) {
					if (SERVICE_NAME_ATTR.equals(attribute.getKey())) {
						serviceName = anyToObject(attribute.getValue()).toString();
					}
					else {
						resourceAttributes.put(attribute.getKey(), anyToObject(attribute.getValue()));
					}
				}
			}
			for (int j = 0; j < resourceLogs.getScopeLogsCount(); j++) {
				ScopeLogs scopeLogs = resourceLogs.getScopeLogs(j);
				Map<String, Object> scopeAttributes = new HashMap<>();
				InstrumentationScope scope = scopeLogs.getScope();
				if (scope.getAttributesCount() > 0) {
					scope.getAttributesList()
						.forEach(attribute -> scopeAttributes.put(attribute.getKey(),
								anyToObject(attribute.getValue())));
				}
				for (int k = 0; k < scopeLogs.getLogRecordsCount(); k++) {
					LogBuilder logBuilder = LogBuilder.log()
						.scope(scope.getName())
						.serviceName(serviceName)
						.resourceAttributes(resourceAttributes);
					Map<String, Object> attributes = new HashMap<>(scopeAttributes);
					LogRecord logRecord = scopeLogs.getLogRecords(k);
					logBuilder.timestamp(Instant.EPOCH.plusNanos(logRecord.getTimeUnixNano()));
					logBuilder.observedTimestamp(Instant.EPOCH.plusNanos(logRecord.getObservedTimeUnixNano()));
					logBuilder.severity(logRecord.getSeverityText());
					logBuilder.body(anyToObject(logRecord.getBody()).toString() /* TODO */);
					logBuilder.traceId(HexFormat.of().formatHex(logRecord.getTraceId().toByteArray()));
					logBuilder.spanId(HexFormat.of().formatHex(logRecord.getSpanId().toByteArray()));
					if (logRecord.getAttributesCount() > 0) {
						logRecord.getAttributesList()
							.forEach(
									attribute -> attributes.put(attribute.getKey(), anyToObject(attribute.getValue())));
						logBuilder.attributes(attributes);
					}
					else {
						logBuilder.attributes(Map.of());
					}
					Log log = logBuilder.build();
					result.add(log);
				}
			}
		}
		return result;
	}

	static Object anyToObject(AnyValue value) {
		if (value.hasStringValue()) {
			return value.getStringValue();
		}
		if (value.hasBoolValue()) {
			return value.getBoolValue();
		}
		if (value.hasBytesValue()) {
			return value.getBytesValue().toByteArray();
		}
		if (value.hasIntValue()) {
			return value.getIntValue();
		}
		if (value.hasDoubleValue()) {
			return value.getDoubleValue();
		}
		if (value.hasArrayValue()) {
			return value.getArrayValue().getValuesList().stream().map(Logs::anyToObject).toList();
		}
		if (value.hasKvlistValue()) {
			return Map.ofEntries(value.getKvlistValue()
				.getValuesList()
				.stream()
				.map(kv -> Map.entry(kv.getKey(), anyToObject(kv.getValue())))
				.toArray(Map.Entry[]::new));
		}
		return "";
	}

}
