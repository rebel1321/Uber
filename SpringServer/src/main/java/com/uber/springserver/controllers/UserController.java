package com.uber.springserver.controllers;

import com.uber.springserver.config.RequestContext;
import com.uber.springserver.dto.auth.RefreshTokenRequest;
import com.uber.springserver.dto.auth.UserLoginRequest;
import com.uber.springserver.dto.auth.UserRegisterRequest;
import com.uber.springserver.models.User;
import com.uber.springserver.services.BlacklistService;
import com.uber.springserver.services.UserService;
import com.uber.springserver.utils.JwtUtil;
import com.uber.springserver.utils.PasswordUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;
    private final BlacklistService blacklistService;

    public UserController(UserService userService,
                          PasswordUtil passwordUtil,
                          JwtUtil jwtUtil,
                          BlacklistService blacklistService) {
        this.userService = userService;
        this.passwordUtil = passwordUtil;
        this.jwtUtil = jwtUtil;
        this.blacklistService = blacklistService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody UserRegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userService.findByEmail(email).isPresent()) {
            return Map.of("message", "User already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(email);
        user.setPassword(passwordUtil.hash(request.getPassword()));
        user = userService.create(user);

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        userService.updateRefreshToken(user.getId(), refreshToken);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("user", user);
        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody UserLoginRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase(Locale.ROOT);

        User user = userService.findByEmail(email).orElse(null);
        if (user == null || !passwordUtil.matches(request.getPassword(), user.getPassword())) {
            return Map.of("message", "Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        userService.updateRefreshToken(user.getId(), refreshToken);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("user", user);
        return response;
    }

    @PostMapping("/refresh")
    public Map<String, Object> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            Claims claims = jwtUtil.parseRefreshToken(request.getRefreshToken());
            String userId = claims.get("_id", String.class);

            User user = userService.findById(userId).orElse(null);
            if (user == null || user.getRefreshToken() == null || !user.getRefreshToken().equals(request.getRefreshToken())) {
                return Map.of("message", "Token mismatch");
            }

            String newAccessToken = jwtUtil.generateAccessToken(userId);
            return Map.of("accessToken", newAccessToken);
        } catch (Exception ex) {
            return Map.of("message", "Invalid token");
        }
    }

    @GetMapping("/profile")
    public Map<String, Object> profile(HttpServletRequest request) {
        User user = RequestContext.getUser(request);
        if (user == null) {
            return Map.of("message", "User not found");
        }
        return Map.of("user", user, "message", "Profile fetched successfully");
    }

    @GetMapping("/logout")
    public Map<String, Object> logout(@RequestHeader(name = "Authorization", required = false) String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return Map.of("message", "Token missing");
        }

        String[] parts = authorization.split(" ");
        if (parts.length != 2) {
            return Map.of("message", "Invalid token format");
        }

        blacklistService.add(parts[1]);
        return Map.of("message", "User logged out successfully");
    }
}
