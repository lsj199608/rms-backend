package rms.security.handler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletResponse;
import rms.security.exception.InvalidRefreshTokenException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AuthenticationServiceException.class)
	public void handleAuthService(AuthenticationServiceException ex, HttpServletResponse response) throws java.io.IOException {
		write(response, HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler(InvalidRefreshTokenException.class)
	public void handleInvalidRefreshToken(InvalidRefreshTokenException ex, HttpServletResponse response) throws java.io.IOException {
		write(response, HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public void handleBadRequest(MethodArgumentNotValidException ex, HttpServletResponse response) throws java.io.IOException {
		write(response, HttpStatus.BAD_REQUEST, "Validation failed");
	}

	private void write(HttpServletResponse response, HttpStatus status, String message) throws java.io.IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body));
	}
}
