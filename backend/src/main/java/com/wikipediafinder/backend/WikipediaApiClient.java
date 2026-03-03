package com.wikipediafinder.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * Client for interacting with the Wikipedia API to fetch page links efficiently.
 *
 * <p>This client uses the Wikipedia REST API to fetch outgoing links from a page without
 * downloading and parsing the entire HTML content. This is significantly faster than using Jsoup to
 * scrape the page HTML.
 */
public class WikipediaApiClient {
  private static final String API_BASE_URL = "https://en.wikipedia.org/w/api.php";
  private static final HttpClient httpClient =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(10))
          .followRedirects(HttpClient.Redirect.NORMAL)
          .build();

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Fetches up to {@code limit} outgoing links from the given Wikipedia page title.
   *
   * @param pageTitle the title of the Wikipedia page (e.g., "Python_(programming_language)")
   * @param limit maximum number of links to retrieve
   * @return set of Wikipedia URLs for outgoing links
   * @throws IOException if the API request fails
   */
  public static Set<String> getOutgoingLinks(String pageTitle, int limit) throws IOException {
    Set<String> links = new HashSet<>();
    try {
      String encodedTitle = URLEncoder.encode(pageTitle, StandardCharsets.UTF_8);
      String apiUrl =
          String.format(
              "%s?action=query&titles=%s&prop=links&pllimit=%d&plnamespace=0&format=json",
              API_BASE_URL, encodedTitle, limit);

      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        throw new IOException("Wikipedia API returned status code: " + response.statusCode());
      }

      JsonNode root = objectMapper.readTree(response.body());
      JsonNode pages = root.path("query").path("pages");

      if (pages.isMissingNode()) {
        return links;
      }

      // Wikipedia API returns pages as an object with page IDs as keys
      pages
          .fields()
          .forEachRemaining(
              entry -> {
                JsonNode pageNode = entry.getValue();
                JsonNode linksArray = pageNode.path("links");
                if (!linksArray.isMissingNode() && linksArray.isArray()) {
                  linksArray.forEach(
                      linkNode -> {
                        String title = linkNode.path("title").asText();
                        if (!title.isEmpty()) {
                          // Convert title to Wikipedia URL format
                          String url = titleToUrl(title);
                          links.add(url);
                        }
                      });
                }
              });

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }

    return links;
  }

  /**
   * Converts a Wikipedia page title to a full URL.
   *
   * @param title the page title (e.g., "Python (programming language)")
   * @return the full Wikipedia URL
   */
  private static String titleToUrl(String title) {
    // Replace spaces with underscores and encode special characters
    String formattedTitle = title.replace(" ", "_");
    try {
      formattedTitle = URLEncoder.encode(formattedTitle, StandardCharsets.UTF_8);
      // Decode certain characters that Wikipedia URLs use literally
      formattedTitle = formattedTitle.replace("%28", "(").replace("%29", ")");
    } catch (Exception e) {
      // Fallback: just replace spaces
      formattedTitle = title.replace(" ", "_");
    }
    return "https://en.wikipedia.org/wiki/" + formattedTitle;
  }

  /**
   * Extracts the page title from a Wikipedia URL.
   *
   * @param url the full Wikipedia URL
   * @return the page title, or null if the URL is invalid
   */
  public static String urlToTitle(String url) {
    if (url == null || !url.contains("/wiki/")) {
      return null;
    }
    String[] parts = url.split("/wiki/");
    if (parts.length < 2) {
      return null;
    }
    String encodedTitle = parts[1];
    // Remove any fragment or query parameters
    int hashIndex = encodedTitle.indexOf('#');
    if (hashIndex != -1) {
      encodedTitle = encodedTitle.substring(0, hashIndex);
    }
    int queryIndex = encodedTitle.indexOf('?');
    if (queryIndex != -1) {
      encodedTitle = encodedTitle.substring(0, queryIndex);
    }
    // Replace underscores with spaces
    return encodedTitle.replace("_", " ");
  }

  /**
   * Checks if a Wikipedia page exists by making a lightweight API call.
   *
   * @param pageTitle the title of the page to check
   * @return true if the page exists, false otherwise
   */
  public static boolean pageExists(String pageTitle) {
    try {
      String encodedTitle = URLEncoder.encode(pageTitle, StandardCharsets.UTF_8);
      String apiUrl =
          String.format("%s?action=query&titles=%s&format=json", API_BASE_URL, encodedTitle);

      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        return false;
      }

      JsonNode root = objectMapper.readTree(response.body());
      JsonNode pages = root.path("query").path("pages");

      // If the page doesn't exist, it will have a negative page ID and "missing" field
      for (JsonNode page : pages) {
        if (page.has("missing")) {
          return false;
        }
      }
      return true;

    } catch (Exception e) {
      return false;
    }
  }
}
