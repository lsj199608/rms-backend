package rms.security.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import rms.security.domain.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {

	Optional<AppUser> findByUsername(String username);

	boolean existsByUsernameIgnoreCase(String username);

	@EntityGraph(attributePaths = {"roles", "roles.permissions"})
	Optional<AppUser> findWithRolesAndPermissionsByUsername(String username);

	@Query("select u from AppUser u")
	@EntityGraph(attributePaths = {"roles"})
	List<AppUser> findAllWithRoles();

	@Query("select u from AppUser u where u.id = :id")
	@EntityGraph(attributePaths = {"roles"})
	Optional<AppUser> findWithRolesById(Long id);
}
