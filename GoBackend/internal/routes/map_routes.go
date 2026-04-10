package routes

import (
	"uber/internal/controllers"
	"uber/internal/middleware"

	"github.com/gin-gonic/gin"
)

func RegisterMapRoutes(rg *gin.RouterGroup) {

	maps := rg.Group("/maps")
	{
		maps.GET("/get-coordinates",
			middleware.AuthMiddleware("users"),
			controllers.GetCoordinates,
		)

		maps.GET("/get-distance-time",
			middleware.AuthMiddleware("users"),
			controllers.GetDistanceTime,
		)

		maps.GET("/get-suggestions",
			middleware.AuthMiddleware("users"),
			controllers.GetAutoCompleteSuggestions,
		)
	}
}