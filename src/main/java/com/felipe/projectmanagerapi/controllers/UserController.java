package com.felipe.projectmanagerapi.controllers;

import com.felipe.projectmanagerapi.dtos.UserRegisterDTO;
import com.felipe.projectmanagerapi.dtos.UserResponseDTO;
import com.felipe.projectmanagerapi.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/auth/test")
  public String test() {
    return "Caiu aqui, CORNO";
  }

  @PostMapping("/auth/register")
  public ResponseEntity<UserResponseDTO> register(@RequestBody UserRegisterDTO body) {
    UserResponseDTO response = this.userService.register(body);
    return ResponseEntity.ok().body(response);
  }
}
