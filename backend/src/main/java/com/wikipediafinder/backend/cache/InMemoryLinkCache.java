package com.wikipediafinder.backend.cache;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLinkCache implements LinkCache {
  private final Map<String, Set<String>> map = new ConcurrentHashMap<>();

  @Override
  public Set<String> get(String url) {
    return map.getOrDefault(url, null);
  }

  @Override
  public void put(String url, Set<String> neighbors) {
    if (url == null || neighbors == null) return;
    map.putIfAbsent(url, Collections.unmodifiableSet(neighbors));
  }
}
