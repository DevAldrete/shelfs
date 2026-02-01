package com.devaldrete.service;

import com.devaldrete.domain.User;
import com.devaldrete.dto.UserDTO;
import com.devaldrete.exception.ResourceNotFoundException;
import com.devaldrete.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<UserDTO> getAllUsers() {
    return userRepository.findAll().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  public UserDTO getUserById(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    return convertToDTO(user);
  }

  public UserDTO createUser(UserDTO userDTO) {
    if (userRepository.existsByUsername(userDTO.getUsername())) {
      throw new IllegalArgumentException("Username already exists");
    }
    if (userRepository.existsByEmail(userDTO.getEmail())) {
      throw new IllegalArgumentException("Email already exists");
    }

    User user = new User();
    user.setUsername(userDTO.getUsername());
    user.setEmail(userDTO.getEmail());
    user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

    User savedUser = userRepository.save(user);
    return convertToDTO(savedUser);
  }

  public UserDTO updateUser(Long id, UserDTO userDTO) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

    user.setUsername(userDTO.getUsername());
    user.setEmail(userDTO.getEmail());

    if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
      user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
    }

    User updatedUser = userRepository.save(user);
    return convertToDTO(updatedUser);
  }

  public void deleteUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    userRepository.delete(user);
  }

  private UserDTO convertToDTO(User user) {
    return new UserDTO(user.getId(), user.getUsername(), user.getEmail());
  }
}
