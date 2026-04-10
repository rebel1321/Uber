package realtime

import (
	"context"
	"encoding/json"
	"net/http"
	"os"
	"strings"

	"uber/config"

	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type wsMessage struct {
	Type    string          `json:"type"`
	Payload json.RawMessage `json:"payload"`
}

type joinPayload struct {
	UserID   string `json:"userId"`
	UserType string `json:"userType"`
}

type locationPayload struct {
	UserID   string `json:"userId"`
	Location struct {
		Ltd float64 `json:"ltd"`
		Lng float64 `json:"lng"`
	} `json:"location"`
}

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin: func(r *http.Request) bool {
		origin := r.Header.Get("Origin")
		if origin == "" {
			return true
		}
		allowedOriginsStr := os.Getenv("ALLOWED_ORIGINS")
		if allowedOriginsStr == "" {
			allowedOriginsStr = "http://localhost:5173"
		}
		allowedOrigins := strings.Split(allowedOriginsStr, ",")
		for _, allowed := range allowedOrigins {
			if strings.TrimSpace(allowed) == origin {
				return true
			}
		}
		return false
	},
}

func HandleWS(c *gin.Context) {
	conn, err := upgrader.Upgrade(c.Writer, c.Request, nil)
	if err != nil {
		return
	}

	client := &Client{conn: conn}

	defer func() {
		hub.Unregister(client)
		_ = conn.Close()
	}()

	for {
		_, data, err := conn.ReadMessage()
		if err != nil {
			return
		}

		var message wsMessage
		if err := json.Unmarshal(data, &message); err != nil {
			continue
		}

		switch message.Type {
		case "join":
			var payload joinPayload
			if err := json.Unmarshal(message.Payload, &payload); err != nil {
				continue
			}
			client.userID = payload.UserID
			client.userType = payload.UserType
			hub.Register(client)
		case "update-location-captain":
			var payload locationPayload
			if err := json.Unmarshal(message.Payload, &payload); err != nil {
				continue
			}
			updateCaptainLocation(payload)
		default:
			continue
		}
	}
}

func updateCaptainLocation(payload locationPayload) {
	if payload.UserID == "" {
		return
	}

	objID, err := primitive.ObjectIDFromHex(payload.UserID)
	if err != nil {
		return
	}

	_, _ = config.DB.Collection("captains").UpdateOne(
		context.Background(),
		bson.M{"_id": objID},
		bson.M{"$set": bson.M{"location": bson.M{"ltd": payload.Location.Ltd, "lng": payload.Location.Lng}}},
	)
}
