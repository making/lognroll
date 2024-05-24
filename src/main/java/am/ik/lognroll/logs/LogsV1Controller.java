package am.ik.lognroll.logs;

import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;
import io.opentelemetry.proto.logs.v1.LogsData;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogsV1Controller {

	private final LogStore logStore;

	public LogsV1Controller(LogStore logStore) {
		this.logStore = logStore;
	}

	@PostMapping(path = "/v1/logs",
			consumes = { MediaType.APPLICATION_PROTOBUF_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public void logs(@RequestBody LogsData logs) throws InvalidProtocolBufferException {
		List<Log> data = Log.from(logs);
		this.logStore.addAll(data);
	}

}
