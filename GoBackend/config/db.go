package config

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

var DB *mongo.Database

func ConnectDB() {

	mongoURI := os.Getenv("MONGO_URI")
	if mongoURI == "" {
		log.Fatal("MONGO_URI not set")
	}

	clientOptions := options.Client().ApplyURI(mongoURI)

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	client, err := mongo.Connect(ctx, clientOptions)
	if err != nil {
		log.Fatal("MongoDB connection error:", err)
	}

	err = client.Ping(ctx, nil)
	if err != nil {
		log.Fatal("MongoDB ping failed:", err)
	}

	fmt.Println("✅ MongoDB Connected")

	DB = client.Database("UberGo")

	// 🔥 TTL INDEX (Blacklist)
	blacklistCollection := DB.Collection("blacklist")

	ttlIndex := mongo.IndexModel{
		Keys:    bson.M{"createdAt": 1},
		Options: options.Index().SetExpireAfterSeconds(86400),
	}

	_, err = blacklistCollection.Indexes().CreateOne(context.Background(), ttlIndex)
	if err != nil {
		log.Println("⚠️ Failed to create TTL index:", err)
	} else {
		fmt.Println("✅ Blacklist TTL index created")
	}

	// 🔥 GEO INDEX (Captains Location)
	captainCollection := DB.Collection("captains")

	geoIndex := mongo.IndexModel{
		Keys: bson.M{"location": "2dsphere"},
	}

	_, err = captainCollection.Indexes().CreateOne(context.Background(), geoIndex)
	if err != nil {
		log.Println("⚠️ Failed to create geo index:", err)
	} else {
		fmt.Println("✅ Captain geo index created")
	}
}