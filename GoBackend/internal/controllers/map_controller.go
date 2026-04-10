package controllers

import (

	"uber/internal/services"

	"github.com/gin-gonic/gin"
)

// 📍 COORDINATES
func GetCoordinates(c *gin.Context) {

	address := c.Query("address")
	if len(address) < 3 {
		c.JSON(400, gin.H{"message": "Invalid address"})
		return
	}

	data, err := services.GetAddressCoordinates(address)
	if err != nil {
		c.JSON(404, gin.H{"message": "Coordinates not found"})
		return
	}

	c.JSON(200, data)
}

// 🚗 DISTANCE
func GetDistanceTime(c *gin.Context) {

	origin := c.Query("origin")
	destination := c.Query("destination")

	if origin == "" || destination == "" {
		c.JSON(400, gin.H{"message": "Origin & destination required"})
		return
	}

	data, err := services.GetDistanceAndTime(origin, destination)
	if err != nil {
		c.JSON(500, gin.H{"message": err.Error()})
		return
	}

	c.JSON(200, data)
}

// 🔍 AUTOCOMPLETE
func GetAutoCompleteSuggestions(c *gin.Context) {

	input := c.Query("input")
	if input == "" {
		c.JSON(400, gin.H{"message": "Input required"})
		return
	}

	data, err := services.GetAutoCompleteSuggestions(input)
	if err != nil {
		c.JSON(500, gin.H{"message": "Failed to fetch suggestions"})
		return
	}

	c.JSON(200, data)
}