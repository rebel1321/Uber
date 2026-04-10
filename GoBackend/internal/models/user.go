package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type FullName struct {
	FirstName string `bson:"firstName" json:"firstName"`
	LastName  string `bson:"lastName" json:"lastName"`
}

type User struct {
	ID           primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	FullName     FullName           `bson:"fullName" json:"fullName"`
	Email        string             `bson:"email" json:"email"`
	Password     string             `bson:"password" json:"-"`
	SocketID     string             `bson:"socketId,omitempty" json:"socketId,omitempty"`
	RefreshToken string             `bson:"refreshToken,omitempty" json:"-"`
	CreatedAt    time.Time          `bson:"createdAt"`
}