package rms.chat.dto;

import java.util.Set;

import jakarta.validation.constraints.NotNull;

public record ModifyChatRoomMembersRequest(
	@NotNull(message = "멤버는 null일 수 없습니다.")
	Set<Long> memberIds
) {
}
