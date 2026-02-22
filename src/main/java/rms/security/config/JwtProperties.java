package rms.security.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "app.security.jwt")
@Getter
@Setter
public class JwtProperties {
	private String secret;
	private Duration accessTokenExpiration = Duration.ofMinutes(15);
	private Duration refreshTokenExpiration = Duration.ofDays(30);
	private String accessTokenName = "access_token";
	private String refreshTokenName = "refresh_token";
	private boolean cookieSecure = false;
	private String cookieSameSite = "Lax";
	private String issuer = "rms-backend";
}
