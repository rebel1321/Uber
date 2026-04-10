package routes

import (
	"uber/internal/controllers"
	"uber/internal/middleware"

	"github.com/gin-gonic/gin"
)

func RegisterCaptainRoutes(rg *gin.RouterGroup) {

	captains := rg.Group("/captain")
	{
		captains.POST("/register", controllers.RegisterCaptain)
		captains.POST("/login", controllers.LoginCaptain)
		captains.POST("/refresh", controllers.RefreshCaptainToken)
		captains.GET("/profile", middleware.AuthMiddleware("captains"), controllers.GetCaptainProfile)
		captains.GET("/logout", middleware.AuthMiddleware("captains"), controllers.LogoutCaptain)
	}
}