package com.wikipediafinder.backend;

import com.wikipediafinder.backend.interfaces.WikipediaFinderApplicationInterface;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/** Main Spring Boot application class for Wikipedia path finder. */
@SpringBootApplication
public class WikipediaFinderApplication implements WikipediaFinderApplicationInterface {

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(WikipediaFinderApplication.class, args);
  }

  /**
   * Cors filter cors filter.
   *
   * @return the cors filter
   */
  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("http://localhost:5173"); // Frontend origin
    config.addAllowedOrigin("https://wikipedia-path-finder.vercel.app"); // Second Frontend origin
    config.addAllowedHeader("*"); // Allow all headers
    config.addAllowedMethod("*"); // Allow all HTTP methods
    source.registerCorsConfiguration("/**", config); // Apply configuration to all endpoints
    return new CorsFilter(source); // Pass the source to CorsFilter
  }
}
