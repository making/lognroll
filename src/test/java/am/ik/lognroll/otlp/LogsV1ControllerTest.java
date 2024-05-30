package am.ik.lognroll.otlp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import am.ik.lognroll.logs.LogStore;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.proto.logs.v1.LogsData;
import am.ik.lognroll.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;

class LogsV1ControllerTest extends IntegrationTestBase {

	@Autowired
	LogStore logStore;

	@BeforeEach
	void resetData() {
		this.logStore.clear();
	}

	@Test
	void ingestProtobuf() throws Exception {
		String json = StreamUtils.copyToString(new ClassPathResource("logs.json").getInputStream(),
				StandardCharsets.UTF_8);
		LogsData.Builder builder = LogsData.newBuilder();
		JsonFormat.parser().merge(json, builder);
		ResponseEntity<Void> response = this.restClient.post()
			.uri("/v1/logs")
			.contentType(MediaType.APPLICATION_PROTOBUF)
			.header(HttpHeaders.AUTHORIZATION, "Bearer changeme")
			.body(builder.build())
			.retrieve()
			.toBodilessEntity();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertData();
	}

	@Test
	void ingestProtobufGzip() throws Exception {
		String json = StreamUtils.copyToString(new ClassPathResource("logs.json").getInputStream(),
				StandardCharsets.UTF_8);
		LogsData.Builder builder = LogsData.newBuilder();
		JsonFormat.parser().merge(json, builder);
		ResponseEntity<Void> response = this.restClient.post()
			.uri("/v1/logs")
			.contentType(MediaType.APPLICATION_PROTOBUF)
			.header(HttpHeaders.AUTHORIZATION, "Bearer changeme")
			.header(HttpHeaders.CONTENT_ENCODING, "gzip")
			.body(compress(builder.build().toByteArray()))
			.retrieve()
			.toBodilessEntity();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertData();
	}

	@Test
	void ingestJson() throws Exception {
		String json = StreamUtils.copyToString(new ClassPathResource("logs.json").getInputStream(),
				StandardCharsets.UTF_8);
		ResponseEntity<Void> response = this.restClient.post()
			.uri("/v1/logs")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, "Bearer changeme")
			.body(json)
			.retrieve()
			.toBodilessEntity();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertData();
	}

	@Test
	void ingestJsonGzip() throws Exception {
		byte[] json = new ClassPathResource("logs.json").getContentAsByteArray();
		ResponseEntity<Void> response = this.restClient.post()
			.uri("/v1/logs")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, "Bearer changeme")
			.header(HttpHeaders.CONTENT_ENCODING, "gzip")
			.body(compress(json))
			.retrieve()
			.toBodilessEntity();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertData();
	}

	void assertData() {
		String logs = this.restClient.get()
			.uri("/api/logs")
			.header(HttpHeaders.AUTHORIZATION, "Bearer changeme")
			.retrieve()
			.body(String.class);
		JsonContent<Object> content = this.json.from(logs);
		assertThat(content).extractingJsonPathNumberValue("$.length()").isEqualTo(1);
		assertThat(content).extractingJsonPathNumberValue("$[0].logId").isNotNull();
		assertThat(content).extractingJsonPathStringValue("$[0].timestamp").isEqualTo("2018-12-13T14:51:00.300Z");
		assertThat(content).extractingJsonPathStringValue("$[0].observedTimestamp")
			.isEqualTo("2018-12-13T14:51:00.300Z");
		assertThat(content).extractingJsonPathStringValue("$[0].severityText").isEqualTo("Information");
		assertThat(content).extractingJsonPathNumberValue("$[0].severityNumber").isEqualTo(10);
		assertThat(content).extractingJsonPathStringValue("$[0].serviceName").isEqualTo("my.service");
		assertThat(content).extractingJsonPathStringValue("$[0].scope").isEqualTo("my.library");
		assertThat(content).extractingJsonPathStringValue("$[0].body").isEqualTo("Example log record");
		assertThat(content).extractingJsonPathStringValue("$[0].traceId")
			.isEqualTo("e41f0414517bf7cd37f35d370f6ebd07adf7f35dc50bad02");
		assertThat(content).extractingJsonPathStringValue("$[0].spanId").isEqualTo("104135f41ec40b70b5075ef8");
		assertThat(content).extractingJsonPathNumberValue("$[0].attributes['int.attribute']").isEqualTo(10);
		assertThat(content).extractingJsonPathNumberValue("$[0].attributes['array.attribute'].length()").isEqualTo(2);
		assertThat(content).extractingJsonPathStringValue("$[0].attributes['array.attribute'][0]").isEqualTo("many");
		assertThat(content).extractingJsonPathStringValue("$[0].attributes['array.attribute'][1]").isEqualTo("values");
		assertThat(content).extractingJsonPathNumberValue("$[0].attributes['double.attribute']").isEqualTo(637.704);
		assertThat(content).extractingJsonPathStringValue("$[0].attributes['string.attribute']")
			.isEqualTo("some string");
		assertThat(content).extractingJsonPathMapValue("$[0].attributes['map.attribute']").hasSize(1);
		assertThat(content).extractingJsonPathStringValue("$[0].attributes['map.attribute']['some.map.key']")
			.isEqualTo("some value");
		assertThat(content).extractingJsonPathBooleanValue("$[0].attributes['boolean.attribute']").isTrue();
		assertThat(content).extractingJsonPathStringValue("$[0].attributes['my.scope.attribute']")
			.isEqualTo("some scope attribute");
		assertThat(content).extractingJsonPathMapValue("$[0].resourceAttributes").isEmpty();
	}

	static byte[] compress(byte[] body) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)) {
			gzipOutputStream.write(body);
		}
		return baos.toByteArray();
	}

}