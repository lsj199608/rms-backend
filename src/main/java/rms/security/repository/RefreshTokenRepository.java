package rms.security.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import rms.security.domain.AppUser;
import rms.security.domain.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

	@Transactional
	@Modifying
	@Query("""
		update RefreshToken r
		set r.revoked = true
		where r.user = :user and r.revoked = false
		""")
	void revokeActiveTokensByUser(@Param("user") AppUser user);

	Optional<RefreshToken> findByTokenHashAndRevokedFalseAndExpiresAtAfter(String tokenHash, Instant now);

	@Transactional
	@Modifying
	@Query("""
		update RefreshToken r
		set r.revoked = true
		where r.user = :user and r.tokenHash = :tokenHash and r.revoked = false
		""")
	void revokeByUserAndHash(@Param("user") AppUser user, @Param("tokenHash") String tokenHash);
}
