package rms.security.user;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;
import rms.security.domain.AppUser;
import rms.security.domain.Permission;

@AllArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {

	private final AppUser user;

	@Override
	public List<GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> authorities = new LinkedHashSet<>();

		user.getRoles().forEach(role -> {
			String roleName = role.getRoleName();
			authorities.add(new SimpleGrantedAuthority(roleName));
			for (Permission permission : role.getPermissions()) {
				authorities.add(new SimpleGrantedAuthority(permission.getPermName()));
			}
		});

		return new ArrayList<>(authorities);
	}

	public Long getUserId() {
		return user.getId();
	}

	@Override
	public String getPassword() {
		return user.getPasswordHash();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	public List<String> getRoleNames() {
		return user.getRoles().stream()
			.map(role -> role.getRoleName())
			.distinct()
			.toList();
	}

	public List<String> getPermissionNames() {
		return user.getRoles().stream()
			.flatMap(role -> role.getPermissions().stream())
			.map(permission -> permission.getPermName())
			.distinct()
			.toList();
	}
}
