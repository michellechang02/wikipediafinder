package com.wikipediafinder.backend.interfaces;

import com.wikipediafinder.backend.PageNode;
import java.util.Map;
import java.util.Set;

/** Interface representing a page node in the Wikipedia graph. */
public interface PageNodeInterface {
  void findOutgoingLinks();

  void setOutLinks(Map<String, PageNode> outLinks);

  boolean isValidPage();

  String getURL();

  Set<String> getOutLinks();

  Set<PageNode> getOutNodes();

  boolean hasLink(String link);
}
