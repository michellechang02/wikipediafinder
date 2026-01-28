package com.wikipediafinder.backend.config;

import com.wikipediafinder.backend.cache.InMemoryLinkCache;
import com.wikipediafinder.backend.cache.LinkCache;
import com.wikipediafinder.backend.cache.SqlLinkCache;
import java.net.URI;
import java.time.Duration;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
  @Bean
  public LinkCache linkCache() {
    String dbUrl = System.getenv("DATABASE_URL");
    if (dbUrl != null && !dbUrl.isEmpty()) {
      try {
        // Parse DATABASE_URL of form: postgres://user:pass@host:port/dbname
        URI uri = new URI(dbUrl);
        String userInfo = uri.getUserInfo();
        String username = null;
        String password = null;
        if (userInfo != null && userInfo.contains(":")) {
          String[] parts = userInfo.split(":", 2);
          username = parts[0];
          password = parts[1];
        }
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();
        DataSource ds =
            DataSourceBuilder.create().url(jdbcUrl).username(username).password(password).build();
        return new SqlLinkCache(ds, Duration.ofHours(24));
      } catch (Exception e) {
        // Fall back to in-memory if parsing fails
        return new InMemoryLinkCache();
      }
    }
    return new InMemoryLinkCache();
  }
}
