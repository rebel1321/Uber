package services

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"net/url"
	"os"

	"uber/config"
	"uber/internal/models"

	"go.mongodb.org/mongo-driver/bson"
)

// 📍 RESPONSE STRUCTS

type Coordinates struct {
	Latitude         float64 `json:"latitude"`
	Longitude        float64 `json:"longitude"`
	FormattedAddress string  `json:"formatted_address"`
}

type DistanceElement struct {
	Distance float64 `json:"distance"`
	Duration float64 `json:"duration"`
}

// 📍 GET COORDINATES
func GetAddressCoordinates(address string) (*Coordinates, error) {

	if address == "" {
		return nil, fmt.Errorf("address required")
	}

	apiKey := os.Getenv("OLA_API_KEY")

	baseURL := "https://api.olamaps.io/places/v1/geocode"

	u, err := url.Parse(baseURL)
	if err != nil {
		return nil, err
	}

	q := u.Query()
	q.Set("address", address)
	q.Set("api_key", apiKey)
	u.RawQuery = q.Encode()

	resp, err := http.Get(u.String())
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	var result map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return nil, err
	}

	results, ok := result["geocodingResults"].([]interface{})
	if !ok || len(results) == 0 {
		return nil, fmt.Errorf("no coordinates found")
	}

	first := results[0].(map[string]interface{})
	geometry := first["geometry"].(map[string]interface{})
	location := geometry["location"].(map[string]interface{})

	return &Coordinates{
		Latitude:         location["lat"].(float64),
		Longitude:        location["lng"].(float64),
		FormattedAddress: first["formatted_address"].(string),
	}, nil
}

// 🚗 GET DISTANCE + TIME
func GetDistanceAndTime(origin, destination string) (*DistanceElement, error) {

	if origin == "" || destination == "" {
		return nil, fmt.Errorf("origin and destination required")
	}

	apiKey := os.Getenv("OLA_API_KEY")

	originCoords, err := GetAddressCoordinates(origin)
	if err != nil {
		return nil, err
	}

	destCoords, err := GetAddressCoordinates(destination)
	if err != nil {
		return nil, err
	}

	baseURL := "https://api.olamaps.io/routing/v1/distanceMatrix"

	u, err := url.Parse(baseURL)
	if err != nil {
		return nil, err
	}

	q := u.Query()
	q.Set("origins", fmt.Sprintf("%f,%f", originCoords.Latitude, originCoords.Longitude))
	q.Set("destinations", fmt.Sprintf("%f,%f", destCoords.Latitude, destCoords.Longitude))
	q.Set("api_key", apiKey)
	u.RawQuery = q.Encode()

	resp, err := http.Get(u.String())
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	var result map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return nil, err
	}

	rows, ok := result["rows"].([]interface{})
	if !ok || len(rows) == 0 {
		return nil, fmt.Errorf("no distance data found")
	}

	elements := rows[0].(map[string]interface{})["elements"].([]interface{})
	data := elements[0].(map[string]interface{})

	return &DistanceElement{
		Distance: data["distance"].(float64),
		Duration: data["duration"].(float64),
	}, nil
}

// 🔍 AUTOCOMPLETE
func GetAutoCompleteSuggestions(input string) ([]interface{}, error) {

	if input == "" {
		return nil, fmt.Errorf("input required")
	}

	apiKey := os.Getenv("OLA_API_KEY")

	baseURL := "https://api.olamaps.io/places/v1/autocomplete"

	u, err := url.Parse(baseURL)
	if err != nil {
		return nil, err
	}

	q := u.Query()
	q.Set("input", input)
	q.Set("api_key", apiKey)
	u.RawQuery = q.Encode()

	resp, err := http.Get(u.String())
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	var result map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return nil, err
	}

	predictions, ok := result["predictions"].([]interface{})
	if !ok {
		return []interface{}{}, nil
	}

	return predictions, nil
}

// 📡 GET CAPTAINS IN RADIUS (FIXED)
func GetCaptainsInRadius(lat, lng, radius float64) ([]models.Captain, error) {

	filter := bson.M{
	"location": bson.M{
		"$geoWithin": bson.M{
			"$centerSphere": []interface{}{
				[]float64{lng, lat},
				radius / 6371,
			},
		},
	},
}

	cursor, err := config.DB.Collection("captains").Find(context.Background(), filter)
	if err != nil {
		return nil, err
	}

	var captains []models.Captain

	if err := cursor.All(context.Background(), &captains); err != nil {
		return nil, err
	}

	return captains, nil
}