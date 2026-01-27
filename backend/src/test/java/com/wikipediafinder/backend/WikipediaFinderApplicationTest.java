package com.wikipediafinder.backend;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WikipediaFinderApplicationTest {
  @Test
  public void contextLoads() {
    // This test will pass if the Spring context loads successfully
    assertTrue(true);
  }
}
