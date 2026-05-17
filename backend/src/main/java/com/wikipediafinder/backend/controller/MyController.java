package com.wikipediafinder.backend.controller;

import com.wikipediafinder.backend.BFS;
import com.wikipediafinder.backend.BFSResult;
import com.wikipediafinder.backend.PageNode;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger logger = LoggerFactory.getLogger(MyController.class);

  private final BFS bfs;

  public MyController(BFS bfs) {
    this.bfs = bfs;
  }

  @GetMapping("/health")
  public ResponseEntity<String> hello() {
    long startTime = System.currentTimeMillis();
    ResponseEntity<String> response = ResponseEntity.ok("[Health check] - This app is running!");
    long latencyMs = System.currentTimeMillis() - startTime;
    logger.info("GET /api/health completed in {}ms", latencyMs);
    return response;
  }

  @CrossOrigin(origins = {"http://localhost:5173", "https://wikipedia-path-finder.vercel.app"})
  @GetMapping("/getResults")
  public ResponseEntity<Object> getResults(
      @RequestParam String startinglink, @RequestParam String endinglink) {
    long startTime = System.currentTimeMillis();
    String safeStart = startinglink.replaceAll("[\r\n]", "_");
    String safeEnd = endinglink.replaceAll("[\r\n]", "_");
    logger.info(
        "GET /api/getResults called with startinglink={} endinglink={}", safeStart, safeEnd);
    try {
      BFSResult result = bfs.getPathWithStats(new PageNode(startinglink), new PageNode(endinglink));

      if (result.getPath() == null) {
        long latencyMs = System.currentTimeMillis() - startTime;
        logger.info("GET /api/getResults completed in {}ms - no path found", latencyMs);
        return new ResponseEntity<>(
            Map.of("message", "No path found or query took too long"), HttpStatus.OK);
      }

      long latencyMs = System.currentTimeMillis() - startTime;
      logger.info(
          "GET /api/getResults completed in {}ms - path length={} nodesExplored={}",
          latencyMs,
          result.getPath().size(),
          result.getNodesExplored());
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      long latencyMs = System.currentTimeMillis() - startTime;
      logger.warn(
          "GET /api/getResults completed in {}ms - bad request: {}", latencyMs, e.getMessage());
      return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }
}
