package com.wikipediafinder.backend;

import com.wikipediafinder.backend.interfaces.BFSResultInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable result returned by a BFS search.
 *
 * <p>The result contains an ordered list of page URLs representing the discovered path (or {@code
 * null} if no path was found) and the number of nodes explored during the search. The path list,
 * when present, is unmodifiable.
 */
public class BFSResult implements BFSResultInterface {
  private final List<String> path;
  private final int nodesExplored;

  /**
   * Create a new BFSResult.
   *
   * @param path ordered list of URLs from start to end (or {@code null} if no path)
   * @param nodesExplored number of nodes expanded during the search
   */
  public BFSResult(List<String> path, int nodesExplored) {
    if (path == null) {
      this.path = null;
    } else {
      this.path = Collections.unmodifiableList(new ArrayList<>(path));
    }
    this.nodesExplored = nodesExplored;
  }

  /** Returns the discovered path or {@code null} if no path was found. */
  @Override
  public List<String> getPath() {
    return path;
  }

  /** Returns the number of nodes explored while searching for the path. */
  @Override
  public int getNodesExplored() {
    return nodesExplored;
  }
}
