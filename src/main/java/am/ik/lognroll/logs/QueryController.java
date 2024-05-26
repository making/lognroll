package am.ik.lognroll.logs;

import java.time.Instant;
import java.util.List;

import am.ik.lognroll.logs.LogQuery.Cursor;
import am.ik.lognroll.logs.filter.FilterExpressionTextParser;
import am.ik.pagination.CursorPageRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueryController {

	private final LogQuery logQuery;

	private final FilterExpressionTextParser parser = new FilterExpressionTextParser();

	public QueryController(LogQuery logQuery) {
		this.logQuery = logQuery;
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

}
