package am.ik.lognroll.logs;

import java.util.List;

import am.ik.lognroll.logs.LogQuery.Cursor;
import am.ik.pagination.CursorPageRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueryController {

	private final LogQuery logQuery;

	public QueryController(LogQuery logQuery) {
		this.logQuery = logQuery;
	}

	@GetMapping(path = "/api/logs")
	public List<Log> logs(@RequestParam(required = false) String query, CursorPageRequest<Cursor> pageRequest) {
		SearchRequestBuilder searchRequest = SearchRequestBuilder.searchRequest().pageRequest(pageRequest);
		if (StringUtils.hasText(query)) {
			searchRequest.query(query);
		}
		return this.logQuery.findLatestLogs(searchRequest.build());
	}

}
