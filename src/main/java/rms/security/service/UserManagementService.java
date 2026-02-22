package rms.security.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import rms.controller.dto.RoleDto;
import rms.controller.dto.UserCreateRequest;
import rms.controller.dto.UserDto;
import rms.controller.dto.UserUpdateRequest;
import rms.security.domain.AppUser;
import rms.security.domain.Role;
import rms.security.repository.RoleRepository;
import rms.security.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserManagementService {

	private static final String DEFAULT_ROLE_NAME = "ROLE_USER";

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public List<UserDto> listUsers() {
		return userRepository.findAllWithRoles()
			.stream()
			.map(this::toDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<RoleDto> listRoles() {
		return roleRepository.findAll()
			.stream()
			.map(role -> new RoleDto(role.getId(), role.getRoleName()))
			.sorted(Comparator.comparing(RoleDto::roleName))
			.toList();
	}

	public UserDto createUser(UserCreateRequest request) {
		String username = request.username().trim();
		if (userRepository.existsByUsernameIgnoreCase(username)) {
			throw new IllegalStateException("이미 사용 중인 사용자명입니다.");
		}

		AppUser user = AppUser.builder()
			.username(username)
			.passwordHash(passwordEncoder.encode(request.password()))
			.enabled(Boolean.TRUE.equals(request.enabled()))
			.roles(resolveRolesOrDefault(request.roleIds()))
			.build();

		AppUser saved = userRepository.save(user);
		return toDto(saved);
	}

	public UserDto updateUser(Long userId, UserUpdateRequest request, Long actorUserId) {
		AppUser user = userRepository.findWithRolesById(userId)
			.orElseThrow(() -> new NoSuchElementException("수정할 사용자를 찾을 수 없습니다."));

		if (user.getId().equals(actorUserId)) {
			throw new IllegalStateException("현재 로그인한 계정은 수정할 수 없습니다.");
		}

		String username = request.username().trim();
		if (!user.getUsername().equals(username) && userRepository.existsByUsernameIgnoreCase(username)) {
			throw new IllegalStateException("이미 사용 중인 사용자명입니다.");
		}

		user.setUsername(username);

		if (request.password() != null && !request.password().isBlank()) {
			user.setPasswordHash(passwordEncoder.encode(request.password()));
		}

		if (request.enabled() != null) {
			user.setEnabled(request.enabled());
		}

		if (request.roleIds() != null) {
			user.setRoles(resolveRoles(request.roleIds()));
		}

		return toDto(user);
	}

	public void deleteUser(Long userId, Long actorUserId) {
		AppUser user = userRepository.findById(userId)
			.orElseThrow(() -> new NoSuchElementException("삭제할 사용자를 찾을 수 없습니다."));

		if (user.getId().equals(actorUserId)) {
			throw new IllegalStateException("현재 로그인한 계정은 삭제할 수 없습니다.");
		}

		userRepository.delete(user);
	}

	private Set<Role> resolveRolesOrDefault(Set<Long> roleIds) {
		Set<Long> resolvedIds = normalizeIds(roleIds);
		if (resolvedIds.isEmpty()) {
			Role defaultRole = roleRepository.findByRoleName(DEFAULT_ROLE_NAME)
				.orElseThrow(() -> new IllegalStateException("기본 역할이 존재하지 않습니다."));
			return new HashSet<>(Set.of(defaultRole));
		}
		return resolveRoles(resolvedIds);
	}

	private Set<Role> resolveRoles(Set<Long> roleIds) {
		Set<Long> resolvedIds = normalizeIds(roleIds);
		if (resolvedIds.isEmpty()) {
			return new HashSet<>();
		}

		List<Role> roles = roleRepository.findAllById(resolvedIds);
		if (roles.size() != resolvedIds.size()) {
			throw new IllegalArgumentException("유효하지 않은 역할이 포함되어 있습니다.");
		}

		return new HashSet<>(roles);
	}

	private Set<Long> normalizeIds(Set<Long> roleIds) {
		if (roleIds == null) {
			return new HashSet<>();
		}

		return roleIds.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toCollection(HashSet::new));
	}

	private UserDto toDto(AppUser user) {
		List<String> roleNames = user.getRoles().stream()
			.map(Role::getRoleName)
			.sorted()
			.toList();

		return new UserDto(user.getId(), user.getUsername(), user.isEnabled(), roleNames);
	}
}
