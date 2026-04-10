package services

import (
	"context"
	"crypto/rand"
	"fmt"
	"math/big"

	"uber/config"
	"uber/internal/models"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

func generateOTP(n int) string {
	min := int64(1)
	for i := 1; i < n; i++ {
		min *= 10
	}
	max := min * 10

	num, _ := rand.Int(rand.Reader, big.NewInt(max-min))
	return fmt.Sprintf("%d", num.Int64()+min)
}

func normalizeDistanceKm(distance float64) float64 {
	if distance <= 0 {
		return 0
	}
	if distance > 1000 {
		return distance / 1000
	}
	return distance
}

func tripMultiplier(tripType string) float64 {
	switch tripType {
	case "round_trip", "two_way", "roundtrip":
		return 2
	default:
		return 1
	}
}

func estimateFare(distanceKm float64, tripType string) map[string]float64 {
	base := map[string]float64{
		"auto": 30,
		"car":  50,
		"moto": 20,
	}
	perKm := map[string]float64{
		"auto": 8,
		"car":  12,
		"moto": 6,
	}

	multiplier := tripMultiplier(tripType)

	fare := map[string]float64{}
	for vehicleType, baseFare := range base {
		fare[vehicleType] = (baseFare + perKm[vehicleType]*distanceKm) * multiplier
	}

	return fare
}

func CalculateFare(pickup, destination, tripType string) (map[string]float64, float64, float64, error) {
	distanceKm := 0.0
	duration := 0.0

	distanceData, err := GetDistanceAndTime(pickup, destination)
	if err == nil && distanceData != nil {
		distanceKm = normalizeDistanceKm(distanceData.Distance)
		duration = distanceData.Duration
	}

	if distanceKm == 0 {
		return estimateFare(5, tripType), 5, duration, nil
	}

	return estimateFare(distanceKm, tripType), distanceKm, duration, nil
}

// 🚗 CREATE RIDE
func CreateRide(userID primitive.ObjectID, pickup, destination, vehicleType, tripType string) (*models.Ride, error) {

	if pickup == "" || destination == "" {
		return nil, fmt.Errorf("pickup and destination required")
	}

	fareMap, distanceKm, duration, _ := CalculateFare(pickup, destination, tripType)

	fare, ok := fareMap[vehicleType]
	if !ok {
		return nil, fmt.Errorf("invalid vehicle type")
	}

	ride := models.Ride{
		User:        userID,
		Pickup:      pickup,
		Destination: destination,
		Fare:        fare,
		Status:      "pending",
		VehicleType: vehicleType,
		TripType:    tripType,
		Distance:    distanceKm,
		Duration:    duration,
		OTP:         generateOTP(6),
	}

	res, err := config.DB.Collection("rides").InsertOne(context.Background(), ride)
	if err != nil {
		return nil, err
	}

	ride.ID = res.InsertedID.(primitive.ObjectID)
	return &ride, nil
}

// ✅ CONFIRM RIDE
func ConfirmRide(rideID, captainID primitive.ObjectID) (*models.Ride, error) {

	_, err := config.DB.Collection("rides").UpdateOne(
		context.Background(),
		bson.M{"_id": rideID},
		bson.M{
			"$set": bson.M{
				"status":  "accepted",
				"captain": captainID,
			},
		},
	)
	if err != nil {
		return nil, err
	}

	var ride models.Ride
	err = config.DB.Collection("rides").
		FindOne(context.Background(), bson.M{"_id": rideID}).
		Decode(&ride)

	return &ride, err
}

// 🚗 START RIDE
func StartRide(rideID primitive.ObjectID, otp string) (*models.Ride, error) {

	var ride models.Ride
	err := config.DB.Collection("rides").
		FindOne(context.Background(), bson.M{"_id": rideID}).
		Decode(&ride)

	if err != nil {
		return nil, err
	}

	if ride.Status != "accepted" {
		return nil, fmt.Errorf("ride not accepted")
	}

	if ride.OTP != otp {
		return nil, fmt.Errorf("invalid otp")
	}

	_, err = config.DB.Collection("rides").UpdateOne(
		context.Background(),
		bson.M{"_id": rideID},
		bson.M{"$set": bson.M{"status": "ongoing"}},
	)
	if err != nil {
		return nil, err
	}

	ride.Status = "ongoing"

	return &ride, nil
}

// 🏁 END RIDE
func EndRide(rideID primitive.ObjectID, captainID primitive.ObjectID) (*models.Ride, error) {

	var ride models.Ride
	err := config.DB.Collection("rides").
		FindOne(context.Background(), bson.M{
			"_id":     rideID,
			"captain": captainID,
		}).
		Decode(&ride)

	if err != nil {
		return nil, err
	}

	if ride.Status != "ongoing" {
		return nil, fmt.Errorf("ride not ongoing")
	}

	_, err = config.DB.Collection("rides").UpdateOne(
		context.Background(),
		bson.M{"_id": rideID},
		bson.M{"$set": bson.M{"status": "completed"}},
	)
	if err != nil {
		return nil, err
	}

	ride.Status = "completed"

	return &ride, nil
}

// 💳 MARK PAID
func MarkRidePaid(rideID primitive.ObjectID, userID primitive.ObjectID) (*models.Ride, error) {
	var ride models.Ride
	err := config.DB.Collection("rides").
		FindOne(context.Background(), bson.M{"_id": rideID, "user": userID}).
		Decode(&ride)
	if err != nil {
		return nil, err
	}

	if ride.Status != "completed" {
		return nil, fmt.Errorf("ride not completed")
	}

	_, err = config.DB.Collection("rides").UpdateOne(
		context.Background(),
		bson.M{"_id": rideID},
		bson.M{"$set": bson.M{"paid": true}},
	)
	if err != nil {
		return nil, err
	}

	ride.Paid = true
	return &ride, nil
}

// 🧩 RIDE PAYLOAD (with user/captain info)
func BuildRidePayload(rideID primitive.ObjectID) (map[string]interface{}, error) {
	var ride models.Ride
	err := config.DB.Collection("rides").
		FindOne(context.Background(), bson.M{"_id": rideID}).
		Decode(&ride)
	if err != nil {
		return nil, err
	}

	var user models.User
	err = config.DB.Collection("users").
		FindOne(context.Background(), bson.M{"_id": ride.User}).
		Decode(&user)
	if err != nil {
		return nil, err
	}

	var captainPayload interface{} = nil
	if ride.Captain != nil {
		var captain models.Captain
		if err := config.DB.Collection("captains").
			FindOne(context.Background(), bson.M{"_id": ride.Captain}).
			Decode(&captain); err == nil {
			captainPayload = map[string]interface{}{
				"_id":      captain.ID.Hex(),
				"fullName": captain.FullName,
				"email":    captain.Email,
				"vehicle":  captain.Vehicle,
				"status":   captain.Status,
			}
		}
	}

	payload := map[string]interface{}{
		"_id":         ride.ID.Hex(),
		"pickup":      ride.Pickup,
		"destination": ride.Destination,
		"vehicleType": ride.VehicleType,
		"tripType":    ride.TripType,
		"fare":        ride.Fare,
		"status":      ride.Status,
		"paid":        ride.Paid,
		"otp":         ride.OTP,
		"distance":    ride.Distance,
		"duration":    ride.Duration,
		"user": map[string]interface{}{
			"_id":      user.ID.Hex(),
			"fullName": user.FullName,
			"email":    user.Email,
		},
		"captain": captainPayload,
	}

	return payload, nil
}
