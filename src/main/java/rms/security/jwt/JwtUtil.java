package rms.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import rms.security.config.JwtProperties;
import rms.security.user.CustomUserDetails;

@Component
@RequiredArgsConstructor
public class JwtUtil {

	private final JwtProperties jwtProperties;

	public String generateAccessToken(CustomUserDetails userDetails) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(jwtProperties.getAccessTokenExpiration());

		return Jwts.builder()
			.issuer(jwtProperties.getIssuer())
			.subject(userDetails.getUsername())
			.claim("uid", userDetails.getUserId())
			.claim("roles", userDetails.getRoleNames())
			.claim("permissions", userDetails.getPermissionNames())
			.issuedAt(java.util.Date.from(now))
			.expiration(java.util.Date.from(expiresAt))
			.signWith(getSigningKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	public String generateRefreshToken() {
		return UUID.randomUUID().toString() + "." + UUID.randomUUID();
	}

	public String hashRefreshToken(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashed);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}

	public String extractUsername(String token) {
		return getClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, String username) {
		return extractUsername(token).equals(username) && !isExpired(token);
	}

	public boolean isExpired(String token) {
		return getClaims(token).getExpiration().toInstant().isBefore(Instant.now());
	}

	public Claims getClaims(String token) {
		try {
			return Jwts.parser()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (JwtException e) {
			throw e;
		}
	}

	public long remainingSeconds(Instant expiry) {
		return Math.max(0, expiry.getEpochSecond() - Instant.now().getEpochSecond());
	}

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	public Map<String, Object> getClaimsMap(String token) {
		return getClaims(token)
			.entrySet()
			.stream()
			.collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
