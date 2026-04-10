package server

import (
	"fmt"
	"log"
	"os"

	"uber/config"
	"uber/internal/middleware"
	"uber/internal/realtime"
	"uber/internal/cron"
	"uber/internal/routes"

	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
)

func Start() {
	// Load env
	err := godotenv.Load("config/.env")
	if err != nil {
		log.Println("⚠️ No .env file found - using platform environment variables")
	}


	// ✅ Start periodic ping
	cron.StartPing()
	// Connect DB
	config.ConnectDB()

	// Port
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	// ✅ Production mode
	gin.SetMode(gin.ReleaseMode)

	// ✅ Create router
	router := gin.Default()

	// ✅ Security (important)
	router.SetTrustedProxies([]string{"127.0.0.1"})

	// ✅ Apply CORS middleware
	router.Use(middleware.CORSMiddleware())

	// ✅ WebSocket endpoint
	router.GET("/ws", realtime.HandleWS)

	// ✅ Attach routes
	routes.SetupRoutes(router)

	fmt.Println("🚀 Server running on port", port)

	// Start server
	err = router.Run(":" + port)
	if err != nil {
		log.Fatal("❌ Server failed:", err)
	}
}