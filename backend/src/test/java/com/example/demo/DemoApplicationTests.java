package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
		List<String> res = BFS.getPath(new PageNode("https://en.wikipedia.org/wiki/Economic_history_of_Argentina"), new PageNode("https://en.wikipedia.org/wiki/Economic_history_of_Argentina"));

		LinkedList<String> example = new LinkedList<>();
		example.add("https://en.wikipedia.org/wiki/Economic_history_of_Argentina");

		assertEquals(example, res, "test");

	}

}
