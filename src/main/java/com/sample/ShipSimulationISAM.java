package com.sample;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ShipSimulationISAM {

    private static final String DATA_FILE = "ship_data.dat";
    private static final String INDEX_FILE = "ship_index.dat";
    private static final int RECORD_SIZE = 80;

    // 거리 계산 (Haversine formula)
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    // 배 위치 저장
    public static synchronized void insertShipRecord(String shipId, double lat, double lon, int targetPortId) throws IOException {
        RandomAccessFile dataFile = new RandomAccessFile(DATA_FILE, "rw");
        RandomAccessFile indexFile = new RandomAccessFile(INDEX_FILE, "rw");

        long pos = dataFile.length();
        dataFile.seek(pos);

        String shipStr = String.format("%-10s", shipId);
        String latStr = String.format("%15.6f", lat);
        String lonStr = String.format("%15.6f", lon);
        String portStr = String.format("%05d", targetPortId);

        String record = shipStr + "|" + latStr + "|" + lonStr + "|" + portStr;
        record = String.format("%-" + RECORD_SIZE + "s", record);

        dataFile.write(record.getBytes(StandardCharsets.UTF_8));
        indexFile.seek(indexFile.length());
        indexFile.write((shipId + "," + pos + "\n").getBytes(StandardCharsets.UTF_8));

        dataFile.close();
        indexFile.close();
    }

    // -----------------------------
    // Ship 클래스 (Runnable)
    // -----------------------------
    static class Ship implements Runnable {
        private final String shipId;
        private final double[][] route;
        private final int[] portIds;
        private final double speed;

        public Ship(String shipId, double[][] route, int[] portIds, double speed) {
            this.shipId = shipId;
            this.route = route;
            this.portIds = portIds;
            this.speed = speed;
        }

        @Override
        public void run() {
            try {
                while (true) { // 무한 루프 → 계속 운항
                    for (int i = 0; i < route.length - 1; i++) {
                        double lat1 = route[i][0];
                        double lon1 = route[i][1];
                        double lat2 = route[i+1][0];
                        double lon2 = route[i+1][1];

                        double totalDist = distance(lat1, lon1, lat2, lon2);
                        int steps = (int) Math.ceil(totalDist / speed);

                        for (int step = 0; step < steps; step++) {
                            double ratio = Math.min(1.0, (step * speed) / totalDist);
                            double lat = lat1 + (lat2 - lat1) * ratio;
                            double lon = lon1 + (lon2 - lon1) * ratio;

                            insertShipRecord(shipId, lat, lon, portIds[i+1]);

                            System.out.printf("[%s] %d0s → %.4f, %.4f (to %d)%n",
                                    shipId, step, lat, lon, portIds[i+1]);

                            Thread.sleep(10000); // 10초마다 이동
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // -----------------------------
    // Main (배 여러 척 실행 가능)
    // -----------------------------
    public static void main(String[] args) {
        // 항로 정의
        double[][] route = {
            {35.6762, 139.6503},   // Tokyo
            {33.7406, -118.2760},  // Los Angeles
            {19.1738, -96.1342}    // Veracruz
        };
        int[] portIds = {2001, 1001, 3001};

        // Ship 인스턴스 실행
        Ship ship1 = new Ship("SHIP01", route, portIds, 3000.0);
        Thread t1 = new Thread(ship1);
        t1.start();

        // 배를 더 추가하고 싶으면 여기서 또 실행 가능
        // Ship ship2 = new Ship("SHIP02", route, portIds, 2500.0);
        // new Thread(ship2).start();
    }
}
