package realtime

import (
	"encoding/json"
	"sync"
)

type Client struct {
	conn     Conn
	userID   string
	userType string
	writeMu  sync.Mutex
}

type Conn interface {
	WriteMessage(messageType int, data []byte) error
	Close() error
}

type Hub struct {
	mu     sync.RWMutex
	byType map[string]map[string]map[*Client]bool
}

func NewHub() *Hub {
	return &Hub{
		byType: map[string]map[string]map[*Client]bool{
			"user":    {},
			"captain": {},
		},
	}
}

func (h *Hub) Register(client *Client) {
	if client.userID == "" || client.userType == "" {
		return
	}

	h.mu.Lock()
	defer h.mu.Unlock()

	if _, ok := h.byType[client.userType]; !ok {
		h.byType[client.userType] = make(map[string]map[*Client]bool)
	}
	if _, ok := h.byType[client.userType][client.userID]; !ok {
		h.byType[client.userType][client.userID] = make(map[*Client]bool)
	}

	h.byType[client.userType][client.userID][client] = true
}

func (h *Hub) Unregister(client *Client) {
	h.mu.Lock()
	defer h.mu.Unlock()

	if client.userID == "" || client.userType == "" {
		return
	}

	clientsByID, ok := h.byType[client.userType]
	if !ok {
		return
	}

	clients, ok := clientsByID[client.userID]
	if !ok {
		return
	}

	delete(clients, client)
	if len(clients) == 0 {
		delete(clientsByID, client.userID)
	}
}

func (h *Hub) SendTo(userType, userID string, payload []byte) {
	h.mu.RLock()
	defer h.mu.RUnlock()

	clientsByID, ok := h.byType[userType]
	if !ok {
		return
	}

	clients := clientsByID[userID]
	for client := range clients {
		client.send(payload)
	}
}

func (h *Hub) BroadcastToType(userType string, payload []byte) {
	h.mu.RLock()
	defer h.mu.RUnlock()

	clientsByID, ok := h.byType[userType]
	if !ok {
		return
	}

	for _, clients := range clientsByID {
		for client := range clients {
			client.send(payload)
		}
	}
}

func (c *Client) send(payload []byte) {
	c.writeMu.Lock()
	defer c.writeMu.Unlock()

	_ = c.conn.WriteMessage(1, payload)
}

var hub = NewHub()

func SendToUser(userID string, event string, payload interface{}) {
	msg := buildMessage(event, payload)
	hub.SendTo("user", userID, msg)
}

func SendToCaptain(captainID string, event string, payload interface{}) {
	msg := buildMessage(event, payload)
	hub.SendTo("captain", captainID, msg)
}

func BroadcastToCaptains(event string, payload interface{}) {
	msg := buildMessage(event, payload)
	hub.BroadcastToType("captain", msg)
}

func buildMessage(event string, payload interface{}) []byte {
	message := map[string]interface{}{
		"type":    event,
		"payload": payload,
	}

	data, err := json.Marshal(message)
	if err != nil {
		return []byte("{}")
	}

	return data
}
