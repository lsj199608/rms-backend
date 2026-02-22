package rms.security.controller;

import java.time.Duration;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import rms.security.config.JwtProperties;
import rms.security.domain.AppUser;
import rms.security.dto.AuthResponse;
import rms.security.dto.LoginRequest;
import rms.security.jwt.JwtUtil;
import rms.security.repository.UserRepository;
import rms.security.service.RefreshTokenService;
import rms.security.user.CustomUserDetails;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final JwtProperties jwtProperties;
	private final RefreshTokenService refreshTokenService;
	private final UserRepository userRepository;

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
		CustomUserDetails userDetails;
		try {
			Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.username(), request.password())
			);
			userDetails = (CustomUserDetails) authentication.getPrincipal();
		} catch (AuthenticationException e) {
			throw new AuthenticationServiceException("Invalid username or password");
		}

		AppUser user = userRepository.findByUsername(userDetails.getUsername())
			.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		String accessToken = jwtUtil.generateAccessToken(userDetails);
		String refreshToken = refreshTokenService.issue(user);

		setCookie(response, jwtProperties.getAccessTokenName(), accessToken, jwtProperties.getAccessTokenExpiration());
		setCookie(response, jwtProperties.getRefreshTokenName(), refreshToken, jwtProperties.getRefreshTokenExpiration());


		return ResponseEntity.ok(toResponse(userDetails, accessToken));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = getCookieValue(request, jwtProperties.getRefreshTokenName());
		AppUser user = refreshTokenService.validateAndRotate(refreshToken);
		CustomUserDetails userDetails = new CustomUserDetails(user);

		String accessToken = jwtUtil.generateAccessToken(userDetails);
		String newRefreshToken = refreshTokenService.issue(user);

		setCookie(response, jwtProperties.getAccessTokenName(), accessToken, jwtProperties.getAccessTokenExpiration());
		setCookie(response, jwtProperties.getRefreshTokenName(), newRefreshToken, jwtProperties.getRefreshTokenExpiration());
		return ResponseEntity.ok(toResponse(userDetails, accessToken));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
			refreshTokenService.logoutWithUser(userDetails.getUser());
		}

		String refreshToken = getCookieValue(request, jwtProperties.getRefreshTokenName());
		refreshTokenService.logoutWithRaw(refreshToken);

		clearCookie(response, jwtProperties.getAccessTokenName());
		clearCookie(response, jwtProperties.getRefreshTokenName());
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	public ResponseEntity<AuthResponse> me(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		String accessToken = jwtUtil.generateAccessToken(userDetails);
		return ResponseEntity.ok(toResponse(userDetails, accessToken));
	}

	private AuthResponse toResponse(CustomUserDetails userDetails, String accessToken) {
		return new AuthResponse(
			userDetails.getUserId(),
			userDetails.getUsername(),
			userDetails.getRoleNames(),
			userDetails.getPermissionNames(),
			accessToken
		);
	}

	private void setCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
		ResponseCookie cookie = ResponseCookie.from(name, value)
			.httpOnly(true)
			.secure(jwtProperties.isCookieSecure())
			.sameSite(jwtProperties.getCookieSameSite())
			.path("/")
			.maxAge(maxAge)
			.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}

	private void clearCookie(HttpServletResponse response, String name) {
		ResponseCookie cookie = ResponseCookie.from(name, "")
			.httpOnly(true)
			.secure(jwtProperties.isCookieSecure())
			.sameSite(jwtProperties.getCookieSameSite())
			.path("/")
			.maxAge(0)
			.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}

	private String getCookieValue(HttpServletRequest request, String name) {
		if (request.getCookies() == null) {
			return null;
		}
		for (Cookie cookie : request.getCookies()) {
			if (name.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
