package am.ik.lognroll.logs;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import am.ik.lognroll.logs.filter.Filter;
import am.ik.pagination.CursorPageRequest;
import jakarta.annotation.Nullable;
import org.jilt.Builder;

public interface LogQuery {

	List<Log> findLatestLogs(SearchRequest request);

	long count(SearchRequest request);

	List<Frequency> findFrequencies(SearchRequest request, Duration interval);

	record Frequency(Instant date, long count) {

	}

	@Builder
	record SearchRequest(String query, @Nullable CursorPageRequest<Cursor> pageRequest,
			@Nullable Filter.Expression filterExpression, @Nullable Instant from, @Nullable Instant to) {

	}

	record Cursor(Instant timestamp, Instant observedTimestamp) {

		public static Cursor valueOf(String s) {
			String[] vals = s.split(",", 2);
			return new Cursor(Instant.parse(vals[0]), Instant.parse(vals[1]));
		}
	}

}
