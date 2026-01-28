package com.wikipediafinder.backend.cache;

import java.time.Duration;
import java.util.Set;

/**
 * Placeholder stub for RedisLinkCache. Redis support was removed in favor of SQL-backed cache. This
 * class exists only to keep compatibility for any accidental references; it does nothing.
 */
public class RedisLinkCache implements LinkCache {
  public RedisLinkCache(String redisUri, Duration ttl) {
    throw new UnsupportedOperationException(
        "RedisLinkCache is disabled; use SQL or InMemory cache");
  }

  @Override
  public Set<String> get(String url) {
    return null;
  }

  @Override
  public void put(String url, Set<String> neighbors) {
    // no-op
  }
}
