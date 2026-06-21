package com.uber.springserver.config;

import org.bson.Document;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Bean
    public ApplicationRunner mongoIndexInitializer(MongoTemplate mongoTemplate) {
        return args -> {
            mongoTemplate.getCollection("captains")
                    .createIndex(new Document("location", "2dsphere"));
            mongoTemplate.getCollection("blacklist")
                    .createIndex(new Document("createdAt", 1),
                            new com.mongodb.client.model.IndexOptions().expireAfter(86400L, java.util.concurrent.TimeUnit.SECONDS));
        };
    }
}
