package am.ik.lognroll.logs;

import java.time.Instant;
import java.util.List;

import am.ik.pagination.CursorPageRequest;
import org.jilt.Builder;

public interface LogQuery {

	List<Log> findLatestLogs(SearchRequest request);

	@Builder
	record SearchRequest(String query, CursorPageRequest<Cursor> pageRequest) {

	}

	record Cursor(Instant timestamp) {

		public static Cursor valueOf(String s) {
			return new Cursor(Instant.parse(s));
		}
	}

}
