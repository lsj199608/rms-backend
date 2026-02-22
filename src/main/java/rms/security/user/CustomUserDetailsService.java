package rms.security.user;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import rms.security.domain.AppUser;
import rms.security.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) {
		Optional<AppUser> found = userRepository.findWithRolesAndPermissionsByUsername(username);
		if (found.isEmpty()) {
			throw new UsernameNotFoundException("User not found: " + username);
		}
		return new CustomUserDetails(found.get());
	}
}
