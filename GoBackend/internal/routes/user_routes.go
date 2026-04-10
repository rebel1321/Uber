package routes

import (
	"uber/internal/controllers"
	"uber/internal/middleware"

	"github.com/gin-gonic/gin"
)

func RegisterUserRoutes(rg *gin.RouterGroup) {
	users := rg.Group("/user")
	{
		users.POST("/register", controllers.RegisterUser)
		users.POST("/login", controllers.LoginUser)
		users.POST("/refresh", controllers.RefreshToken)

		users.GET("/profile", middleware.AuthMiddleware("users"), controllers.GetProfile)
		users.GET("/logout", middleware.AuthMiddleware("users"), controllers.LogoutUser)
	}
}