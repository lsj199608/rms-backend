package rms.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import rms.controller.dto.RoleDto;
import rms.controller.dto.UserCreateRequest;
import rms.controller.dto.UserDto;
import rms.controller.dto.UserUpdateRequest;
import rms.security.service.UserManagementService;
import rms.security.user.CustomUserDetails;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

	private final UserManagementService userManagementService;

	@GetMapping
	@PreAuthorize("hasAuthority('USER_READ')")
	public List<UserDto> listUsers() {
		return userManagementService.listUsers();
	}

	@GetMapping("/roles")
	@PreAuthorize("hasAuthority('USER_READ')")
	public List<RoleDto> listRoles() {
		return userManagementService.listRoles();
	}

	@PostMapping
	@PreAuthorize("hasAuthority('USER_WRITE')")
	public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateRequest request) {
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(userManagementService.createUser(request));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
		}
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('USER_WRITE')")
	public ResponseEntity<?> updateUser(
		@PathVariable Long id,
		@Valid @RequestBody UserUpdateRequest request,
		@AuthenticationPrincipal CustomUserDetails me
	) {
		try {
			return ResponseEntity.ok(userManagementService.updateUser(id, request, me.getUserId()));
		} catch (NoSuchElementException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('USER_WRITE')")
	public ResponseEntity<?> deleteUser(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails me) {
		try {
			userManagementService.deleteUser(id, me.getUserId());
			return ResponseEntity.noContent().build();
		} catch (NoSuchElementException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
		}
	}
}
