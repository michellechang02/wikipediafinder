package com.wikipediafinder.backend.logging;

import com.wikipediafinder.backend.BFSResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Centralized logger for the Wikipedia path finder backend. Handles logging for API endpoints and
 * other backend operations with consistent formatting and latency tracking.
 */
@Component
public class BackendLogger {

  private static final Logger logger = LoggerFactory.getLogger(BackendLogger.class);

  /** Logs the start of a health check endpoint call. */
  public void logHealthCheckStart() {
    logger.info("GET /api/health started");
  }

  /**
   * Logs the completion of a health check endpoint call with latency.
   *
   * @param latencyMs the latency in milliseconds
   */
  public void logHealthCheckComplete(long latencyMs) {
    logger.info("GET /api/health completed in {}ms", latencyMs);
  }

  /**
   * Logs the start of a getResults endpoint call with sanitized parameters.
   *
   * @param startinglink the starting Wikipedia link (will be sanitized)
   * @param endinglink the ending Wikipedia link (will be sanitized)
   */
  public void logGetResultsStart(String startinglink, String endinglink) {
    String safeStart = sanitizeForLogging(startinglink);
    String safeEnd = sanitizeForLogging(endinglink);
    logger.info(
        "GET /api/getResults called with startinglink={} endinglink={}", safeStart, safeEnd);
  }

  /**
   * Logs the successful completion of a getResults endpoint call with path details.
   *
   * @param latencyMs the latency in milliseconds
   * @param result the BFSResult containing path and nodes explored
   */
  public void logGetResultsSuccess(long latencyMs, BFSResult result) {
    logger.info(
        "GET /api/getResults completed in {}ms - path length={} nodesExplored={}",
        latencyMs,
        result.getPath().size(),
        result.getNodesExplored());
  }

  /**
   * Logs when a getResults endpoint call completes with no path found.
   *
   * @param latencyMs the latency in milliseconds
   */
  public void logGetResultsNoPath(long latencyMs) {
    logger.info("GET /api/getResults completed in {}ms - no path found", latencyMs);
  }

  /**
   * Logs when a getResults endpoint call fails with a bad request error.
   *
   * @param latencyMs the latency in milliseconds
   * @param errorMessage the error message
   */
  public void logGetResultsError(long latencyMs, String errorMessage) {
    String safeErrorMessage = sanitizeForLogging(errorMessage);
    logger.info(
        "GET /api/getResults completed in {}ms - bad request: {}", latencyMs, safeErrorMessage);
  }

  /**
   * Sanitizes a string for logging by replacing newline and carriage return characters to prevent
   * log injection attacks.
   *
   * @param input the input string to sanitize
   * @return the sanitized string
   */
  private String sanitizeForLogging(String input) {
    if (input == null) {
      return "null";
    }
    return input.replaceAll("[\r\n]", "_");
  }
}
