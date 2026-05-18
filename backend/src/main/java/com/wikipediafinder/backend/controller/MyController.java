package com.wikipediafinder.backend.controller;

import com.wikipediafinder.backend.BFS;
import com.wikipediafinder.backend.BFSResult;
import com.wikipediafinder.backend.PageNode;
import com.wikipediafinder.backend.logging.BackendLogger;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes the API endpoints for the Wikipedia path finder. The controller is
 * thin and delegates search work to the injected {@link BFS} service.
 */
@RestController
@RequestMapping("/api")
public class MyController {

  private final BFS bfs;
  private final BackendLogger backendLogger;

  public MyController(BFS bfs, BackendLogger backendLogger) {
    this.bfs = bfs;
    this.backendLogger = backendLogger;
  }

  @GetMapping("/health")
  public ResponseEntity<String> hello() {
    long startTime = System.currentTimeMillis();
    backendLogger.logHealthCheckStart();
    ResponseEntity<String> response = ResponseEntity.ok("[Health check] - This app is running!");
    long latencyMs = System.currentTimeMillis() - startTime;
    backendLogger.logHealthCheckComplete(latencyMs);
    return response;
  }

  @CrossOrigin(origins = {"http://localhost:5173", "https://wikipedia-path-finder.vercel.app"})
  @GetMapping("/getResults")
  public ResponseEntity<Object> getResults(
      @RequestParam String startinglink, @RequestParam String endinglink) {
    long startTime = System.currentTimeMillis();
    backendLogger.logGetResultsStart(startinglink, endinglink);
    try {
      BFSResult result = bfs.getPathWithStats(new PageNode(startinglink), new PageNode(endinglink));

      if (result.getPath() == null) {
        long latencyMs = System.currentTimeMillis() - startTime;
        backendLogger.logGetResultsNoPath(latencyMs);
        return new ResponseEntity<>(
            Map.of("message", "No path found or query took too long"), HttpStatus.OK);
      }

      long latencyMs = System.currentTimeMillis() - startTime;
      backendLogger.logGetResultsSuccess(latencyMs, result);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      long latencyMs = System.currentTimeMillis() - startTime;
      backendLogger.logGetResultsError(latencyMs, e.getMessage());
      return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }
}
