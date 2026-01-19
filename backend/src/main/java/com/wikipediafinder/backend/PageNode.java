package com.wikipediafinder.backend;

import java.io.IOException;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PageNode {
    /*
     * Fields
     */
    private String url;
    private Document page;
    private Map<String, PageNode> outLinks;
    private String WIKI_LINK_PREFIX = "https://en.wikipedia.org";

    /*
     * Constructor for the PageNode object
     *
     * @param url The URL of the Wikipedia page
     * @throws IllegalArgumentException if the input is null or does not start with the Wikipedia link prefix
     */
    public PageNode(String url) {

        // Validate input URL
        if (url == null || url.length() < WIKI_LINK_PREFIX.length() || !url.startsWith(WIKI_LINK_PREFIX)) {
            throw new IllegalArgumentException("Invalid URL for PageNode: " + url);
        }

        this.url = url;

        // Create the Document object using Jsoup
        try {
            page = Jsoup.connect(url).get();
            System.out.println("Successfully connected to: " + url);
        } catch (IOException e) {
            System.err.println("Failed to connect to: " + url);
            e.printStackTrace();
            page = null;
        }

        outLinks = new HashMap<>();
    }

    /*
     * Loads outgoing links for this page.
     *
     * Caps the number of outgoing links to 10 to limit memory usage and computation time.
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
                    PageNode outPage = new PageNode(absoluteUrl);
                    if (outPage.isValidPage()) {
                        outLinks.put(absoluteUrl, outPage);
                        count++;
                    }
                } catch (IllegalArgumentException ignored) {
                    // Skip invalid links
                }
            }
        }
    }

    /*
     * Checks if a link should be skipped
     */
    private boolean isSkippableLink(String link) {
        return link.isEmpty() || link.charAt(0) == '#' ||
            link.contains("index.php") || link.contains("File:") ||
            link.contains("Category:") || link.contains("Help:") ||
            link.contains("Special:") || link.contains("Wikipedia:");
    }

    /*
     * Sets outgoing links
     */
    public void setOutLinks(Map<String, PageNode> outLinks) {
        if (outLinks == null) {
            throw new IllegalArgumentException("Outgoing links map cannot be null.");
        }
        this.outLinks = outLinks;
    }



    /*
     * Checks if this page is valid (successfully loaded)
     */
    public boolean isValidPage() {
        return page != null;
    }

    /*
     * Gets the URL of this PageNode
     */
    public String getURL() {
        return this.url;
    }

    /*
     * Gets the URLs of outgoing links as a set
     */
    public Set<String> getOutLinks() {
        return this.outLinks.keySet();
    }

    /*
     * Gets outgoing PageNodes as a set
     */
    public Set<PageNode> getOutNodes() {
        return new HashSet<>(outLinks.values());
    }

    /*
     * Checks if this PageNode has a link to a specific URL
     */
    public boolean hasLink(String link) {
        return outLinks.containsKey(link);
    }

    /*
     * Overrides equals to compare URLs
     */
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