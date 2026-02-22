package rms.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import rms.chat.domain.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);

	Optional<ChatMessage> findTopByRoomIdOrderByCreatedAtDesc(Long roomId);
}
