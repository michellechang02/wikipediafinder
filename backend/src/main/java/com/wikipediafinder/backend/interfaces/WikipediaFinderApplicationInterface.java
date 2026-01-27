package com.wikipediafinder.backend.interfaces;

import org.springframework.web.filter.CorsFilter;

/** Public contract for application-level beans used by the app. */
public interface WikipediaFinderApplicationInterface {
  /** Exposes the CORS filter bean used by the application. */
  CorsFilter corsFilter();
}
