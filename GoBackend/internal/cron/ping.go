package cron

import (
	"context"
	"log"
	"time"

	"uber/config"
)

const pingInterval = 10 * time.Minute

func StartPing() {
	go func() {
		ticker := time.NewTicker(pingInterval)
		defer ticker.Stop()

		for {
			<-ticker.C
			ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
			err := config.DB.Client().Ping(ctx, nil)
			cancel()
			if err != nil {
				log.Println("⚠️ Ping failed:", err)
				continue
			}
			log.Println("✅ Ping ok")
		}
	}()
}
