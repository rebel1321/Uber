package controllers

import (
	"uber/internal/models"
	"uber/internal/realtime"
	"uber/internal/services"

	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

// 💰 FARE ESTIMATE
func GetFare(c *gin.Context) {
	pickup := c.Query("pickup")
	destination := c.Query("destination")
	tripType := c.DefaultQuery("tripType", "one_way")
	if pickup == "" || destination == "" {
		c.JSON(400, gin.H{"error": "pickup and destination required"})
		return
	}

	fare, distanceKm, duration, _ := services.CalculateFare(pickup, destination, tripType)

	c.JSON(200, gin.H{
		"fare":     fare,
		"distance": distanceKm,
		"duration": duration,
	})
}

// 🚗 CREATE
func CreateRide(c *gin.Context) {

	var body struct {
		Pickup      string `json:"pickup"`
		Destination string `json:"destination"`
		VehicleType string `json:"vehicleType"`
		TripType    string `json:"tripType"`
	}

	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(400, gin.H{"error": err.Error()})
		return
	}

	user := c.MustGet("user").(models.User)
	userID := user.ID

	tripType := body.TripType
	if tripType == "" {
		tripType = "one_way"
	}

	ride, err := services.CreateRide(userID, body.Pickup, body.Destination, body.VehicleType, tripType)
	if err != nil {
		c.JSON(500, gin.H{"error": err.Error()})
		return
	}

	if payload, err := services.BuildRidePayload(ride.ID); err == nil {
		realtime.BroadcastToCaptains("new-ride", payload)
	}

	c.JSON(201, ride)
}

// 🚗 CONFIRM
func ConfirmRide(c *gin.Context) {

	var body struct {
		RideID string `json:"rideId"`
	}

	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(400, gin.H{"error": err.Error()})
		return
	}
	if body.RideID == "" {
		c.JSON(400, gin.H{"error": "rideId required"})
		return
	}

	captain := c.MustGet("user").(models.Captain)
	captainID := captain.ID

	rideID, err := primitive.ObjectIDFromHex(body.RideID)
	if err != nil {
		c.JSON(400, gin.H{"error": "invalid rideId"})
		return
	}

	ride, err := services.ConfirmRide(rideID, captainID)
	if err != nil {
		c.JSON(500, gin.H{"error": err.Error()})
		return
	}

	if payload, err := services.BuildRidePayload(ride.ID); err == nil {
		realtime.SendToUser(ride.User.Hex(), "ride-confirmed", payload)
	}

	c.JSON(200, ride)
}

// 🚗 START
func StartRide(c *gin.Context) {

	rideIDStr := c.Query("rideId")
	otp := c.Query("otp")
	if rideIDStr == "" || otp == "" {
		c.JSON(400, gin.H{"error": "rideId and otp required"})
		return
	}

	rideID, err := primitive.ObjectIDFromHex(rideIDStr)
	if err != nil {
		c.JSON(400, gin.H{"error": "invalid rideId"})
		return
	}

	ride, err := services.StartRide(rideID, otp)
	if err != nil {
		c.JSON(400, gin.H{"error": err.Error()})
		return
	}

	if payload, err := services.BuildRidePayload(ride.ID); err == nil {
		realtime.SendToUser(ride.User.Hex(), "ride-started", payload)
	}

	c.JSON(200, ride)
}

// 🏁 END
func EndRide(c *gin.Context) {

	var body struct {
		RideID string `json:"rideId"`
	}

	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(400, gin.H{"error": err.Error()})
		return
	}
	if body.RideID == "" {
		c.JSON(400, gin.H{"error": "rideId required"})
		return
	}

	captain := c.MustGet("user").(models.Captain)
	captainID := captain.ID

	rideID, err := primitive.ObjectIDFromHex(body.RideID)
	if err != nil {
		c.JSON(400, gin.H{"error": "invalid rideId"})
		return
	}

	ride, err := services.EndRide(rideID, captainID)
	if err != nil {
		c.JSON(400, gin.H{"error": err.Error()})
		return
	}

	if payload, err := services.BuildRidePayload(ride.ID); err == nil {
		realtime.SendToUser(ride.User.Hex(), "ride-ended", payload)
	}

	c.JSON(200, ride)
}

// 💳 PAY
func PayRide(c *gin.Context) {
	var body struct {
		RideID string `json:"rideId"`
	}

	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(400, gin.H{"error": err.Error()})
		return
	}
	if body.RideID == "" {
		c.JSON(400, gin.H{"error": "rideId required"})
		return
	}

	user := c.MustGet("user").(models.User)

	rideID, err := primitive.ObjectIDFromHex(body.RideID)
	if err != nil {
		c.JSON(400, gin.H{"error": "invalid rideId"})
		return
	}

	ride, err := services.MarkRidePaid(rideID, user.ID)
	if err != nil {
		c.JSON(400, gin.H{"error": err.Error()})
		return
	}

	if ride.Captain != nil {
		if payload, err := services.BuildRidePayload(ride.ID); err == nil {
			realtime.SendToCaptain(ride.Captain.Hex(), "ride-paid", payload)
		}
	}

	c.JSON(200, ride)
}
