package controllers

import (
	"context"
	"os"
	"strings"

	"uber/config"
	"uber/internal/models"
	"uber/internal/services"
	"uber/internal/utils"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
	"github.com/golang-jwt/jwt/v5"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

var validateCaptain = validator.New()

// 🚗 REGISTER
func RegisterCaptain(c *gin.Context) {
	type registerCaptainRequest struct {
		FullName models.FullName `json:"fullName" validate:"required"`
		Email    string          `json:"email" validate:"required,email"`
		Password string          `json:"password" validate:"required,min=6"`
		Vehicle  models.Vehicle  `json:"vehicle" validate:"required"`
		Location models.Location `json:"location"`
	}

	var req registerCaptainRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(400, gin.H{"error": err.Error()})
		return
	}

	if err := validateCaptain.Struct(req); err != nil {
		c.JSON(400, gin.H{"error": err.Error()})
		return
	}

	input := models.Captain{
		FullName: req.FullName,
		Email:    req.Email,
		Password: req.Password,
		Vehicle:  req.Vehicle,
		Location: req.Location,
	}

	input.Email = strings.ToLower(strings.TrimSpace(input.Email))

	existing, err := services.FindCaptainByEmail(input.Email)
	if err != nil {
		c.JSON(500, gin.H{"error": "Server error"})
		return
	}
	if existing != nil {
		c.JSON(400, gin.H{"message": "Captain already exists"})
		return
	}

	hash, err := utils.HashPassword(input.Password)
	if err != nil {
		c.JSON(500, gin.H{"error": "Hashing failed"})
		return
	}

	input.Password = hash
	input.Status = "inactive"

	captain, err := services.CreateCaptain(input)
	if err != nil {
		c.JSON(500, gin.H{"error": err.Error()})
		return
	}

	access, _ := utils.GenerateAccessToken(captain.ID.Hex())
	refresh, _ := utils.GenerateRefreshToken(captain.ID.Hex())

	config.DB.Collection("captains").UpdateOne(
		context.Background(),
		bson.M{"_id": captain.ID},
		bson.M{"$set": bson.M{"refreshToken": refresh}},
	)

	c.JSON(201, gin.H{
		"accessToken":  access,
		"refreshToken": refresh,
		"captain":      captain,
	})
}

// 🚗 LOGIN
func LoginCaptain(c *gin.Context) {

	var body struct {
		Email    string `json:"email" validate:"required,email"`
		Password string `json:"password" validate:"required,min=6"`
	}

	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(400, gin.H{"error": err.Error()})
		return
	}

	if err := validateCaptain.Struct(body); err != nil {
		c.JSON(400, gin.H{"error": err.Error()})
		return
	}

	body.Email = strings.ToLower(strings.TrimSpace(body.Email))

	captain, err := services.FindCaptainByEmail(body.Email)
	if err != nil {
		c.JSON(500, gin.H{"message": "Server error"})
		return
	}

	if captain == nil {
		c.JSON(401, gin.H{"message": "Invalid credentials"})
		return
	}

	if utils.ComparePassword(captain.Password, body.Password) != nil {
		c.JSON(401, gin.H{"message": "Invalid credentials"})
		return
	}

	access, _ := utils.GenerateAccessToken(captain.ID.Hex())
	refresh, _ := utils.GenerateRefreshToken(captain.ID.Hex())

	config.DB.Collection("captains").UpdateOne(
		context.Background(),
		bson.M{"_id": captain.ID},
		bson.M{"$set": bson.M{"refreshToken": refresh}},
	)

	c.JSON(200, gin.H{
		"accessToken":  access,
		"refreshToken": refresh,
		"captain":      captain,
	})
}

// 🚗 PROFILE
func GetCaptainProfile(c *gin.Context) {
	captain, exists := c.Get("user")
	if !exists {
		c.JSON(500, gin.H{"message": "Captain not found"})
		return
	}

	c.JSON(200, gin.H{"captain": captain})
}

// 🔄 REFRESH
func RefreshCaptainToken(c *gin.Context) {

	var body struct {
		RefreshToken string `json:"refreshToken"`
	}

	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(400, gin.H{"error": "Invalid request"})
		return
	}

	token, err := jwt.Parse(body.RefreshToken, func(t *jwt.Token) (interface{}, error) {
		return []byte(os.Getenv("JWT_REFRESH_SECRET")), nil
	})

	if err != nil || !token.Valid {
		c.JSON(401, gin.H{"message": "Invalid token"})
		return
	}

	claims := token.Claims.(jwt.MapClaims)
	id := claims["_id"].(string)

	objID, _ := primitive.ObjectIDFromHex(id)

	var captain struct {
		RefreshToken string `bson:"refreshToken"`
	}

	err = config.DB.Collection("captains").
		FindOne(context.Background(), bson.M{"_id": objID}).
		Decode(&captain)

	if err != nil || captain.RefreshToken != body.RefreshToken {
		c.JSON(401, gin.H{"message": "Token mismatch"})
		return
	}

	newAccess, _ := utils.GenerateAccessToken(id)

	c.JSON(200, gin.H{"accessToken": newAccess})
}

// 🚪 LOGOUT
func LogoutCaptain(c *gin.Context) {

	auth := c.GetHeader("Authorization")

	if auth == "" {
		c.JSON(400, gin.H{"message": "Token missing"})
		return
	}

	parts := strings.Split(auth, " ")
	if len(parts) != 2 {
		c.JSON(400, gin.H{"message": "Invalid token format"})
		return
	}

	services.AddToBlacklist(parts[1])

	c.JSON(200, gin.H{"message": "Logged out successfully"})
}
