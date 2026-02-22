package rms.chat.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import rms.chat.dto.ChatMessageDto;
import rms.chat.dto.ChatRoomDto;
import rms.chat.dto.ChatRoomWithMembersDto;
import rms.chat.dto.CreateChatRoomRequest;
import rms.chat.dto.ModifyChatRoomMembersRequest;
import rms.chat.dto.SendChatMessageRequest;
import rms.chat.service.ChatService;
import rms.security.user.CustomUserDetails;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	@GetMapping("/rooms")
	@PreAuthorize("hasAuthority('USER_READ')")
	public List<ChatRoomDto> listRooms(@AuthenticationPrincipal CustomUserDetails me) {
		return chatService.listRooms(me.getUserId());
	}

	@PostMapping("/rooms")
	@PreAuthorize("hasAuthority('USER_READ')")
	public ResponseEntity<?> createRoom(
		@Valid @RequestBody CreateChatRoomRequest request,
		@AuthenticationPrincipal CustomUserDetails me
	) {
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createRoom(request, me.getUserId()));
		} catch (NoSuchElementException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "이미 존재하는 채팅방 이름입니다."));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
		}
	}

	@GetMapping("/rooms/{roomId}/messages")
	@PreAuthorize("hasAuthority('USER_READ')")
	public ResponseEntity<?> listMessages(
		@PathVariable Long roomId,
		@AuthenticationPrincipal CustomUserDetails me
	) {
		try {
			List<ChatMessageDto> messages = chatService.listMessages(roomId, me.getUserId());
			return ResponseEntity.ok(messages);
		} catch (NoSuchElementException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
		}
	}

	@GetMapping("/rooms/{roomId}/members")
	@PreAuthorize("hasAuthority('USER_READ')")
	public ResponseEntity<?> getMembers(
		@PathVariable Long roomId,
		@AuthenticationPrincipal CustomUserDetails me
	) {
		try {
			ChatRoomWithMembersDto members = chatService.getRoomMembers(roomId, me.getUserId());
			return ResponseEntity.ok(members);
		} catch (NoSuchElementException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
		}
	}

	@PatchMapping("/rooms/{roomId}/members")
	@PreAuthorize("hasAuthority('USER_READ')")
	public ResponseEntity<?> addMembers(
		@PathVariable Long roomId,
		@Valid @RequestBody ModifyChatRoomMembersRequest request,
		@AuthenticationPrincipal CustomUserDetails me
	) {
		try {
			return ResponseEntity.ok(chatService.addMembers(roomId, me.getUserId(), request));
		} catch (NoSuchElementException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
		}
	}

	@DeleteMapping("/rooms/{roomId}/members/{memberId}")
	@PreAuthorize("hasAuthority('USER_READ')")
	public ResponseEntity<?> removeMember(
		@PathVariable Long roomId,
		@PathVariable Long memberId,
		@AuthenticationPrincipal CustomUserDetails me
	) {
		try {
			return ResponseEntity.ok(chatService.removeMember(roomId, me.getUserId(), memberId));
		} catch (NoSuchElementException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
		}
	}

	@PostMapping("/rooms/{roomId}/messages")
	@PreAuthorize("hasAuthority('USER_READ')")
	public ResponseEntity<?> sendMessage(
		@PathVariable Long roomId,
		@Valid @RequestBody SendChatMessageRequest request,
		@AuthenticationPrincipal CustomUserDetails me
	) {
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(
				chatService.sendMessage(roomId, me.getUserId(), request.content())
			);
		} catch (NoSuchElementException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
		}
	}

}
