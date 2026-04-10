package middleware

import (
	"context"
	"os"
	"strings"

	"uber/config"
	"uber/internal/models"
	"uber/internal/services"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

func extractToken(c *gin.Context) (string, bool) {
	auth := c.GetHeader("Authorization")
	if auth != "" {
		parts := strings.Split(auth, " ")
		if len(parts) == 2 {
			return parts[1], true
		}
	}
	if cookie, err := c.Cookie("token"); err == nil {
		return cookie, true
	}
	return "", false
}

func AuthMiddleware(collection string) gin.HandlerFunc {
	return func(c *gin.Context) {

		token, ok := extractToken(c)
		if !ok {
			c.JSON(401, gin.H{"message": "Token missing"})
			c.Abort()
			return
		}

		if services.IsBlacklisted(token) {
			c.JSON(401, gin.H{"message": "Token blacklisted"})
			c.Abort()
			return
		}

		parsed, err := jwt.Parse(token, func(t *jwt.Token) (interface{}, error) {
			return []byte(os.Getenv("JWT_ACCESS_SECRET")), nil
		})

		if err != nil || !parsed.Valid {
			c.JSON(401, gin.H{"message": "Invalid token"})
			c.Abort()
			return
		}

		id := parsed.Claims.(jwt.MapClaims)["_id"].(string)
		objID, _ := primitive.ObjectIDFromHex(id)

		if collection == "users" {
			var u models.User
			err = config.DB.Collection("users").
				FindOne(context.Background(), bson.M{"_id": objID}).
				Decode(&u)
			if err != nil {
				c.JSON(401, gin.H{"message": "User not found"})
				c.Abort()
				return
			}
			c.Set("user", u)
		} else {
			var cpt models.Captain
			err = config.DB.Collection("captains").
				FindOne(context.Background(), bson.M{"_id": objID}).
				Decode(&cpt)
			if err != nil {
				c.JSON(401, gin.H{"message": "Captain not found"})
				c.Abort()
				return
			}
			c.Set("user", cpt)
		}

		c.Next()
	}
}