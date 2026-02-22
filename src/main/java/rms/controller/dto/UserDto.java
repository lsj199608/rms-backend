package rms.controller.dto;

import java.util.List;

public record UserDto(
	Long id,
	String username,
	boolean enabled,
	List<String> roles
) {
}
