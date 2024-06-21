package am.ik.lognroll.otlp;

import java.util.List;

import am.ik.lognroll.logs.Log;
import am.ik.lognroll.logs.LogStore;
import am.ik.lognroll.logs.Logs;
import com.google.protobuf.InvalidProtocolBufferException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.proto.logs.v1.LogsData;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogsV1Controller {

	private final LogStore logStore;

	private final MeterRegistry registry;

	public LogsV1Controller(LogStore logStore, MeterRegistry registry) {
		this.logStore = logStore;
		this.registry = registry;
	}

	@PostMapping(path = "/v1/logs",
			consumes = { MediaType.APPLICATION_PROTOBUF_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public void logs(@RequestBody LogsData logs) throws InvalidProtocolBufferException {
		List<Log> data = Logs.from(logs);
		Counter.builder("logs.ingested").register(this.registry).increment(data.size());
		this.logStore.addAll(data);
	}

}
