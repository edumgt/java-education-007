package com.ship;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

public class UpdateShipLocation {
    public static void main(String[] args) {
        // 1. MongoDB 연결
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("testdb");
            MongoCollection<Document> ships = database.getCollection("ships");

            // 2. 업데이트할 선박 이름
            String shipName = "Evergreen-1";

            // 3. 새로운 좌표
            double newLat = 28.5;
            double newLon = -145.2;

            // 4. 업데이트 실행
            ships.updateOne(
                Filters.eq("name", shipName),
                Updates.combine(
                    Updates.set("lat", newLat),
                    Updates.set("lon", newLon),
                    Updates.set("status", "en route") // 상태도 갱신 가능
                )
            );

            System.out.println("✅ " + shipName + " 위치 업데이트 완료!");
        }
    }
}
