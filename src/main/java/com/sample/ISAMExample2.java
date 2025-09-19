package com.sample;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ISAMExample2 {
    private static final String DATA_FILE = "isam_data.dat";
    private static final String INDEX_FILE = "isam_index.dat";
    private static final int RECORD_SIZE = 110; // ID(5) + 항구명(70) + 위도(15) + 경도(15) + 구분자 포함

    // 데이터 저장
    public static void insertRecord(int id, String portName, double lat, double lon) throws IOException {
        RandomAccessFile dataFile = new RandomAccessFile(DATA_FILE, "rw");
        RandomAccessFile indexFile = new RandomAccessFile(INDEX_FILE, "rw");

        long pos = dataFile.length();
        dataFile.seek(pos);

        // ID
        String idStr = String.format("%05d", id);
        byte[] idBytes = idStr.getBytes(StandardCharsets.UTF_8);

        // 항구명
        byte[] nameBytes = portName.getBytes(StandardCharsets.UTF_8);
        byte[] nameFixed = new byte[70];
        System.arraycopy(nameBytes, 0, nameFixed, 0, Math.min(nameBytes.length, nameFixed.length));

        // 위도 경도
        String latStr = String.format("%15.6f", lat);
        String lonStr = String.format("%15.6f", lon);
        byte[] latBytes = latStr.getBytes(StandardCharsets.UTF_8);
        byte[] lonBytes = lonStr.getBytes(StandardCharsets.UTF_8);

        // 최종 레코드
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(idBytes);
        baos.write("|".getBytes(StandardCharsets.UTF_8));
        baos.write(nameFixed);
        baos.write("|".getBytes(StandardCharsets.UTF_8));
        baos.write(latBytes);
        baos.write("|".getBytes(StandardCharsets.UTF_8));
        baos.write(lonBytes);

        byte[] record = baos.toByteArray();
        if (record.length < RECORD_SIZE) {
            byte[] padded = new byte[RECORD_SIZE];
            System.arraycopy(record, 0, padded, 0, record.length);
            record = padded;
        }

        dataFile.write(record);

        // 인덱스 저장
        indexFile.seek(indexFile.length());
        indexFile.write((id + "," + pos + "\n").getBytes(StandardCharsets.UTF_8));

        dataFile.close();
        indexFile.close();
    }

    // 데이터 검색
    public static String findRecord(int id) throws IOException {
        RandomAccessFile dataFile = new RandomAccessFile(DATA_FILE, "r");
        BufferedReader indexReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(INDEX_FILE), StandardCharsets.UTF_8));

        String line;
        while ((line = indexReader.readLine()) != null) {
            String[] parts = line.split(",");
            int key = Integer.parseInt(parts[0]);
            long pos = Long.parseLong(parts[1]);

            if (key == id) {
                dataFile.seek(pos);
                byte[] buffer = new byte[RECORD_SIZE];
                dataFile.read(buffer);
                dataFile.close();
                indexReader.close();
                return new String(buffer, StandardCharsets.UTF_8).trim();
            }
        }
        dataFile.close();
        indexReader.close();
        return null;
    }

    public static void main(String[] args) throws IOException {
        // 미국
        insertRecord(1001, "Los Angeles", 33.7406, -118.2760);
        insertRecord(1002, "Long Beach", 33.7676, -118.1997);
        insertRecord(1003, "New York/New Jersey", 40.7128, -74.0060);

        // 일본
        insertRecord(2001, "도쿄", 35.6762, 139.6503);
        insertRecord(2002, "Yokohama", 35.4437, 139.6380);
        insertRecord(2005, "Osaka", 34.6937, 135.5023);

        // 멕시코
        insertRecord(3001, "Veracruz", 19.1738, -96.1342);
        insertRecord(3003, "Lázaro Cárdenas", 17.9582, -102.1930);

        // 검색 예시
        System.out.println("찾은 레코드: " + findRecord(2005)); // Osaka
        System.out.println("찾은 레코드: " + findRecord(3003)); // Lázaro Cárdenas
    }
}
