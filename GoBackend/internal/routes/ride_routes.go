package routes

import (
	"uber/internal/controllers"
	"uber/internal/middleware"

	"github.com/gin-gonic/gin"
)

func RegisterRideRoutes(rg *gin.RouterGroup) {

	rides := rg.Group("/ride")
	{
		rides.GET("/get-fare", middleware.AuthMiddleware("users"), controllers.GetFare)
		rides.POST("/create", middleware.AuthMiddleware("users"), controllers.CreateRide)
		rides.POST("/pay", middleware.AuthMiddleware("users"), controllers.PayRide)

		rides.POST("/confirm", middleware.AuthMiddleware("captains"), controllers.ConfirmRide)

		rides.GET("/start", middleware.AuthMiddleware("captains"), controllers.StartRide)

		rides.POST("/end", middleware.AuthMiddleware("captains"), controllers.EndRide)
	}

	legacy := rg.Group("/rides")
	{
		legacy.GET("/get-fare", middleware.AuthMiddleware("users"), controllers.GetFare)
		legacy.POST("/create", middleware.AuthMiddleware("users"), controllers.CreateRide)
		legacy.POST("/pay", middleware.AuthMiddleware("users"), controllers.PayRide)
		legacy.POST("/confirm", middleware.AuthMiddleware("captains"), controllers.ConfirmRide)
		legacy.GET("/start-ride", middleware.AuthMiddleware("captains"), controllers.StartRide)
		legacy.POST("/end-ride", middleware.AuthMiddleware("captains"), controllers.EndRide)
	}
}
