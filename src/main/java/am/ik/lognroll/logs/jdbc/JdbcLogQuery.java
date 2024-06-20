package am.ik.lognroll.logs.jdbc;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import am.ik.lognroll.logs.Log;
import am.ik.lognroll.logs.LogBuilder;
import am.ik.lognroll.logs.LogQuery;
import am.ik.lognroll.logs.filter.FilterExpressionConverter;
import am.ik.lognroll.logs.filter.converter.Sqlite3FilterExpressionConverter;
import am.ik.lognroll.util.Json;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JdbcLogQuery implements LogQuery {

	private final JdbcClient jdbcClient;

	private final ObjectMapper objectMapper;

	private final FilterExpressionConverter converter = new Sqlite3FilterExpressionConverter();

	public JdbcLogQuery(JdbcClient jdbcClient, ObjectMapper objectMapper) {
		this.jdbcClient = jdbcClient;
		this.objectMapper = objectMapper;
	}

	@Override
	public List<Log> findLatestLogs(SearchRequest request) {
		StringBuilder sql = new StringBuilder("""
				SELECT log.log_id,
				       log.timestamp,
				       log.observed_timestamp,
				       log.severity_text,
				       log.severity_number,
				       log.service_name,
				       log.scope,
				       log.body,
				       log.trace_id,
				       log.span_id,
				       log.trace_flags,
				       log.attributes,
				       resource_attributes.resource_attributes
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
					AND observed_timestamp < :observed_timestamp
					""");
			params.put("timestamp", Timestamp.from(cursor.timestamp()));
			params.put("observed_timestamp", Timestamp.from(cursor.observedTimestamp()));
		}
		if (request.from() != null) {
			sql.append("""
					AND timestamp >= :from
					""");
			params.put("from", Timestamp.from(request.from()));
		}
		if (request.to() != null) {
			sql.append("""
					AND timestamp <= :to
					""");
			params.put("to", Timestamp.from(request.to()));
		}
		if (StringUtils.hasText(query)) {
			sql.append("""
					AND log_fts MATCH(:query)
					""");
			params.put("query", "\"" + query + "\"");
		}
		if (request.filterExpression() != null) {
			sql.append("AND ")
				.append(this.converter.convertExpression(request.filterExpression()))
				.append(System.lineSeparator());
		}
		sql.append("""
				ORDER BY observed_timestamp DESC, timestamp DESC
				""");
		if (request.pageRequest().pageSize() > 0) {
			sql.append("LIMIT %d".formatted(request.pageRequest().pageSize()));
		}
		return this.jdbcClient.sql(sql.toString()) //
			.params(params) //
			.query((rs, rowNum) -> LogBuilder.log()
				.logId(rs.getLong("log_id"))
				.timestamp(rs.getTimestamp("timestamp").toInstant())
				.observedTimestamp(rs.getTimestamp("observed_timestamp").toInstant())
				.severityText(rs.getString("severity_text"))
				.severityNumber(rs.getInt("severity_number"))
				.serviceName(rs.getString("service_name"))
				.scope(rs.getString("scope"))
				.body(rs.getString("body"))
				.traceId(rs.getString("trace_id"))
				.spanId(rs.getString("span_id"))
				.traceFlags(rs.getInt("trace_flags"))
				.attributes(Json.parse(this.objectMapper, rs.getString("attributes")))
				.resourceAttributes(Json.parse(this.objectMapper, rs.getString("resource_attributes")))
				.build()) //
			.list();
	}

}
