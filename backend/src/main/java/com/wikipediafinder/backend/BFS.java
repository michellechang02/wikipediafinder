package com.wikipediafinder.backend;

import com.wikipediafinder.backend.interfaces.BFSInterface;
import java.util.*;
import java.util.function.Function;
import org.springframework.stereotype.Service;

/**
 * Breadth-first search service for finding paths between Wikipedia pages.
 *
 * <p>This class performs a breadth-first traversal over Wikipedia pages (represented by {@link
 * PageNode}). The public API is instance-based (implements {@link BFSInterface}), which makes the
 * implementation easier to mock and inject in tests and frameworks.
 */
@Service
public class BFS implements BFSInterface {

  // Default factory for production
  private static final Function<String, PageNode> DEFAULT_FACTORY = PageNode::new;

  /**
   * Instance method: find the shortest path (list of URLs) from {@code start} to {@code end}.
   *
   * <p>The algorithm performs a breadth-first search with a cap of 1000 expanded nodes to avoid
   * long-running queries. When the start and end URLs are equal, a singleton list containing the
   * start URL is returned.
   *
   * @param start starting PageNode (must be non-null)
   * @param end ending PageNode (must be non-null)
   * @return ordered list of URLs from start to end (inclusive), or {@code null} if no path found or
   *     cap reached.
   * @throws IllegalArgumentException if {@code start} or {@code end} is null
   */
  @Override
  public List<String> getPath(PageNode start, PageNode end) {
    return getPath(start, end, DEFAULT_FACTORY);
  }

  /**
   * Instance method: find the shortest path (list of URLs) using a custom node factory.
   *
   * @param start starting PageNode (must be non-null)
   * @param end ending PageNode (must be non-null)
   * @param nodeFactory function that, given a URL string, returns a {@link PageNode} instance used
   *     during expansion
   * @return ordered list of URLs from start to end (inclusive), or {@code null} if no path found or
   *     cap reached.
   * @throws IllegalArgumentException if {@code start} or {@code end} is null
   */
  @Override
  public List<String> getPath(
      PageNode start, PageNode end, Function<String, PageNode> nodeFactory) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end nodes cannot be null.");
    }
    String startUrl = start.getURL();
    String endUrl = end.getURL();
    if (startUrl.equals(endUrl)) {
      return Collections.singletonList(startUrl);
    }

    Queue<String> queue = new LinkedList<>();
    Set<String> discovered = new HashSet<>();
    Map<String, String> parents = new HashMap<>();
    Map<String, Set<String>> linkCache = new HashMap<>();
    queue.add(startUrl);
    discovered.add(startUrl);
    int nodeCnt = 0;
    while (!queue.isEmpty() && nodeCnt < 1000) {
      String currentUrl = queue.poll();
      nodeCnt++;
      // Fetch outgoing links (cache per URL)
      Set<String> neighbors = linkCache.get(currentUrl);
      if (neighbors == null) {
        PageNode node = nodeFactory.apply(currentUrl);
        node.findOutgoingLinks();
        neighbors = new HashSet<>();
        for (PageNode n : node.getOutNodes()) {
          neighbors.add(n.getURL());
        }
        linkCache.put(currentUrl, neighbors);
      }
      for (String neighborUrl : neighbors) {
        if (!discovered.contains(neighborUrl)) {
          discovered.add(neighborUrl);
          parents.put(neighborUrl, currentUrl);
          queue.add(neighborUrl);
          if (neighborUrl.equals(endUrl)) {
            // Early exit: reconstruct path
            List<String> result = new LinkedList<>();
            String temp = endUrl;
            while (!temp.equals(startUrl)) {
              result.add(temp);
              temp = parents.get(temp);
            }
            result.add(startUrl);
            Collections.reverse(result);
            return result;
          }
        }
      }
    }
    return null;
  }

  // ---- getPathWithStats counterpart methods ----

  /**
   * Instance method: find the shortest path and return a {@link BFSResult} that includes stats
   * (number of nodes explored).
   *
   * @param start starting PageNode (must be non-null)
   * @param end ending PageNode (must be non-null)
   * @return {@link BFSResult} containing the path (or null) and nodes explored count
   * @throws IllegalArgumentException if {@code start} or {@code end} is null
   */
  @Override
  public BFSResult getPathWithStats(PageNode start, PageNode end) {
    return getPathWithStats(start, end, DEFAULT_FACTORY);
  }

  @Override
  public BFSResult getPathWithStats(
      PageNode start, PageNode end, Function<String, PageNode> nodeFactory) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end nodes cannot be null.");
    }
    String startUrl = start.getURL();
    String endUrl = end.getURL();
    if (startUrl.equals(endUrl)) {
      return new BFSResult(Collections.singletonList(startUrl), 1);
    }
    Queue<String> queue = new LinkedList<>();
    Set<String> discovered = new HashSet<>();
    Map<String, String> parents = new HashMap<>();
    Map<String, Set<String>> linkCache = new HashMap<>();
    queue.add(startUrl);
    discovered.add(startUrl);
    int nodeCnt = 0;
    while (!queue.isEmpty() && nodeCnt < 1000) {
      String currentUrl = queue.poll();
      nodeCnt++;
      Set<String> neighbors = linkCache.get(currentUrl);
      if (neighbors == null) {
        PageNode node = nodeFactory.apply(currentUrl);
        node.findOutgoingLinks();
        neighbors = new HashSet<>();
        for (PageNode n : node.getOutNodes()) {
          neighbors.add(n.getURL());
        }
        linkCache.put(currentUrl, neighbors);
      }
      for (String neighborUrl : neighbors) {
        if (!discovered.contains(neighborUrl)) {
          discovered.add(neighborUrl);
          parents.put(neighborUrl, currentUrl);
          queue.add(neighborUrl);
          if (neighborUrl.equals(endUrl)) {
            // Early exit: reconstruct path
            List<String> result = new LinkedList<>();
            String temp = endUrl;
            while (!temp.equals(startUrl)) {
              result.add(temp);
              temp = parents.get(temp);
            }
            result.add(startUrl);
            Collections.reverse(result);
            return new BFSResult(result, nodeCnt);
          }
        }
      }
    }
    // Not found or cap reached
    return new BFSResult(null, nodeCnt);
  }
}
