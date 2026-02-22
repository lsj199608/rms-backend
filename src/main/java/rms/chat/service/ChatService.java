package rms.chat.service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import rms.chat.domain.ChatMessage;
import rms.chat.domain.ChatRoom;
import rms.chat.dto.ChatMessageDto;
import rms.chat.dto.ChatRoomDto;
import rms.chat.dto.ChatRoomMemberDto;
import rms.chat.dto.ChatRoomWithMembersDto;
import rms.chat.dto.CreateChatRoomRequest;
import rms.chat.dto.ModifyChatRoomMembersRequest;
import rms.chat.repository.ChatMessageRepository;
import rms.chat.repository.ChatRoomRepository;
import rms.security.domain.AppUser;
import rms.security.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

	private static final int MAX_ROOM_NAME_LENGTH = 120;
	private static final int MAX_MESSAGE_LENGTH = 2000;
	private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_INSTANT;

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<ChatRoomDto> listRooms(Long actorUserId) {
		return chatRoomRepository.findAllByMemberId(actorUserId)
			.stream()
			.map(this::toRoomDto)
			.sorted(
				Comparator
					.comparing(
						(ChatRoomDto room) -> room.lastMessageAt().isBlank() ? "1970-01-01T00:00:00Z" : room.lastMessageAt()
					)
					.reversed()
					.thenComparing(ChatRoomDto::name)
			)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<ChatMessageDto> listMessages(Long roomId, Long actorUserId) {
		validateRoomMembership(roomId, actorUserId);
		return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId)
			.stream()
			.map(this::toMessageDto)
			.toList();
	}

	public ChatRoomDto createRoom(CreateChatRoomRequest request, Long actorUserId) {
		String roomName = request.name().trim();
		if (roomName.length() > MAX_ROOM_NAME_LENGTH) {
			throw new IllegalArgumentException("채팅방 이름은 120자 이하여야 합니다.");
		}

		Set<Long> requestedMemberIds = normalizeMemberIds(request.memberIds());
		requestedMemberIds.add(actorUserId);
		if (requestedMemberIds.isEmpty()) {
			throw new IllegalArgumentException("최소 1명의 참여자가 필요합니다.");
		}

		Set<AppUser> members = resolveMembers(requestedMemberIds);

		ChatRoom room = ChatRoom.builder()
			.name(roomName)
			.members(members)
			.build();

		ChatRoom saved = chatRoomRepository.save(room);
		return toRoomDto(saved);
	}

	public ChatMessageDto sendMessage(Long roomId, Long actorUserId, String content) {
		if (content == null || content.trim().isBlank()) {
			throw new IllegalArgumentException("메시지는 비어 있을 수 없습니다.");
		}
		if (content.trim().length() > MAX_MESSAGE_LENGTH) {
			throw new IllegalArgumentException("메시지는 1~2000자 사이여야 합니다.");
		}

		ChatRoom room = validateRoomMembership(roomId, actorUserId);
		AppUser actor = userRepository.findById(actorUserId)
			.orElseThrow(() -> new NoSuchElementException("발신자 계정을 찾을 수 없습니다."));

		ChatMessage message = ChatMessage.builder()
			.room(room)
			.sender(actor)
			.content(content.trim())
			.build();

		ChatMessage saved = chatMessageRepository.save(message);
		return toMessageDto(saved);
	}

	public ChatRoomDto validateRoomMembershipDto(Long roomId, Long actorUserId) {
		ChatRoom room = validateRoomMembership(roomId, actorUserId);
		return toRoomDto(room);
	}

	public ChatRoomWithMembersDto getRoomMembers(Long roomId, Long actorUserId) {
		ChatRoom room = validateRoomMembership(roomId, actorUserId);
		return toRoomWithMembersDto(room);
	}

	public ChatRoomDto addMembers(Long roomId, Long actorUserId, ModifyChatRoomMembersRequest request) {
		ChatRoom room = validateRoomMembership(roomId, actorUserId);
		Set<Long> requestedMemberIds = normalizeMemberIds(request.memberIds());
		if (requestedMemberIds.isEmpty()) {
			throw new IllegalArgumentException("추가할 멤버가 없습니다.");
		}

		Set<AppUser> requestedMembers = resolveMembers(requestedMemberIds);
		requestedMembers.forEach(room.getMembers()::add);
		ChatRoom saved = chatRoomRepository.save(room);
		return toRoomDto(saved);
	}

	public ChatRoomDto removeMember(Long roomId, Long actorUserId, Long memberId) {
		ChatRoom room = validateRoomMembership(roomId, actorUserId);
		if (memberId == null) {
			throw new IllegalArgumentException("제거할 멤버가 필요합니다.");
		}
		if (actorUserId.equals(memberId) && room.getMembers().size() <= 1) {
			throw new IllegalStateException("마지막 멤버는 채팅방을 나갈 수 없습니다.");
		}

		boolean removed = room.getMembers().removeIf(user -> memberId.equals(user.getId()));
		if (!removed) {
			throw new IllegalArgumentException("채팅방에 존재하지 않는 사용자입니다.");
		}

		if (room.getMembers().isEmpty()) {
			throw new IllegalStateException("채팅방에는 최소 1명이 필요합니다.");
		}

		ChatRoom saved = chatRoomRepository.save(room);
		return toRoomDto(saved);
	}

	private ChatRoom validateRoomMembership(Long roomId, Long actorUserId) {
		ChatRoom room = chatRoomRepository.findByIdWithMembers(roomId)
			.orElseThrow(() -> new NoSuchElementException("채팅방을 찾을 수 없습니다."));

		boolean isMember = room.getMembers().stream().anyMatch(user -> user.getId().equals(actorUserId));
		if (!isMember) {
			throw new IllegalStateException("참여하지 않은 채팅방입니다.");
		}

		return room;
	}

	private Set<AppUser> resolveMembers(Set<Long> memberIds) {
		Set<AppUser> members = new HashSet<>(userRepository.findAllById(memberIds));
		if (members.size() != memberIds.size()) {
			throw new IllegalArgumentException("유효하지 않은 사용자 계정이 포함되어 있습니다.");
		}
		return members;
	}

	private Set<Long> normalizeMemberIds(Set<Long> memberIds) {
		if (memberIds == null) {
			return new HashSet<>();
		}
		return memberIds.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toCollection(HashSet::new));
	}

	private ChatRoomDto toRoomDto(ChatRoom room) {
		ChatMessage lastMessage = chatMessageRepository.findTopByRoomIdOrderByCreatedAtDesc(room.getId())
			.orElse(null);

		return new ChatRoomDto(
			room.getId(),
			room.getName(),
			room.getMembers().size(),
			lastMessage == null ? "" : lastMessage.getContent(),
			lastMessage == null ? "" : ISO_FORMAT.format(lastMessage.getCreatedAt())
		);
	}

	private ChatMessageDto toMessageDto(ChatMessage message) {
		return new ChatMessageDto(
			message.getRoom().getId(),
			message.getId(),
			message.getSender().getId(),
			message.getSender().getUsername(),
			message.getContent(),
			ISO_FORMAT.format(message.getCreatedAt())
		);
	}

	private ChatRoomWithMembersDto toRoomWithMembersDto(ChatRoom room) {
		List<ChatRoomMemberDto> members = room.getMembers()
			.stream()
			.sorted(java.util.Comparator.comparing(AppUser::getUsername))
			.map(member -> new ChatRoomMemberDto(member.getId(), member.getUsername()))
			.toList();

		ChatMessage lastMessage = chatMessageRepository.findTopByRoomIdOrderByCreatedAtDesc(room.getId())
			.orElse(null);

		return new ChatRoomWithMembersDto(
			room.getId(),
			room.getName(),
			room.getMembers().size(),
			lastMessage == null ? "" : lastMessage.getContent(),
			lastMessage == null ? "" : ISO_FORMAT.format(lastMessage.getCreatedAt()),
			members
		);
	}
}
