package com.wikipediafinder.backend;

import com.wikipediafinder.backend.interfaces.PageNodeInterface;
import java.io.IOException;
import java.util.*;

/**
 * Represents a Wikipedia page and provides access to its outgoing (hyperlink) neighbors.
 *
 * <p>The class uses the Wikipedia API to efficiently fetch outgoing links without downloading the
 * entire HTML content. This is significantly faster than the previous Jsoup-based approach. For
 * production usage this class makes API requests during findOutgoingLinks(); in tests, a
 * lightweight mock subclass is used instead.
 */
public class PageNode implements PageNodeInterface {
  /*
   * Fields
   */
  private String url;
  private String pageTitle;
  private boolean validPage;
  private Map<String, PageNode> outLinks;
  private String WIKI_LINK_PREFIX = "https://en.wikipedia.org";

  /**
   * Construct a PageNode for the given URL. The constructor performs validation to ensure the URL
   * looks like a Wikipedia link. Unlike the previous implementation, this does NOT fetch the page
   * immediately - the page is assumed to be valid until findOutgoingLinks() is called, at which
   * point validity is determined based on whether the API call succeeds.
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
    this.pageTitle = WikipediaApiClient.urlToTitle(url);

    // Assume valid until proven otherwise during findOutgoingLinks()
    this.validPage = true;

    outLinks = new HashMap<>();
  }

  /**
   * Loads and caches up to 10 outgoing links using the Wikipedia API. This is significantly faster
   * than parsing HTML. Links are fetched using the Wikipedia API and converted to PageNode objects
   * (without making additional network requests per link).
   */
  public void findOutgoingLinks() {
    if (pageTitle == null) {
      validPage = false;
      return; // No links to process if the page title is invalid
    }

    try {
      Set<String> linkUrls = WikipediaApiClient.getOutgoingLinks(pageTitle, 10);

      for (String linkUrl : linkUrls) {
        if (!outLinks.containsKey(linkUrl)) {
          // Create a lightweight PageNode without triggering additional API calls
          try {
            PageNode outPage = new PageNode(linkUrl);
            outLinks.put(linkUrl, outPage);
          } catch (IllegalArgumentException ignored) {
            // Skip invalid links
          }
        }
      }
    } catch (IOException e) {
      System.err.println("Failed to fetch links for: " + url);
      e.printStackTrace();
      validPage = false;
    }
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
    return validPage;
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
