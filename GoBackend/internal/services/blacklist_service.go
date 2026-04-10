package services

import (
	"context"
	"time"

	"uber/config"
	"uber/internal/models"
	"go.mongodb.org/mongo-driver/bson"
)

func AddToBlacklist(token string) error {
	_, err := config.DB.Collection("blacklist").
		InsertOne(context.Background(), models.BlacklistToken{
			Token: token,
			CreatedAt: time.Now(),
		})
	return err
}

func IsBlacklisted(token string) bool {
	err := config.DB.Collection("blacklist").
		FindOne(context.Background(), bson.M{"token": token}).Err()
	return err == nil
}