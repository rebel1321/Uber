package com.uber.springserver.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of("message", "🚀 Uber API Server");
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "healthy");
    }
}
