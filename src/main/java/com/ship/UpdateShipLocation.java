package com.ship;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

public class UpdateShipLocation {
    public static void main(String[] args) throws InterruptedException {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("testdb");
            MongoCollection<Document> ships = database.getCollection("ships");

            String shipName = "Evergreen-1";

            // 무한 루프 (Ctrl+C로 종료)
            while (true) {
                // 현재 좌표 가져오기
                Document ship = ships.find(Filters.eq("name", shipName)).first();
                if (ship == null) {
                    System.out.println("❌ Ship not found: " + shipName);
                    break;
                }

                double currentLat = ship.getDouble("lat");
                double currentLon = ship.getDouble("lon");

                // 2000km 이동 → 약 18° 경도 증가
                double newLat = currentLat;          // 위도는 그대로
                double newLon = currentLon + 18.0;   // 동쪽으로 이동

                // 경도가 ±180을 넘어가면 wrap-around 처리
                if (newLon > 180) newLon -= 360;
                if (newLon < -180) newLon += 360;

                // DB 업데이트
                ships.updateOne(
                    Filters.eq("name", shipName),
                    Updates.combine(
                        Updates.set("lat", newLat),
                        Updates.set("lon", newLon),
                        Updates.set("status", "en route")
                    )
                );

                System.out.printf("✅ %s 위치 업데이트: lat=%.2f, lon=%.2f%n", shipName, newLat, newLon);

                // 5초 대기
                Thread.sleep(5000);
            }
        }
    }
}
