package rms.chat.dto;

public record ChatMessageDto(
	Long roomId,
	Long id,
	Long senderId,
	String senderName,
	String content,
	String createdAt
) {
}
