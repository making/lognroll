package am.ik.lognroll.logs;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import am.ik.lognroll.logs.LogQuery.Cursor;
import am.ik.lognroll.logs.filter.FilterExpressionTextParser;
import am.ik.pagination.CursorPageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class QueryController {

	private final LogQuery logQuery;

	private final LogStore logStore;

	private final FilterExpressionTextParser parser = new FilterExpressionTextParser();

	private final Logger logger = LoggerFactory.getLogger(QueryController.class);

	private final Resource dbFile;

	public QueryController(LogQuery logQuery, LogStore logStore, @Value("file://${lognroll.db.path}") Resource dbFile) {
		this.logQuery = logQuery;
		this.logStore = logStore;
		this.dbFile = dbFile;
	}

	@GetMapping(path = "/api/logs")
	public LogsResponse showLogs(@RequestParam(required = false) String query, CursorPageRequest<Cursor> pageRequest,
			@RequestParam(required = false) String filter, @RequestParam(required = false) Instant from,
			@RequestParam(required = false) Instant to) {
		SearchRequestBuilder searchRequest = SearchRequestBuilder.searchRequest()
			.pageRequest(pageRequest)
			.from(from)
			.to(to);
		if (StringUtils.hasText(query)) {
			searchRequest.query(query);
		}
		if (StringUtils.hasText(filter)) {
			try {
				searchRequest.filterExpression(this.parser.parse(filter));
			}
			catch (FilterExpressionTextParser.FilterExpressionParseException e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
			}
		}
		LogQuery.SearchRequest request = searchRequest.build();
		try {
			return new LogsResponse(this.logQuery.findLatestLogs(request));
		}
		catch (UncategorizedSQLException e) {
			if (e.getCause() instanceof SQLiteException) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getCause().getMessage(), e);
			}
			throw e;
		}
	}

	@GetMapping(path = "/api/logs/count")
	public CountResponse showCount(@RequestParam(required = false) String query,
			@RequestParam(required = false) String filter, @RequestParam(required = false) Instant from,
			@RequestParam(required = false) Instant to) {
		SearchRequestBuilder searchRequest = SearchRequestBuilder.searchRequest().from(from).to(to);
		if (StringUtils.hasText(query)) {
			searchRequest.query(query);
		}
		if (StringUtils.hasText(filter)) {
			try {
				searchRequest.filterExpression(this.parser.parse(filter));
			}
			catch (FilterExpressionTextParser.FilterExpressionParseException e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
			}
		}
		LogQuery.SearchRequest request = searchRequest.build();
		try {
			return new CountResponse(this.logQuery.count(request));
		}
		catch (UncategorizedSQLException e) {
			if (e.getCause() instanceof SQLiteException) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getCause().getMessage(), e);
			}
			throw e;
		}
	}

	@GetMapping(path = "/api/logs/frequencies")
	public FrequenciesResponse showFrequencies(@RequestParam(required = false) String query,
			@RequestParam(required = false) String filter, @RequestParam(required = false) Instant from,
			@RequestParam(required = false) Instant to,
			@RequestParam(required = false, defaultValue = "PT10M") Duration interval) {
		SearchRequestBuilder searchRequest = SearchRequestBuilder.searchRequest().from(from).to(to);
		if (StringUtils.hasText(query)) {
			searchRequest.query(query);
		}
		if (StringUtils.hasText(filter)) {
			try {
				searchRequest.filterExpression(this.parser.parse(filter));
			}
			catch (FilterExpressionTextParser.FilterExpressionParseException e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
			}
		}
		LogQuery.SearchRequest request = searchRequest.build();
		try {
			return new FrequenciesResponse(this.logQuery.findFrequencies(request, interval));
		}
		catch (UncategorizedSQLException e) {
			if (e.getCause() instanceof SQLiteException) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getCause().getMessage(), e);
			}
			throw e;
		}
	}

	@GetMapping(path = "/api/logs/download")
	public Resource downloadLogs() {
		return this.dbFile;
	}

	@DeleteMapping(path = "/api/logs")
	public ResponseEntity<Void> clearLogs() {
		logger.info("Clear logs!!!");
		this.logStore.clear();
		return ResponseEntity.noContent().build();
	}

	public record LogsResponse(List<Log> logs) {
	}

	public record CountResponse(long totalCount) {
	}

	public record FrequenciesResponse(List<LogQuery.Frequency> frequencies) {
	}

}
