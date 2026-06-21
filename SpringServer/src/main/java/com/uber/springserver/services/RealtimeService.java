package com.uber.springserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uber.springserver.realtime.RealtimeHub;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RealtimeService {

    private final RealtimeHub realtimeHub;
    private final ObjectMapper objectMapper;

    public RealtimeService(RealtimeHub realtimeHub, ObjectMapper objectMapper) {
        this.realtimeHub = realtimeHub;
        this.objectMapper = objectMapper;
    }

    public void sendToUser(String userId, String event, Object payload) {
        realtimeHub.sendTo("user", userId, buildMessage(event, payload));
    }

    public void sendToCaptain(String captainId, String event, Object payload) {
        realtimeHub.sendTo("captain", captainId, buildMessage(event, payload));
    }

    public void broadcastToCaptains(String event, Object payload) {
        realtimeHub.broadcastToType("captain", buildMessage(event, payload));
    }

    private String buildMessage(String event, Object payload) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", event);
        message.put("payload", payload);
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
