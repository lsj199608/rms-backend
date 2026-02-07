package rms.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

	@GetMapping(value = {"", "/"})
	public ResponseEntity<Map<String, Object>> root() {
		return ResponseEntity.ok(Map.of(
			"application", "rms-backend",
			"status", "up",
			"docs", "/api/test"
		));
	}
}
