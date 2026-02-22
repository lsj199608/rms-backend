package rms.security.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "app.security.cors")
@Getter
@Setter
public class SecurityCorsProperties {
	private List<String> allowedOrigins = List.of(
		"http://localhost:*",
		"http://127.0.0.1:*",
		"https://localhost:*",
		"https://127.0.0.1:*"
	);
}
