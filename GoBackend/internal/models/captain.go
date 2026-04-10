package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type Vehicle struct {
	Color       string `bson:"color" json:"color"`
	Plate       string `bson:"plate" json:"plate"`
	Capacity    int    `bson:"capacity" json:"capacity"`
	VehicleType string `bson:"vehicleType" json:"vehicleType"`
}

type Location struct {
	Ltd float64 `bson:"ltd" json:"ltd"`
	Lng float64 `bson:"lng" json:"lng"`
}

type Captain struct {
	ID           primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	FullName     FullName           `bson:"fullName" json:"fullName"`
	Email        string             `bson:"email" json:"email"`
	Password     string             `bson:"password" json:"-"`
	SocketID     string             `bson:"socketId,omitempty"`
	Status       string             `bson:"status" json:"status"`
	Vehicle      Vehicle            `bson:"vehicle" json:"vehicle"`
	Location     Location           `bson:"location" json:"location"`
	RefreshToken string             `bson:"refreshToken,omitempty"`
	CreatedAt    time.Time          `bson:"createdAt"`
}