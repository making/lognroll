package am.ik.lognroll.logs;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jilt.Builder;

@Builder
public record Log(Long logId, Instant timestamp, Instant observedTimestamp, String severity, String serviceName,
		String scope, String body, String traceId, String spanId, Map<String, Object> attributes,
		Map<String, Object> resourceAttributes) {

	@JsonIgnore
	public long resourceAttributesDigest() {
		return calculateMapDigest(this.resourceAttributes);
	}

	private static long calculateMapDigest(Map<?, ?> map) {
		long hash = 0;
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			hash += (entry.getKey().hashCode() ^ entry.getValue().hashCode());
		}
		return hash;
	}
}
