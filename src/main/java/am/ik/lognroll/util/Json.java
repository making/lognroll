package am.ik.lognroll.util;

import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class Json {

	private static final TypeReference<Map<String, Object>> jsonTypeReference = new TypeReference<>() {
	};

	public static String stringify(ObjectMapper objectMapper, Object json) {
		try {
			return objectMapper.writeValueAsString(json);
		}
		catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static Map<String, Object> parse(ObjectMapper objectMapper, String json) {
		if (json == null) {
			return Collections.emptyMap();
		}
		try {
			return objectMapper.readValue(json, jsonTypeReference);
		}
		catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

}