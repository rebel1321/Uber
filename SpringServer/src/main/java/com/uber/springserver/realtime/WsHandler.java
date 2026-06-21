package com.uber.springserver.realtime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uber.springserver.models.Location;
import com.uber.springserver.services.CaptainService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WsHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final RealtimeHub realtimeHub;
    private final CaptainService captainService;
    private final Map<String, String> sessionUserType = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUserId = new ConcurrentHashMap<>();

    public WsHandler(ObjectMapper objectMapper, RealtimeHub realtimeHub, CaptainService captainService) {
        this.objectMapper = objectMapper;
        this.realtimeHub = realtimeHub;
        this.captainService = captainService;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode root = objectMapper.readTree(message.getPayload());
            String type = root.path("type").asText();
            JsonNode payload = root.path("payload");

            switch (type) {
                case "join" -> handleJoin(session, payload);
                case "update-location-captain" -> handleLocationUpdate(payload);
                default -> {
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void handleJoin(WebSocketSession session, JsonNode payload) {
        String userId = payload.path("userId").asText();
        String userType = payload.path("userType").asText();
        if (userId == null || userId.isBlank() || userType == null || userType.isBlank()) {
            return;
        }

        sessionUserId.put(session.getId(), userId);
        sessionUserType.put(session.getId(), userType);
        realtimeHub.register(userType, userId, session);
    }

    private void handleLocationUpdate(JsonNode payload) {
        String userId = payload.path("userId").asText();
        JsonNode locationNode = payload.path("location");
        if (userId == null || userId.isBlank() || locationNode.isMissingNode()) {
            return;
        }

        Location location = new Location();
        location.setLtd(locationNode.path("ltd").asDouble());
        location.setLng(locationNode.path("lng").asDouble());
        captainService.updateLocation(userId, location);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userType = sessionUserType.remove(session.getId());
        String userId = sessionUserId.remove(session.getId());
        realtimeHub.unregister(userType, userId, session);
    }
}
