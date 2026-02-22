package rms.chat.websocket;

import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import lombok.extern.slf4j.Slf4j;

import rms.security.config.JwtProperties;
import rms.security.jwt.JwtUtil;
import rms.security.user.CustomUserDetailsService;

import org.springframework.http.server.ServletServerHttpRequest;
import rms.security.user.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

	private final JwtUtil jwtUtil;
	private final CustomUserDetailsService userDetailsService;
	private final JwtProperties jwtProperties;

	@Override
	public boolean beforeHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Map<String, Object> attributes
	) {
		if (!(request instanceof ServletServerHttpRequest servletRequest)) {
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return false;
		}

		HttpServletRequest httpRequest = servletRequest.getServletRequest();
		String accessToken = getTokenFromRequest(httpRequest);
		log.info("Chat handshake request: uri={}, origin={}, hasToken={}", request.getURI(), httpRequest.getHeader("Origin"), accessToken != null);
		if (accessToken == null) {
			log.info("Chat handshake rejected: missing access token");
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return false;
		}

		try {
			UserDetails userDetails = userDetailsService.loadUserByUsername(jwtUtil.extractUsername(accessToken));
			if (!jwtUtil.isTokenValid(accessToken, userDetails.getUsername())) {
				log.info("Chat handshake rejected: token validation failed (expired or username mismatch)");
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				return false;
			}

			CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
			Long userId = customUserDetails.getUser().getId();
			String username = userDetails.getUsername();
			attributes.put("userId", userId);
			attributes.put("username", username);
			log.info("Chat handshake accepted for userId={} username={}", userId, username);
			return true;
		} catch (Exception e) {
			log.info("Chat handshake rejected: token validation error ({})", e.getMessage());
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return false;
		}
	}

	private String getTokenFromRequest(HttpServletRequest request) {
		String token = getCookieValue(request, jwtProperties.getAccessTokenName());
		if (token != null) {
			log.info("Chat handshake token source: cookie [{}]", jwtProperties.getAccessTokenName());
			return token;
		}

		String queryToken = request.getParameter(jwtProperties.getAccessTokenName());
		if (queryToken != null) {
			log.info("Chat handshake token source: query [{}]", jwtProperties.getAccessTokenName());
			return queryToken;
		}

		String rawToken = request.getParameter("token");
		log.info("Chat handshake token source: {}",
			rawToken == null ? "none" : "query [token]");
		return rawToken;
	}

	@Override
	public void afterHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Exception exception
	) {
	}

	private String getCookieValue(HttpServletRequest request, String tokenName) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}

		for (Cookie cookie : cookies) {
			if (tokenName.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
