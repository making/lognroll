package am.ik.lognroll.jdbc;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import am.ik.lognroll.logs.Log;
import am.ik.lognroll.logs.LogBuilder;
import am.ik.lognroll.logs.LogQuery;
import am.ik.lognroll.util.Json;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JdbcLogQuery implements LogQuery {

	private final JdbcClient jdbcClient;

	private final ObjectMapper objectMapper;

	public JdbcLogQuery(JdbcClient jdbcClient, ObjectMapper objectMapper) {
		this.jdbcClient = jdbcClient;
		this.objectMapper = objectMapper;
	}

	@Override
	public List<Log> findLatestLogs(SearchRequest request) {
		StringBuilder sql = new StringBuilder("""
				SELECT log.log_id,
				       log.timestamp,
				       log.severity,
				       log.service_name,
				       log.scope,
				       log.body,
				       log.trace_id,
				       log.span_id,
				       log.attributes,
				       resource_attributes.attributes AS resource_attributes
				""");
		Map<String, Object> params = new HashMap<>();
		String query = request.query();
		if (StringUtils.hasText(query)) {
			sql.append("""
					FROM log_fts
					JOIN log ON log_fts.rowid = log.log_id
					JOIN resource_attributes ON log.resource_attributes_digest = resource_attributes.digest
					""");
		}
		else {
			sql.append("""
					FROM log
					JOIN resource_attributes ON log.resource_attributes_digest = resource_attributes.digest
					""");
		}
		sql.append("""
				WHERE 1 = 1
				""");
		Cursor cursor = request.pageRequest().cursor();
		if (cursor != null) {
			sql.append("""
					AND timestamp <= :timestamp
					AND log_id < :log_id
					""");
			params.put("timestamp", Timestamp.from(cursor.timestamp()));
			params.put("log_id", cursor.logId());
		}
		if (StringUtils.hasText(query)) {
			sql.append("""
					AND log_fts MATCH(:query)
					""");
			params.put("query", "\"" + query + "\"");
		}
		sql.append("""
				ORDER BY timestamp DESC, log_id DESC
				""");
		if (request.pageRequest().pageSize() > 0) {
			sql.append("LIMIT %d".formatted(request.pageRequest().pageSize()));
		}
		System.out.println(sql);
		return this.jdbcClient.sql(sql.toString()) //
			.params(params) //
			.query((rs, rowNum) -> LogBuilder.log()
				.logId(rs.getLong("log_id"))
				.timestamp(rs.getTimestamp("timestamp").toInstant())
				.severity(rs.getString("severity"))
				.serviceName(rs.getString("service_name"))
				.scope(rs.getString("scope"))
				.body(rs.getString("body"))
				.traceId(rs.getString("trace_id"))
				.spanId(rs.getString("span_id"))
				.attributes(Json.parse(this.objectMapper, rs.getString("attributes")))
				.resourceAttributes(Json.parse(this.objectMapper, rs.getString("resource_attributes")))
				.build()) //
			.list();
	}

}
