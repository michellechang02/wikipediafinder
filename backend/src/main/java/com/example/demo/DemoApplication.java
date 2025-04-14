package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


import java.util.List;
import java.util.Map;


@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("http://localhost:5173"); // Frontend origin
		config.addAllowedHeader("*"); // Allow all headers
		config.addAllowedMethod("*"); // Allow all HTTP methods
		source.registerCorsConfiguration("/**", config); // Apply configuration to all endpoints
		return new CorsFilter(source); // Pass the source to CorsFilter
	}

}

@RestController
@RequestMapping("/api")
class MyController {

	@GetMapping("/hello")
	public ResponseEntity<String> hello() {
		return ResponseEntity.ok("Hello, World!");
	}

	@CrossOrigin(origins = "http://localhost:5173")
	@GetMapping("/getResults")
	public ResponseEntity<Object> getResults(
			@RequestParam String startinglink,
			@RequestParam String endinglink) {
		try {
			// Perform BFS to get the results
			List<String> results = BFS.getPath(new PageNode(startinglink), new PageNode(endinglink));

			if (results == null) {
				return new ResponseEntity<>(Map.of("message", "No path found or query took too long"), HttpStatus.OK);
			}

			return new ResponseEntity<>(results, HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}
}