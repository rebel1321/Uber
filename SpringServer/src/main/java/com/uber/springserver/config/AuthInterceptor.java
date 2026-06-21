package com.uber.springserver.config;

import com.uber.springserver.models.Captain;
import com.uber.springserver.models.User;
import com.uber.springserver.services.BlacklistService;
import com.uber.springserver.services.CaptainService;
import com.uber.springserver.services.UserService;
import com.uber.springserver.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final BlacklistService blacklistService;
    private final UserService userService;
    private final CaptainService captainService;

    public AuthInterceptor(JwtUtil jwtUtil,
                           BlacklistService blacklistService,
                           UserService userService,
                           CaptainService captainService) {
        this.jwtUtil = jwtUtil;
        this.blacklistService = blacklistService;
        this.userService = userService;
        this.captainService = captainService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow CORS preflight requests to pass without authentication
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = extractToken(request);
        if (token == null || token.isBlank()) {
            unauthorized(response, "Token missing");
            return false;
        }

        if (blacklistService.isBlacklisted(token)) {
            unauthorized(response, "Token blacklisted");
            return false;
        }

        Claims claims;
        try {
            claims = jwtUtil.parseAccessToken(token);
        } catch (Exception ex) {
            unauthorized(response, "Invalid token");
            return false;
        }

        String id = claims.get("_id", String.class);
        String path = request.getRequestURI();

        if (path.contains("/captain") || path.contains("/ride/confirm") || path.contains("/ride/start") || path.contains("/ride/end") || path.contains("/rides/start-ride") || path.contains("/rides/end-ride")) {
            Captain captain = captainService.findById(id).orElse(null);
            if (captain == null) {
                unauthorized(response, "Captain not found");
                return false;
            }
            request.setAttribute(RequestContext.CAPTAIN_ATTR, captain);
        } else {
            User user = userService.findById(id).orElse(null);
            if (user == null) {
                unauthorized(response, "User not found");
                return false;
            }
            request.setAttribute(RequestContext.USER_ATTR, user);
        }

        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private void unauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
