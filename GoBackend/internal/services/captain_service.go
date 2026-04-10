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

func CreateCaptain(c models.Captain) (*models.Captain, error) {
	c.Email = strings.ToLower(strings.TrimSpace(c.Email))
	c.CreatedAt = time.Now()

	// Insert into DB
	result, err := config.DB.Collection("captains").InsertOne(context.Background(), c)
	if err != nil {
		return nil, err
	}

	// ✅ Set the generated MongoDB _id
	c.ID = result.InsertedID.(primitive.ObjectID)

	return &c, nil
}

func FindCaptainByEmail(email string) (*models.Captain, error) {
	email = strings.ToLower(strings.TrimSpace(email))

	var c models.Captain
	err := config.DB.Collection("captains").
		FindOne(context.Background(), bson.M{"email": email}).
		Decode(&c)

	if err == mongo.ErrNoDocuments {
		return nil, nil // ✅ correct
	}

	return &c, err
}
