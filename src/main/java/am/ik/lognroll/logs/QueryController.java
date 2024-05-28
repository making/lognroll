package am.ik.lognroll.logs;

import java.time.Instant;
import java.util.List;

import am.ik.lognroll.logs.LogQuery.Cursor;
import am.ik.lognroll.logs.filter.FilterExpressionTextParser;
import am.ik.pagination.CursorPageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueryController {

	private final LogQuery logQuery;

	private final LogStore logStore;

	private final FilterExpressionTextParser parser = new FilterExpressionTextParser();

	private final Logger logger = LoggerFactory.getLogger(QueryController.class);

	public QueryController(LogQuery logQuery, LogStore logStore) {
		this.logQuery = logQuery;
		this.logStore = logStore;
	}

	@GetMapping(path = "/api/logs")
	public List<Log> logs(@RequestParam(required = false) String query, CursorPageRequest<Cursor> pageRequest,
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
			searchRequest.filterExpression(this.parser.parse(filter));
		}
		return this.logQuery.findLatestLogs(searchRequest.build());
	}

	@DeleteMapping(path = "/api/logs")
	public ResponseEntity<Void> clearLogs() {
		logger.info("Clear logs!!!");
		this.logStore.clear();
		return ResponseEntity.noContent().build();
	}

}
