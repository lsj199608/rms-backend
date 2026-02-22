package rms.chat.dto;

public record ChatRoomDto(
	Long id,
	String name,
	int memberCount,
	String lastMessage,
	String lastMessageAt
) {
}
