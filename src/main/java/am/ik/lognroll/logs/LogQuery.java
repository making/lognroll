package am.ik.lognroll.logs;

import java.time.Instant;
import java.util.List;

import am.ik.lognroll.logs.filter.Filter;
import am.ik.pagination.CursorPageRequest;
import org.jilt.Builder;

public interface LogQuery {

	List<Log> findLatestLogs(SearchRequest request);

	@Builder
	record SearchRequest(String query, CursorPageRequest<Cursor> pageRequest, Filter.Expression filterExpression,
			Instant from, Instant to) {

	}

	record Cursor(Instant timestamp, Instant observedTimestamp) {

		public static Cursor valueOf(String s) {
			String[] vals = s.split(",", 2);
			return new Cursor(Instant.parse(vals[0]), Instant.parse(vals[1]));
		}
	}

}
