package rms.dashboard.controller;

import rms.dashboard.entity.TestEntity;
import rms.dashboard.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

	private final TestService testService;

	@GetMapping
	public ResponseEntity<List<TestEntity>> list() {
		return ResponseEntity.ok(testService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<TestEntity> get(@PathVariable UUID id) {
		return ResponseEntity.ok(testService.findById(id));
	}

	@GetMapping("/code/{code}")
	public ResponseEntity<TestEntity> getByCode(@PathVariable String code) {
		return ResponseEntity.ok(testService.findByCode(code));
	}

	@PostMapping
	public ResponseEntity<TestEntity> create(@RequestBody TestEntity body) {
		TestEntity created = testService.create(body);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	public ResponseEntity<TestEntity> update(@PathVariable UUID id, @RequestBody TestEntity body) {
		body.setId(id);
		return ResponseEntity.ok(testService.update(body));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		testService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
