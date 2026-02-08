package com.devaldrete.controller;

import com.devaldrete.dto.UserDTO;
import com.devaldrete.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<List<UserDTO>> getAllUsers() {
    List<UserDTO> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
    UserDTO user = userService.getUserById(id);
    return ResponseEntity.ok(user);
  }

  /**
   * Get a user by their email address.
   * Primary workflow for library staff looking up members.
   *
   * @param email the email address to search for
   * @return the user
   */
  @GetMapping("/email/{email}")
  public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
    UserDTO user = userService.getUserByEmail(email);
    return ResponseEntity.ok(user);
  }

  /**
   * Get a user by their username.
   *
   * @param username the username to search for
   * @return the user
   */
  @GetMapping("/username/{username}")
  public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
    UserDTO user = userService.getUserByUsername(username);
    return ResponseEntity.ok(user);
  }

  @PostMapping
  public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
    UserDTO createdUser = userService.createUser(userDTO);
    return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserDTO> updateUser(
      @PathVariable Long id,
      @Valid @RequestBody UserDTO userDTO) {
    UserDTO updatedUser = userService.updateUser(id, userDTO);
    return ResponseEntity.ok(updatedUser);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
