package com.wikipediafinder.backend.controller;

import com.wikipediafinder.backend.BFS;
import com.wikipediafinder.backend.PageNode;
import java.util.List;
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

  public MyController(BFS bfs) {
    this.bfs = bfs;
  }

  @GetMapping("/health")
  public ResponseEntity<String> hello() {
    return ResponseEntity.ok("[Health check] - This app is running!");
  }

  @CrossOrigin(origins = "http://localhost:5173")
  @GetMapping("/getResults")
  public ResponseEntity<Object> getResults(
      @RequestParam String startinglink, @RequestParam String endinglink) {
    try {
      List<String> results = bfs.getPath(new PageNode(startinglink), new PageNode(endinglink));

      if (results == null) {
        return new ResponseEntity<>(
            Map.of("message", "No path found or query took too long"), HttpStatus.OK);
      }

      return new ResponseEntity<>(results, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }
}
