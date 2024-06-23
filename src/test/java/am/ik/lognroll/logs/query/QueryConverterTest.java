package am.ik.lognroll.logs.query;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryConverterTest {

	@Test
	void simple() {
		String query = Sqlite3QueryConverter.convertQuery("hello");
		assertThat(query).isEqualTo("\"hello\"");
	}

	@Test
	void and() {
		String query = Sqlite3QueryConverter.convertQuery("hello world");
		assertThat(query).isEqualTo("\"hello\" AND \"world\"");
	}

	@Test
	void or() {
		String query = Sqlite3QueryConverter.convertQuery("hello or world");
		assertThat(query).isEqualTo("\"hello\" OR \"world\"");
	}

	@Test
	void not() {
		String query = Sqlite3QueryConverter.convertQuery("hello -world");
		assertThat(query).isEqualTo("\"hello\" NOT \"world\"");
	}

	@Test
	void singleNot() {
		String query = Sqlite3QueryConverter.convertQuery("-hello");
		assertThat(query).isEqualTo("NOT \"hello\"");
	}

	@Test
	void nest() {
		String query = Sqlite3QueryConverter.convertQuery("hello (world or java)");
		assertThat(query).isEqualTo("\"hello\" AND (\"world\" OR \"java\")");
	}

}