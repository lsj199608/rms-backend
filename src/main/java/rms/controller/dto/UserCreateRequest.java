package rms.controller.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
	@NotBlank(message = "username is required")
	String username,

	@NotBlank(message = "password is required")
	@Size(min = 6, message = "password must be at least 6 characters")
	String password,

	Boolean enabled,
	Set<Long> roleIds
) {
}
