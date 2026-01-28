package com.wikipediafinder.backend.cache;

import java.util.Set;

/** Simple interface for caching outgoing link sets for a page URL. */
public interface LinkCache {
  Set<String> get(String url);

  void put(String url, Set<String> neighbors);
}
