package com.wikipediafinder.backend;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

/**
 * Integration test to verify that caching is working correctly for BFS operations. Verifies that
 * the cache manager is properly configured with the expected caches.
 */
@SpringBootTest
public class CachingTest {

  @Autowired private BFS bfs;

  @Autowired private CacheManager cacheManager;

  @Test
  public void testCacheManagerIsConfigured() {
    assertNotNull(cacheManager, "CacheManager should be configured");
    assertNotNull(cacheManager.getCache("pathCache"), "pathCache should exist");
    assertNotNull(cacheManager.getCache("pathStatsCache"), "pathStatsCache should exist");
  }

  @Test
  public void testCachingWorksForGetPath() {
    // Clear cache before test
    cacheManager.getCache("pathCache").clear();

    PageNode start = new PageNode("https://en.wikipedia.org/wiki/Test_A");
    PageNode end = new PageNode("https://en.wikipedia.org/wiki/Test_B");

    // First call - cache miss, will compute and store
    bfs.getPath(start, end);

    // Verify cache entry exists after first call
    String cacheKey = start.getURL() + "->" + end.getURL();
    assertNotNull(
        cacheManager.getCache("pathCache").get(cacheKey),
        "Cache should contain entry for the path after first call");

    // Second call - cache hit, should return cached result
    bfs.getPath(start, end);

    // Verify cache still contains the entry
    assertNotNull(
        cacheManager.getCache("pathCache").get(cacheKey),
        "Cache should still contain entry after second call");
  }

  @Test
  public void testCachingWorksForGetPathWithStats() {
    // Clear cache before test
    cacheManager.getCache("pathStatsCache").clear();

    PageNode start = new PageNode("https://en.wikipedia.org/wiki/Test_C");
    PageNode end = new PageNode("https://en.wikipedia.org/wiki/Test_D");

    // First call - cache miss
    BFSResult result1 = bfs.getPathWithStats(start, end);

    // Second call - should use cache
    BFSResult result2 = bfs.getPathWithStats(start, end);

    // Verify cache entry exists
    String cacheKey = start.getURL() + "->" + end.getURL();
    assertNotNull(
        cacheManager.getCache("pathStatsCache").get(cacheKey),
        "Cache should contain entry for the path with stats");

    // Results should be equal (content comparison)
    assertEquals(result1.getPath(), result2.getPath(), "Cached result path should match");
    assertEquals(
        result1.getNodesExplored(),
        result2.getNodesExplored(),
        "Cached result nodes explored should match");
  }

  @Test
  public void testCacheDifferentParameters() {
    // Clear cache before test
    cacheManager.getCache("pathCache").clear();

    PageNode start1 = new PageNode("https://en.wikipedia.org/wiki/Test_E");
    PageNode end1 = new PageNode("https://en.wikipedia.org/wiki/Test_F");
    PageNode start2 = new PageNode("https://en.wikipedia.org/wiki/Test_G");
    PageNode end2 = new PageNode("https://en.wikipedia.org/wiki/Test_H");

    // Make calls with different parameters
    bfs.getPath(start1, end1);
    bfs.getPath(start2, end2);

    // Verify both entries are cached separately
    String cacheKey1 = start1.getURL() + "->" + end1.getURL();
    String cacheKey2 = start2.getURL() + "->" + end2.getURL();

    assertNotNull(
        cacheManager.getCache("pathCache").get(cacheKey1), "Cache should contain first path entry");
    assertNotNull(
        cacheManager.getCache("pathCache").get(cacheKey2),
        "Cache should contain second path entry");
  }
}
