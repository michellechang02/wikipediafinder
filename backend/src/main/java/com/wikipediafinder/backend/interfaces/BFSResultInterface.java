package com.wikipediafinder.backend.interfaces;

import java.util.List;

/** Interface for BFS search result. */
public interface BFSResultInterface {
  /** Returns the discovered path or null if none. */
  List<String> getPath();

  /** Returns how many nodes were explored during the search. */
  int getNodesExplored();
}
