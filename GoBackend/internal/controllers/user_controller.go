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

var validate = validator.New()

// 🔐 REGISTER USER
func RegisterUser(c *gin.Context) {
	type registerUserRequest struct {
		FullName models.FullName `json:"fullName" validate:"required"`
		Email    string          `json:"email" validate:"required,email"`
		Password string          `json:"password" validate:"required,min=6"`
	}

	var req registerUserRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(400, gin.H{"error": err.Error()})
		return
	}

	if err := validate.Struct(req); err != nil {
		c.JSON(400, gin.H{"error": err.Error()})
		return
	}

	input := models.User{
		FullName: req.FullName,
		Email:    req.Email,
		Password: req.Password,
	}

	input.Email = strings.ToLower(strings.TrimSpace(input.Email))

	existing, err := services.FindUserByEmail(input.Email)
	if err != nil {
		c.JSON(500, gin.H{"error": "Server error"})
		return
	}
	if existing != nil {
		c.JSON(400, gin.H{"message": "User already exists"})
		return
	}

	hash, err := utils.HashPassword(input.Password)
	if err != nil {
		c.JSON(500, gin.H{"error": "Hashing failed"})
		return
	}
	input.Password = hash

	user, err := services.CreateUser(input)
	if err != nil {
		c.JSON(500, gin.H{"error": err.Error()})
		return
	}

	access, _ := utils.GenerateAccessToken(user.ID.Hex())
	refresh, _ := utils.GenerateRefreshToken(user.ID.Hex())

	// save refresh token
	config.DB.Collection("users").UpdateOne(
		context.Background(),
		bson.M{"_id": user.ID},
		bson.M{"$set": bson.M{"refreshToken": refresh}},
	)

	c.JSON(201, gin.H{
		"accessToken":  access,
		"refreshToken": refresh,
		"user":         user,
	})
}

// 🔐 LOGIN USER
func LoginUser(c *gin.Context) {

	var body struct {
		Email    string `json:"email"`
		Password string `json:"password"`
	}

	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(400, gin.H{"error": err.Error()})
		return
	}

	body.Email = strings.ToLower(strings.TrimSpace(body.Email))

	user, err := services.FindUserByEmail(body.Email)

	// ❗ handle DB error
	if err != nil {
		c.JSON(500, gin.H{"message": "Server error"})
		return
	}

	// ❗ VERY IMPORTANT (this was missing)
	if user == nil {
		c.JSON(401, gin.H{"message": "Invalid credentials"})
		return
	}

	// ❗ now safe to access password
	if utils.ComparePassword(user.Password, body.Password) != nil {
		c.JSON(401, gin.H{"message": "Invalid credentials"})
		return
	}

	access, _ := utils.GenerateAccessToken(user.ID.Hex())
	refresh, _ := utils.GenerateRefreshToken(user.ID.Hex())

	config.DB.Collection("users").UpdateOne(
		context.Background(),
		bson.M{"_id": user.ID},
		bson.M{"$set": bson.M{"refreshToken": refresh}},
	)

	c.JSON(200, gin.H{
		"accessToken":  access,
		"refreshToken": refresh,
		"user":         user,
	})
}

// 👤 PROFILE
func GetProfile(c *gin.Context) {
	user, exists := c.Get("user")
	if !exists {
		c.JSON(500, gin.H{"message": "User not found"})
		return
	}

	c.JSON(200, gin.H{
		"user":    user,
		"message": "Profile fetched successfully",
	})
}

// 🔄 REFRESH TOKEN
func RefreshToken(c *gin.Context) {

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
	userID := claims["_id"].(string)

	objID, err := primitive.ObjectIDFromHex(userID)
	if err != nil {
		c.JSON(400, gin.H{"message": "Invalid user ID"})
		return
	}

	var user struct {
		RefreshToken string `bson:"refreshToken"`
	}

	err = config.DB.Collection("users").
		FindOne(context.Background(), bson.M{"_id": objID}).
		Decode(&user)

	if err != nil || user.RefreshToken != body.RefreshToken {
		c.JSON(401, gin.H{"message": "Token mismatch"})
		return
	}

	newAccess, _ := utils.GenerateAccessToken(userID)

	c.JSON(200, gin.H{
		"accessToken": newAccess,
	})
}

// 🚪 LOGOUT
func LogoutUser(c *gin.Context) {

	token := c.GetHeader("Authorization")

	if token == "" {
		c.JSON(400, gin.H{"message": "Token missing"})
		return
	}

	parts := strings.Split(token, " ")
	if len(parts) != 2 {
		c.JSON(400, gin.H{"message": "Invalid token format"})
		return
	}

	services.AddToBlacklist(parts[1])

	c.JSON(200, gin.H{
		"message": "User logged out successfully",
	})
}
