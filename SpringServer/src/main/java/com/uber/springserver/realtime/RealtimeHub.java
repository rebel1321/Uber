package com.uber.springserver.realtime;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RealtimeHub {

    private final Map<String, Map<String, Set<WebSocketSession>>> byType = new ConcurrentHashMap<>();

    public RealtimeHub() {
        byType.put("user", new ConcurrentHashMap<>());
        byType.put("captain", new ConcurrentHashMap<>());
    }

    public void register(String userType, String userId, WebSocketSession session) {
        if (userType == null || userType.isBlank() || userId == null || userId.isBlank()) {
            return;
        }
        byType.computeIfAbsent(userType, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    public void unregister(String userType, String userId, WebSocketSession session) {
        if (userType == null || userId == null) {
            return;
        }
        Map<String, Set<WebSocketSession>> byId = byType.get(userType);
        if (byId == null) {
            return;
        }
        Set<WebSocketSession> sessions = byId.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            byId.remove(userId);
        }
    }

    public void sendTo(String userType, String userId, String payload) {
        Map<String, Set<WebSocketSession>> byId = byType.get(userType);
        if (byId == null) {
            return;
        }
        Set<WebSocketSession> sessions = byId.get(userId);
        if (sessions == null) {
            return;
        }
        for (WebSocketSession session : sessions) {
            send(session, payload);
        }
    }

    public void broadcastToType(String userType, String payload) {
        Map<String, Set<WebSocketSession>> byId = byType.get(userType);
        if (byId == null) {
            return;
        }
        for (Set<WebSocketSession> sessions : byId.values()) {
            for (WebSocketSession session : sessions) {
                send(session, payload);
            }
        }
    }

    private void send(WebSocketSession session, String payload) {
        if (!session.isOpen()) {
            return;
        }
        synchronized (session) {
            try {
                session.sendMessage(new TextMessage(payload));
            } catch (IOException ignored) {
            }
        }
    }
}
