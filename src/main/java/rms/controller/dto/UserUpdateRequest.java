package rms.controller.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
	@NotBlank(message = "username is required")
	String username,

	String password,

	Boolean enabled,
	Set<Long> roleIds
) {
}
