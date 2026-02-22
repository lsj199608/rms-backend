package rms.security.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import rms.security.config.JwtProperties;
import rms.security.domain.AppUser;
import rms.security.domain.RefreshToken;
import rms.security.exception.InvalidRefreshTokenException;
import rms.security.jwt.JwtUtil;
import rms.security.repository.RefreshTokenRepository;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtProperties jwtProperties;
	private final JwtUtil jwtUtil;

	@Transactional
	public String issue(AppUser user) {
		refreshTokenRepository.revokeActiveTokensByUser(user);

		String raw = jwtUtil.generateRefreshToken();
		String hash = jwtUtil.hashRefreshToken(raw);
		refreshTokenRepository.save(RefreshToken.builder()
			.user(user)
			.tokenHash(hash)
			.expiresAt(Instant.now().plus(jwtProperties.getRefreshTokenExpiration()))
			.revoked(false)
			.build());
		return raw;
	}

	@Transactional
	public AppUser validateAndRotate(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			throw new InvalidRefreshTokenException("Missing refresh token");
		}

		String hash = jwtUtil.hashRefreshToken(rawRefreshToken);
		RefreshToken token = refreshTokenRepository.findByTokenHashAndRevokedFalseAndExpiresAtAfter(hash, Instant.now())
			.orElseThrow(() -> new InvalidRefreshTokenException("Invalid or expired refresh token"));
		if (!token.getUser().isEnabled()) {
			throw new InvalidRefreshTokenException("User is disabled");
		}

		AppUser user = token.getUser();
		refreshTokenRepository.revokeByUserAndHash(user, hash);
		refreshTokenRepository.revokeActiveTokensByUser(user);
		issue(user);
		return user;
	}

	@Transactional
	public void logoutWithUser(AppUser user) {
		refreshTokenRepository.revokeActiveTokensByUser(user);
	}

	@Transactional
	public void logoutWithRaw(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			return;
		}
		String hash = jwtUtil.hashRefreshToken(rawRefreshToken);
		refreshTokenRepository.findByTokenHashAndRevokedFalseAndExpiresAtAfter(hash, Instant.now())
			.ifPresent(token -> refreshTokenRepository.revokeByUserAndHash(token.getUser(), hash));
	}
}
