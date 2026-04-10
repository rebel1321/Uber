package models

import (
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type Ride struct {
	ID          primitive.ObjectID  `bson:"_id,omitempty" json:"id"`
	User        primitive.ObjectID  `bson:"user" json:"user"`
	Captain     *primitive.ObjectID `bson:"captain,omitempty" json:"captain,omitempty"`
	Pickup      string              `bson:"pickup" json:"pickup"`
	Destination string              `bson:"destination" json:"destination"`
	VehicleType string              `bson:"vehicleType" json:"vehicleType"`
	TripType    string              `bson:"tripType" json:"tripType"`
	Fare        float64             `bson:"fare" json:"fare"`
	Status      string              `bson:"status" json:"status"`
	Paid        bool                `bson:"paid,omitempty" json:"paid,omitempty"`
	Duration    float64             `bson:"duration,omitempty" json:"duration,omitempty"`
	Distance    float64             `bson:"distance,omitempty" json:"distance,omitempty"`
	PaymentID   string              `bson:"paymentId,omitempty"`
	OrderID     string              `bson:"orderId,omitempty"`
	Signature   string              `bson:"signature,omitempty"`
	OTP         string              `bson:"otp" json:"-"`
}