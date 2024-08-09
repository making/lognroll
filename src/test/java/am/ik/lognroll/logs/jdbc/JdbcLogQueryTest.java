package am.ik.lognroll.logs.jdbc;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcLogQueryTest {

	@Test
	void splitList() {
		List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		List<List<Integer>> lists = JdbcLogQuery.splitList(list, 3);
		assertThat(lists).containsExactly(List.of(1, 2, 3), List.of(4, 5, 6), List.of(7, 8, 9), List.of(10));
	}

}