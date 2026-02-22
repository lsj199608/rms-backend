package rms.chat.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import lombok.RequiredArgsConstructor;
import rms.security.config.SecurityCorsProperties;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class ChatWebSocketConfig implements WebSocketConfigurer {

	private final ChatWebSocketHandler chatWebSocketHandler;
	private final ChatHandshakeInterceptor chatHandshakeInterceptor;
	private final SecurityCorsProperties securityCorsProperties;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		String[] allowedOriginPatterns = securityCorsProperties.getAllowedOrigins().toArray(new String[0]);
		if (allowedOriginPatterns.length == 0) {
			allowedOriginPatterns = new String[] { "*" };
		}

		registry.addHandler(chatWebSocketHandler, "/ws/chat")
			.setAllowedOriginPatterns(allowedOriginPatterns)
			.addInterceptors(chatHandshakeInterceptor);
	}
}
