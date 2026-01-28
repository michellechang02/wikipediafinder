package com.wikipediafinder.backend.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;

/** Simple SQL-backed LinkCache using plain JDBC. Stores neighbor URL arrays as JSON text. */
public class SqlLinkCache implements LinkCache {
  private final DataSource ds;
  @SuppressWarnings("unused")
  private final Duration
      ttl; // ttl is not actively used here (could be used with a timestamp column)
  private final ObjectMapper mapper = new ObjectMapper();

  public SqlLinkCache(DataSource ds, Duration ttl) {
    this.ds = ds;
    this.ttl = ttl != null ? ttl : Duration.ofHours(24);
    // Ensure table exists
    try (Connection c = ds.getConnection();
        Statement s = c.createStatement()) {
      s.execute(
          "CREATE TABLE IF NOT EXISTS wiki_links (url TEXT PRIMARY KEY, neighbors TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
    } catch (SQLException ignored) {
    }
  }

  @Override
  public Set<String> get(String url) {
    String sql = "SELECT neighbors FROM wiki_links WHERE url = ?";
    try (Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, url);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        String json = rs.getString(1);
        if (json == null || json.isEmpty()) return null;
        String[] parts = mapper.readValue(json, String[].class);
        Set<String> res = new HashSet<>();
        Collections.addAll(res, parts);
        return Collections.unmodifiableSet(res);
      }
    } catch (SQLException | JsonProcessingException e) {
      return null;
    }
  }

  @Override
  public void put(String url, Set<String> neighbors) {
    if (url == null || neighbors == null) return;
    try (Connection c = ds.getConnection()) {
      String json = mapper.writeValueAsString(neighbors);
      String upsert =
          "INSERT INTO wiki_links(url, neighbors) VALUES (?, ?) ON CONFLICT (url) DO UPDATE SET neighbors = EXCLUDED.neighbors, created_at = CURRENT_TIMESTAMP";
      try (PreparedStatement ps = c.prepareStatement(upsert)) {
        ps.setString(1, url);
        ps.setString(2, json);
        ps.executeUpdate();
      }
    } catch (SQLException | JsonProcessingException ignored) {
    }
  }

  @PreDestroy
  public void close() {
    // DataSource managed by Spring; nothing to close here
  }
}
