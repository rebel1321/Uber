package com.uber.springserver.controllers;

import com.uber.springserver.config.RequestContext;
import com.uber.springserver.dto.auth.CaptainLoginRequest;
import com.uber.springserver.dto.auth.CaptainRegisterRequest;
import com.uber.springserver.dto.auth.RefreshTokenRequest;
import com.uber.springserver.models.Captain;
import com.uber.springserver.services.BlacklistService;
import com.uber.springserver.services.CaptainService;
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
@RequestMapping("/api/captain")
public class CaptainController {

    private final CaptainService captainService;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;
    private final BlacklistService blacklistService;

    public CaptainController(CaptainService captainService,
                             PasswordUtil passwordUtil,
                             JwtUtil jwtUtil,
                             BlacklistService blacklistService) {
        this.captainService = captainService;
        this.passwordUtil = passwordUtil;
        this.jwtUtil = jwtUtil;
        this.blacklistService = blacklistService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody CaptainRegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (captainService.findByEmail(email).isPresent()) {
            return Map.of("message", "Captain already exists");
        }

        Captain captain = new Captain();
        captain.setFullName(request.getFullName());
        captain.setEmail(email);
        captain.setPassword(passwordUtil.hash(request.getPassword()));
        captain.setVehicle(request.getVehicle());
        captain.setLocation(request.getLocation());
        captain.setStatus("inactive");
        captain = captainService.create(captain);

        String accessToken = jwtUtil.generateAccessToken(captain.getId());
        String refreshToken = jwtUtil.generateRefreshToken(captain.getId());
        captainService.updateRefreshToken(captain.getId(), refreshToken);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("captain", captain);
        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody CaptainLoginRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        Captain captain = captainService.findByEmail(email).orElse(null);
        if (captain == null || !passwordUtil.matches(request.getPassword(), captain.getPassword())) {
            return Map.of("message", "Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(captain.getId());
        String refreshToken = jwtUtil.generateRefreshToken(captain.getId());
        captainService.updateRefreshToken(captain.getId(), refreshToken);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("captain", captain);
        return response;
    }

    @PostMapping("/refresh")
    public Map<String, Object> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            Claims claims = jwtUtil.parseRefreshToken(request.getRefreshToken());
            String captainId = claims.get("_id", String.class);
            Captain captain = captainService.findById(captainId).orElse(null);
            if (captain == null || captain.getRefreshToken() == null || !captain.getRefreshToken().equals(request.getRefreshToken())) {
                return Map.of("message", "Token mismatch");
            }

            String newAccessToken = jwtUtil.generateAccessToken(captainId);
            return Map.of("accessToken", newAccessToken);
        } catch (Exception ex) {
            return Map.of("message", "Invalid token");
        }
    }

    @GetMapping("/profile")
    public Map<String, Object> profile(HttpServletRequest request) {
        Captain captain = RequestContext.getCaptain(request);
        if (captain == null) {
            return Map.of("message", "Captain not found");
        }
        return Map.of("captain", captain);
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
        return Map.of("message", "Logged out successfully");
    }
}
