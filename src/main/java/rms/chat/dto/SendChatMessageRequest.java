package rms.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendChatMessageRequest(
	@NotBlank(message = "메시지는 비어 있을 수 없습니다.")
	@Size(min = 1, max = 2000, message = "메시지는 1~2000자 사이여야 합니다.")
	String content
) {
}
