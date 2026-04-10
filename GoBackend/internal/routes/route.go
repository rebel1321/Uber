package routes

import "github.com/gin-gonic/gin"

func SetupRoutes(router *gin.Engine) {

	router.GET("/", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"message": "🚀 Uber API Server",
		})
	})

	router.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status": "healthy",
		})
	})

	api := router.Group("/api")
	{
		RegisterUserRoutes(api)
		RegisterCaptainRoutes(api)
		RegisterMapRoutes(api)
		RegisterRideRoutes(api)
	}
}
