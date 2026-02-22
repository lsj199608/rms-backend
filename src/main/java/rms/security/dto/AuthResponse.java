package rms.security.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
	Long userId,
	String username,
	List<String> roles,
	List<String> permissions,
	String accessToken
) {
	@JsonProperty("id")
	public Long id() {
		return userId;
	}
}
