package com.wikipediafinder.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wikipediafinder.backend.BFS;
import com.wikipediafinder.backend.PageNode;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Slice test for MyController: uses MockMvc and mocks the BFS service so tests are focused on
 * controller behavior and responses.
 */
@WebMvcTest(MyController.class)
public class MyControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private BFS bfs;

  @Test
  public void healthEndpointReturnsOk() throws Exception {
    mockMvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(content().string("[Health check] - This app is running!"));
  }

  @Test
  public void getResultsReturnsPathWhenFound() throws Exception {
    when(bfs.getPath(any(PageNode.class), any(PageNode.class)))
        .thenReturn(
            Arrays.asList("https://en.wikipedia.org/wiki/A", "https://en.wikipedia.org/wiki/B"));

    mockMvc
        .perform(
            get("/api/getResults")
                .param("startinglink", "https://en.wikipedia.org/wiki/A")
                .param("endinglink", "https://en.wikipedia.org/wiki/B"))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .json("[\"https://en.wikipedia.org/wiki/A\",\"https://en.wikipedia.org/wiki/B\"]"));
  }

  @Test
  public void getResultsReturnsMessageWhenNoPath() throws Exception {
    when(bfs.getPath(any(PageNode.class), any(PageNode.class))).thenReturn(null);

    mockMvc
        .perform(
            get("/api/getResults")
                .param("startinglink", "https://en.wikipedia.org/wiki/A")
                .param("endinglink", "https://en.wikipedia.org/wiki/B"))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"message\":\"No path found or query took too long\"}"));
  }

  @Test
  public void getResultsReturnsBadRequestWhenBfsThrows() throws Exception {
    when(bfs.getPath(any(PageNode.class), any(PageNode.class)))
        .thenThrow(new IllegalArgumentException("invalid input"));

    mockMvc
        .perform(
            get("/api/getResults")
                .param("startinglink", "https://en.wikipedia.org/wiki/A")
                .param("endinglink", "https://en.wikipedia.org/wiki/B"))
        .andExpect(status().isBadRequest())
        .andExpect(content().json("{\"error\":\"invalid input\"}"));
  }
}
