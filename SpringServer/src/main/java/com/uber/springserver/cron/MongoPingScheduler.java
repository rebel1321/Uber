package com.uber.springserver.cron;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MongoPingScheduler {

    private final MongoTemplate mongoTemplate;

    public MongoPingScheduler(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Scheduled(fixedRate = 600000)
    public void pingMongo() {
        try {
            mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
            System.out.println("✅ Ping ok");
        } catch (Exception ex) {
            System.out.println("⚠️ Ping failed: " + ex.getMessage());
        }
    }
}
