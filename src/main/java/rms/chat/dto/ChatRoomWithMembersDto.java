package rms.chat.dto;

import java.util.List;

public record ChatRoomWithMembersDto(
	Long id,
	String name,
	int memberCount,
	String lastMessage,
	String lastMessageAt,
	List<ChatRoomMemberDto> members
) {
}
