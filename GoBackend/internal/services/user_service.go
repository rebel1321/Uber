package services

import (
	"context"
	"strings"
	"time"

	"uber/config"
	"uber/internal/models"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
)

func CreateUser(user models.User) (*models.User, error) {
	user.Email = strings.ToLower(strings.TrimSpace(user.Email))
	user.CreatedAt = time.Now()

	result, err := config.DB.Collection("users").InsertOne(context.Background(), user)
	if err != nil {
		return nil, err
	}

	user.ID = result.InsertedID.(primitive.ObjectID) // ✅ SET ID

	return &user, nil
}

func FindUserByEmail(email string) (*models.User, error) {
	email = strings.ToLower(strings.TrimSpace(email))

	var user models.User
	err := config.DB.Collection("users").
		FindOne(context.Background(), bson.M{"email": email}).
		Decode(&user)

	if err == mongo.ErrNoDocuments {
		return nil, nil // ✅ correct handling
	}

	return &user, err
}
