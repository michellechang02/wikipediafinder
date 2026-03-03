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
    long startTime1 = System.nanoTime();
    bfs.getPath(start, end);
    long duration1 = System.nanoTime() - startTime1;

    // Second call - cache hit, should be faster
    long startTime2 = System.nanoTime();
    bfs.getPath(start, end);
    long duration2 = System.nanoTime() - startTime2;

    // Verify cache entry exists
    String cacheKey = start.getURL() + "->" + end.getURL();
    assertNotNull(
        cacheManager.getCache("pathCache").get(cacheKey),
        "Cache should contain entry for the path");

    // Second call should be significantly faster (cached)
    // Note: This is a heuristic - cached calls should be much faster
    assertTrue(
        duration2 < duration1 / 2,
        "Cached call should be faster than initial call. Duration1: "
            + duration1
            + ", Duration2: "
            + duration2);
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

    // Results should be identical (same object from cache)
    assertSame(result1, result2, "Cached result should be the same object");
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
