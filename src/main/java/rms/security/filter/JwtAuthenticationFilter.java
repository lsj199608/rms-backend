package rms.security.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import rms.security.config.JwtProperties;
import rms.security.jwt.JwtUtil;
import rms.security.user.CustomUserDetailsService;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final CustomUserDetailsService userDetailsService;
	private final JwtProperties jwtProperties;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String accessToken = getCookieValue(request, jwtProperties.getAccessTokenName());

		if (StringUtils.hasText(accessToken) && SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				String username = jwtUtil.extractUsername(accessToken);
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);
				if (jwtUtil.isTokenValid(accessToken, userDetails.getUsername())) {
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities()
					);
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			} catch (JwtException | IllegalArgumentException e) {
				SecurityContextHolder.clearContext();
			}
		}

		filterChain.doFilter(request, response);
	}

	private String getCookieValue(HttpServletRequest request, String tokenName) {
		if (request.getCookies() == null) {
			return null;
		}

		for (Cookie cookie : request.getCookies()) {
			if (tokenName.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
