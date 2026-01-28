package com.wikipediafinder.backend;

import com.wikipediafinder.backend.interfaces.PageNodeInterface;
import java.io.IOException;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Represents a Wikipedia page and provides access to its outgoing (hyperlink) neighbors.
 *
 * <p>The class lazily loads the page content using Jsoup at construction time and exposes helpers
 * to extract and cache outgoing links. For production usage this class is relatively heavy-weight
 * (creates a network request during construction); in tests, a lightweight mock subclass is used
 * instead.
 */
public class PageNode implements PageNodeInterface {
  /*
   * Fields
   */
  private String url;
  private Document page;
  private Map<String, PageNode> outLinks;
  private String WIKI_LINK_PREFIX = "https://en.wikipedia.org";

  /**
   * Construct a PageNode for the given URL. The constructor performs validation to ensure the URL
   * looks like a Wikipedia link and attempts to fetch the page using Jsoup. If the fetch fails the
   * node is still created but marked as invalid via {@link #isValidPage()}.
   *
   * @param url fully-qualified Wikipedia URL (must start with the official wiki prefix)
   * @throws IllegalArgumentException if the URL is null or does not start with the wiki prefix
   */
  public PageNode(String url) {

    // Validate input URL
    if (url == null
        || url.length() < WIKI_LINK_PREFIX.length()
        || !url.startsWith(WIKI_LINK_PREFIX)) {
      throw new IllegalArgumentException("Invalid URL for PageNode: " + url);
    }

    this.url = url;

    // Create the Document object using Jsoup with sensible timeouts and a user agent
    try {
      page =
          Jsoup.connect(url)
              .userAgent("Mozilla/5.0 (compatible; WikipediaFinder/1.0; +https://example.com)")
              .timeout(5000) // 5s timeout to avoid hanging on slow requests
              .maxBodySize(0)
              .get();
      System.out.println("Successfully connected to: " + url);
    } catch (IOException e) {
      System.err.println("Failed to connect to: " + url);
      e.printStackTrace();
      page = null;
    }

    outLinks = new HashMap<>();
  }

  /**
   * Loads and caches up to 10 outgoing links extracted from the page's bodyContent element. Links
   * that are non-content (files, categories, help pages, etc.) are skipped.
   *
   * <p>Important performance change: this method no longer constructs new PageNode objects for each
   * discovered link. Instead we record the outgoing URL keys only. This prevents eager network
   * fetches for every neighbor and defers fetching until neighbors are actually expanded by the BFS
   * algorithm.
   */
  public void findOutgoingLinks() {
    if (page == null) {
      return; // No links to process if the page is invalid
    }

    Element content = page.getElementById("bodyContent");
    if (content == null) {
      return; // Skip if the page structure is unexpected
    }

    Elements links = content.getElementsByTag("a");
    int count = 0;

    for (Element link : links) {
      if (count >= 10) {
        break; // Limit to 10 outgoing links
      }

      String href = link.attr("href");
      if (isSkippableLink(href)) {
        continue; // Skip non-content links
      }

      String absoluteUrl = link.absUrl("href"); // Resolve to absolute URL
      if (absoluteUrl.startsWith(WIKI_LINK_PREFIX) && !outLinks.containsKey(absoluteUrl)) {
        try {
          // Performance: don't eagerly create PageNode instances for each discovered link.
          // Instead store the URL key; the BFS will instantiate PageNode objects only when
          // expanding a node (i.e. when we actually need to fetch its outgoing links).
          outLinks.put(absoluteUrl, null);
          count++;
        } catch (IllegalArgumentException ignored) {
          // Skip invalid links (defensive - shouldn't happen since we only store URLs)
        }
      }
    }
  }

  /*
   * Checks if a link should be skipped
   */
  private boolean isSkippableLink(String link) {
    return link.isEmpty()
        || link.charAt(0) == '#'
        || link.contains("index.php")
        || link.contains("File:")
        || link.contains("Category:")
        || link.contains("Help:")
        || link.contains("Special:")
        || link.contains("Wikipedia:");
  }

  /**
   * Replace the outgoing links map (used by tests to mock graph structure).
   *
   * @throws IllegalArgumentException if {@code outLinks} is null
   */
  public void setOutLinks(Map<String, PageNode> outLinks) {
    if (outLinks == null) {
      throw new IllegalArgumentException("Outgoing links map cannot be null.");
    }
    this.outLinks = outLinks;
  }

  /** Returns true if the underlying page was successfully loaded. */
  public boolean isValidPage() {
    return page != null;
  }

  /** Returns the full URL of this page. */
  public String getURL() {
    return this.url;
  }

  /** Returns the set of outgoing link URLs (keys of the internal map). */
  public Set<String> getOutLinks() {
    return this.outLinks.keySet();
  }

  /** Returns the set of outgoing PageNode objects (values of the internal map). */
  public Set<PageNode> getOutNodes() {
    return new HashSet<>(outLinks.values());
  }

  /** Returns true if this node has an outgoing link to the provided URL. */
  public boolean hasLink(String link) {
    return outLinks.containsKey(link);
  }

  /** Equality is based on the page URL. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PageNode otherNode = (PageNode) o;
    return this.url.equals(otherNode.getURL());
  }
}
