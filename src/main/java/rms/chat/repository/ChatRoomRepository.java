package rms.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import rms.chat.domain.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	@EntityGraph(attributePaths = {"members"})
	@Query("select distinct r from ChatRoom r left join r.members m where m.id = :userId")
	List<ChatRoom> findAllByMemberId(@Param("userId") Long userId);

	@EntityGraph(attributePaths = {"members"})
	@Query("select distinct r from ChatRoom r left join r.members m where r.id = :roomId")
	Optional<ChatRoom> findByIdWithMembers(@Param("roomId") Long roomId);
}
