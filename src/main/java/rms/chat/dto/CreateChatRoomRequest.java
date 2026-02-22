package rms.chat.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateChatRoomRequest(
	@NotBlank(message = "방 이름은 필수입니다.")
	String name,

	Set<Long> memberIds
) {
}
