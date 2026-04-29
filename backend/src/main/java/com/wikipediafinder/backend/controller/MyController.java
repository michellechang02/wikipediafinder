package com.wikipediafinder.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wikipediafinder.backend.BFS;
import com.wikipediafinder.backend.BFSResult;
import com.wikipediafinder.backend.PageNode;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * REST controller that exposes the API endpoints for the Wikipedia path finder. The controller is
 * thin and delegates search work to the injected {@link BFS} service.
 */
@RestController
@RequestMapping("/api")
public class MyController {

  private final BFS bfs;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ExecutorService executor = Executors.newCachedThreadPool();

  public MyController(BFS bfs) {
    this.bfs = bfs;
  }

  @PreDestroy
  public void shutdown() {
    executor.shutdown();
    try {
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  @GetMapping("/health")
  public ResponseEntity<String> hello() {
    return ResponseEntity.ok("[Health check] - This app is running!");
  }

  @CrossOrigin(origins = {"http://localhost:5173", "https://wikipedia-path-finder.vercel.app"})
  @GetMapping("/getResults")
  public ResponseEntity<Object> getResults(
      @RequestParam String startinglink, @RequestParam String endinglink) {
    try {
      BFSResult result = bfs.getPathWithStats(new PageNode(startinglink), new PageNode(endinglink));

      if (result.getPath() == null) {
        return new ResponseEntity<>(
            Map.of("message", "No path found or query took too long"), HttpStatus.OK);
      }

      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Streaming endpoint that runs BFS and emits Server-Sent Events so the client can observe
   * real-time progress. Events:
   *
   * <ul>
   *   <li>{@code progress} – {@code {"nodesExplored": N}} emitted after each node is explored
   *   <li>{@code result} – final path payload (same shape as {@code /getResults})
   *   <li>{@code error} – {@code {"error": "message"}} on bad input
   * </ul>
   */
  @CrossOrigin(origins = {"http://localhost:5173", "https://wikipedia-path-finder.vercel.app"})
  @GetMapping(value = "/getResultsStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter getResultsStream(
      @RequestParam String startinglink, @RequestParam String endinglink) {
    SseEmitter emitter = new SseEmitter(120_000L);

    executor.execute(
        () -> {
          final AtomicBoolean clientDisconnected = new AtomicBoolean(false);
          try {
            PageNode start = new PageNode(startinglink);
            PageNode end = new PageNode(endinglink);

            BFSResult result =
                bfs.getPathWithStats(
                    start,
                    end,
                    PageNode::new,
                    nodeCount -> {
                      if (clientDisconnected.get()) {
                        // Signal BFS to stop by throwing an unchecked exception that
                        // propagates out of the lambda and terminates the BFS loop.
                        throw new ClientDisconnectedException();
                      }
                      try {
                        emitter.send(
                            SseEmitter.event()
                                .name("progress")
                                .data(Map.of("nodesExplored", nodeCount)));
                      } catch (IOException ignored) {
                        // Client disconnected; mark flag so next callback iteration stops BFS.
                        clientDisconnected.set(true);
                      }
                    });

            if (result.getPath() == null) {
              emitter.send(
                  SseEmitter.event()
                      .name("result")
                      .data(
                          Map.of(
                              "message",
                              "No path found or query took too long",
                              "nodesExplored",
                              result.getNodesExplored())));
            } else {
              emitter.send(
                  SseEmitter.event().name("result").data(objectMapper.writeValueAsString(result)));
            }
            emitter.complete();
          } catch (ClientDisconnectedException e) {
            // Client disconnected mid-BFS; just complete the emitter silently.
            emitter.complete();
          } catch (IllegalArgumentException e) {
            try {
              emitter.send(SseEmitter.event().name("error").data(Map.of("error", e.getMessage())));
            } catch (IOException ignored) {
              // ignore
            }
            emitter.complete();
          } catch (IOException e) {
            emitter.completeWithError(e);
          }
        });

    return emitter;
  }

  /** Thrown inside the BFS progress callback to abort BFS when the client has disconnected. */
  private static final class ClientDisconnectedException extends RuntimeException {
    ClientDisconnectedException() {
      super(null, null, true, false); // Suppress stack-trace filling for performance.
    }
  }
}
