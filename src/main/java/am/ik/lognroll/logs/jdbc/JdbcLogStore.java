package am.ik.lognroll.logs.jdbc;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import am.ik.lognroll.logs.Log;
import am.ik.lognroll.logs.LogStore;
import am.ik.lognroll.util.Json;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Component
@Transactional
public class JdbcLogStore implements LogStore {

	private final JdbcTemplate jdbcTemplate;

	private final ObjectMapper objectMapper;

	public JdbcLogStore(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public void addAll(List<Log> logs) {
		if (CollectionUtils.isEmpty(logs)) {
			return;
		}
		Map<Long, List<Log>> logsMap = logs.stream().collect(Collectors.groupingBy(Log::resourceAttributesDigest));
		logsMap.forEach(this::addAllByDigest);
	}

	private void addAllByDigest(long digest, List<Log> logs) {
		Log firstLog = logs.getFirst();
		int count = Objects.requireNonNull(this.jdbcTemplate
			.queryForObject("SELECT COUNT(digest) FROM resource_attributes WHERE digest = ?", Integer.class, digest));
		if (count == 0) {
			this.jdbcTemplate.update("INSERT INTO resource_attributes(digest, resource_attributes) VALUES (?, ?)",
					digest, Json.stringify(this.objectMapper, firstLog.resourceAttributes()));
		}
		this.jdbcTemplate.batchUpdate("""
				INSERT INTO log(
				    timestamp,
				    observed_timestamp,
				    severity,
				    service_name,
				    scope,
				    body,
				    trace_id,
				    span_id,
				    attributes,
				    resource_attributes_digest
				)
				VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""".trim(),
				logs.stream()
					.map(log -> new Object[] { Timestamp.from(log.timestamp()), Timestamp.from(log.observedTimestamp()),
							log.severity(), log.serviceName(), log.scope(), log.body(), log.traceId(), log.spanId(),
							Json.stringify(this.objectMapper, log.attributes()), digest })
					.toList());
	}

	@Override
	public void clear() {
		this.jdbcTemplate.update("DELETE FROM resource_attributes");
		this.jdbcTemplate.update("DELETE FROM log");
	}

}
