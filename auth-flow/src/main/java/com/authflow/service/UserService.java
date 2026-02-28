package com.authflow.service;

import com.authflow.exception.UserNotFoundException;
import com.authflow.model.User;
import com.authflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
	public User updateProfile(Long userId, String name) {
		User user = findById(userId);
		user.setName(name);
		return userRepository.save(user);
	}
}
