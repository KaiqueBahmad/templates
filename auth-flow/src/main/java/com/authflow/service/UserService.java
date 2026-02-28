package com.authflow.service;

import com.authflow.exception.UserNotFoundException;
import com.authflow.model.User;
import com.authflow.model.enums.AuthProvider;
import com.authflow.model.enums.Role;
import com.authflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
	}

	public User findById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
	}

	public Optional<User> findByEmailOptional(String email) {
		return userRepository.findByEmail(email);
	}

	public User createUser(User user) {
		return userRepository.save(user);
	}

	public User updateUser(User user) {
		return userRepository.save(user);
	}

	public Boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Transactional
	public User processOAuth2User(OAuth2User oAuth2User, AuthProvider provider) {
		String email = oAuth2User.getAttribute("email");
		String name = oAuth2User.getAttribute("name");
		String providerId = oAuth2User.getAttribute("sub");

		Optional<User> userOptional = userRepository.findByEmail(email);

		if (userOptional.isPresent()) {
			User existingUser = userOptional.get();
			existingUser.setName(name);
			existingUser.setEmailVerified(true);
			return userRepository.save(existingUser);
		} else {
			User newUser = User.builder()
					.email(email)
					.name(name)
					.provider(provider)
					.providerId(providerId)
					.roles(Set.of(Role.USER))
					.emailVerified(true)
					.enabled(true)
					.password(null)
					.build();
			return userRepository.save(newUser);
		}
	}

	@Transactional
	public User updateProfile(Long userId, String name) {
		User user = findById(userId);
		user.setName(name);
		return userRepository.save(user);
	}
}
