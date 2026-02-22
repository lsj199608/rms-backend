package rms.chat.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.CloseStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rms.chat.dto.ChatMessageDto;
import rms.chat.service.ChatService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

	private final ChatService chatService;
	private final ObjectMapper objectMapper;

	private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
	private final Map<String, Set<Long>> sessionRooms = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		log.info("WebSocket connected sessionId={}", session.getId());
		sessionRooms.put(session.getId(), ConcurrentHashMap.newKeySet());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		log.info("WebSocket disconnected sessionId={} code={} reason={}", session.getId(), status.getCode(), status.getReason());
		sessionRooms.remove(session.getId());
		roomSessions.values().forEach(sessions -> sessions.remove(session));
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		Long userId = getAttributeAsLong(session, "userId");
		if (userId == null) {
			sendError(session, "UNAUTHORIZED", "인증이 필요합니다.");
			return;
		}

		SocketPayload payload;
		try {
			payload = parsePayload(message.getPayload());
			switch (payload.type()) {
				case "join" -> joinRoom(session, userId, payload.roomId());
				case "leave" -> leaveRoom(session, payload.roomId());
				case "send" -> sendMessage(session, userId, payload.roomId(), payload.text());
				default -> sendError(session, "INVALID_REQUEST", "지원하지 않는 메시지입니다.");
			}
		} catch (IllegalArgumentException | IllegalStateException e) {
			sendError(session, "INVALID_REQUEST", e.getMessage());
		} catch (Exception e) {
			sendError(session, "SERVER_ERROR", "채팅 처리 중 오류가 발생했습니다.");
		}
	}

	private void joinRoom(WebSocketSession session, Long userId, Long roomId) throws IOException {
		if (roomId == null) {
			sendError(session, "INVALID_ROOM", "roomId가 필요합니다.");
			return;
		}

		chatService.validateRoomMembershipDto(roomId, userId);
		roomSessions.computeIfAbsent(roomId, room -> ConcurrentHashMap.newKeySet()).add(session);
		sessionRooms.computeIfAbsent(session.getId(), s -> ConcurrentHashMap.newKeySet()).add(roomId);

		ObjectNode joined = objectMapper.createObjectNode()
			.put("type", "joined")
			.put("roomId", roomId);
		session.sendMessage(new TextMessage(objectMapper.writeValueAsString(joined)));
	}

	private void leaveRoom(WebSocketSession session, Long roomId) throws IOException {
		if (roomId == null) {
			sendError(session, "INVALID_ROOM", "roomId가 필요합니다.");
			return;
		}

		Set<WebSocketSession> sessions = roomSessions.get(roomId);
		if (sessions != null) {
			sessions.remove(session);
		}
		Set<Long> joinedRooms = sessionRooms.get(session.getId());
		if (joinedRooms != null) {
			joinedRooms.remove(roomId);
		}
		ObjectNode left = objectMapper.createObjectNode()
			.put("type", "left")
			.put("roomId", roomId);
		session.sendMessage(new TextMessage(objectMapper.writeValueAsString(left)));
	}

	private void sendMessage(WebSocketSession session, Long userId, Long roomId, String text) throws IOException {
		if (roomId == null || text == null || text.trim().isEmpty()) {
			sendError(session, "INVALID_MESSAGE", "roomId와 message가 필요합니다.");
			return;
		}

		ChatMessageDto saved = chatService.sendMessage(roomId, userId, text);
		ObjectNode payload = objectMapper.createObjectNode()
			.put("type", "message")
			.put("roomId", saved.roomId())
			.put("messageId", saved.id())
			.put("senderId", saved.senderId())
			.put("senderName", saved.senderName())
			.put("content", saved.content())
			.put("createdAt", saved.createdAt());

		broadcast(roomId, payload);
	}

	private void broadcast(Long roomId, ObjectNode payload) throws JsonProcessingException {
		String text = objectMapper.writeValueAsString(payload);
		roomSessions.getOrDefault(roomId, Set.of()).forEach(session -> {
			if (session.isOpen()) {
				try {
					session.sendMessage(new TextMessage(text));
				} catch (IOException ignored) {
					// best effort
				}
			}
		});
	}

	private void sendError(WebSocketSession session, String code, String message) throws IOException {
		ObjectNode payload = objectMapper.createObjectNode()
			.put("type", "error")
			.put("code", code)
			.put("message", message);
		session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
	}

	private SocketPayload parsePayload(String source) throws JsonProcessingException {
		var node = objectMapper.readTree(source);
		String type = node.path("type").asText("");
		Long roomId = node.has("roomId") && node.get("roomId").isNumber() ? node.get("roomId").asLong() : null;
		String text = node.has("text") && node.get("text").isTextual() ? node.get("text").asText() : "";
		return new SocketPayload(type, roomId, text);
	}

	private Long getAttributeAsLong(WebSocketSession session, String key) {
		Object value = session.getAttributes().get(key);
		return value instanceof Long typedValue ? typedValue : null;
	}

	private record SocketPayload(String type, Long roomId, String text) {
	}
}
