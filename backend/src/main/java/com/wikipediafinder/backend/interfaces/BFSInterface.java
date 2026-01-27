package com.wikipediafinder.backend.interfaces;

import com.wikipediafinder.backend.BFSResult;
import com.wikipediafinder.backend.PageNode;
import java.util.List;
import java.util.function.Function;

/**
 * Contract for BFS search utilities.
 *
 * <p>Implementations provide functions to find a path between two {@code PageNode}s and optionally
 * return statistics about the search.
 */
public interface BFSInterface {
  /** Find a path from {@code start} to {@code end} using a default node factory. */
  List<String> getPath(PageNode start, PageNode end);

  /** Find a path from {@code start} to {@code end} using the provided node factory. */
  List<String> getPath(PageNode start, PageNode end, Function<String, PageNode> nodeFactory);

  /** Find a path and return a {@link BFSResult} with statistics using default factory. */
  BFSResult getPathWithStats(PageNode start, PageNode end);

  /** Find a path and return a {@link BFSResult} with statistics using provided factory. */
  BFSResult getPathWithStats(PageNode start, PageNode end, Function<String, PageNode> nodeFactory);
}
