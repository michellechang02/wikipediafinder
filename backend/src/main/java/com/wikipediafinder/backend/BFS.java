package com.wikipediafinder.backend;

import com.wikipediafinder.backend.cache.InMemoryLinkCache;
import com.wikipediafinder.backend.cache.LinkCache;
import com.wikipediafinder.backend.interfaces.BFSInterface;
import java.util.*;
import java.util.concurrent.*;
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

  // Maximum number of nodes to expand to avoid very long-running searches
  private static final int MAX_EXPANDED_NODES = 1000;

  // Toggle parallel fetching of outgoing links (helps when network latency dominates)
  private static final boolean PARALLEL_FETCH = true;

  // Abstracted cache (can be Redis-backed or in-memory)
  private final LinkCache linkCache;

  private final ExecutorService fetchPool =
      PARALLEL_FETCH
          ? Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
          : null;

  // Default constructor used in tests and when no DI is available — uses in-memory cache
  public BFS() {
    this.linkCache = new InMemoryLinkCache();
  }

  // Constructor for DI (Spring will inject the appropriate LinkCache bean)
  public BFS(LinkCache linkCache) {
    this.linkCache = linkCache != null ? linkCache : new InMemoryLinkCache();
  }

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

    queue.add(startUrl);
    discovered.add(startUrl);
    int nodeCnt = 0;

    while (!queue.isEmpty() && nodeCnt < MAX_EXPANDED_NODES) {
      String currentUrl = queue.poll();
      nodeCnt++;

      // Fetch outgoing links (check cache first)
      Set<String> neighbors = linkCache.get(currentUrl);
      if (neighbors == null) {
        // Not cached: build using the factory (which will fetch the page)
        if (PARALLEL_FETCH) {
          // Fetch neighbors asynchronously to allow multiple network calls to overlap
          try {
            neighbors = fetchNeighborsParallel(currentUrl, nodeFactory);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            neighbors = Collections.emptySet();
          }
        } else {
          PageNode node = nodeFactory.apply(currentUrl);
          node.findOutgoingLinks();
          neighbors = new HashSet<>(node.getOutLinks());
        }
        // Cache an unmodifiable set to avoid accidental modification
        linkCache.put(currentUrl, Collections.unmodifiableSet(neighbors));
        neighbors = linkCache.get(currentUrl);
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

    queue.add(startUrl);
    discovered.add(startUrl);
    int nodeCnt = 0;

    while (!queue.isEmpty() && nodeCnt < MAX_EXPANDED_NODES) {
      String currentUrl = queue.poll();
      nodeCnt++;

      Set<String> neighbors = linkCache.get(currentUrl);
      if (neighbors == null) {
        if (PARALLEL_FETCH) {
          try {
            neighbors = fetchNeighborsParallel(currentUrl, nodeFactory);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            neighbors = Collections.emptySet();
          }
        } else {
          PageNode node = nodeFactory.apply(currentUrl);
          node.findOutgoingLinks();
          neighbors = new HashSet<>(node.getOutLinks());
        }
        linkCache.put(currentUrl, Collections.unmodifiableSet(neighbors));
        neighbors = linkCache.get(currentUrl);
      }

      for (String neighborUrl : neighbors) {
        if (!discovered.contains(neighborUrl)) {
          discovered.add(neighborUrl);
          parents.put(neighborUrl, currentUrl);
          queue.add(neighborUrl);
          if (neighborUrl.equals(endUrl)) {
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
    return new BFSResult(null, nodeCnt);
  }

  // Helper to fetch neighbors using an ExecutorService so network I/O can overlap
  private Set<String> fetchNeighborsParallel(String url, Function<String, PageNode> nodeFactory)
      throws InterruptedException {
    // Simple strategy: submit a task to fetch this URL's neighbors and wait a bounded time
    Future<Set<String>> future =
        fetchPool.submit(
            () -> {
              PageNode node = nodeFactory.apply(url);
              node.findOutgoingLinks();
              return extractNeighborUrls(node);
            });

    try {
      // Wait up to 6 seconds for neighbor fetch; if it times out, return empty set so BFS can
      // continue exploring other nodes.
      return future.get(6, TimeUnit.SECONDS);
    } catch (ExecutionException e) {
      // Fetch failed; return empty set
      return Collections.emptySet();
    } catch (TimeoutException e) {
      future.cancel(true);
      return Collections.emptySet();
    }
  }

  // Extract neighbor URLs from a PageNode. Support both getOutLinks() (URL-keyed map) and
  // getOutNodes() (tests/mock override) to remain compatible with existing tests and
  // implementations.
  private Set<String> extractNeighborUrls(PageNode node) {
    Set<String> res = new HashSet<>();
    try {
      Set<String> outLinks = node.getOutLinks();
      if (outLinks != null && !outLinks.isEmpty()) {
        res.addAll(outLinks);
      } else {
        for (PageNode pn : node.getOutNodes()) {
          if (pn != null && pn.getURL() != null) {
            res.add(pn.getURL());
          }
        }
      }
    } catch (Exception e) {
      // Fallback: return empty set on unexpected errors
    }
    return res;
  }
}
