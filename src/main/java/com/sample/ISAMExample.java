package com.sample;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ISAMExample {
    private static final String DATA_FILE = "isam_data.dat";
    private static final String INDEX_FILE = "isam_index.dat";
    private static final int RECORD_SIZE = 80; // ID + 항구명 고정 길이 (UTF-8)

    // 데이터 저장
    public static void insertRecord(int id, String portName) throws IOException {
        RandomAccessFile dataFile = new RandomAccessFile(DATA_FILE, "rw");
        RandomAccessFile indexFile = new RandomAccessFile(INDEX_FILE, "rw");

        long pos = dataFile.length();
        dataFile.seek(pos);

        // ID 5자리
        String idStr = String.format("%05d", id);
        byte[] idBytes = idStr.getBytes(StandardCharsets.UTF_8);

        // 항구명 UTF-8 바이트
        byte[] nameBytes = portName.getBytes(StandardCharsets.UTF_8);
        byte[] nameFixed = new byte[70]; // 항구명 70바이트 고정
        System.arraycopy(nameBytes, 0, nameFixed, 0, Math.min(nameBytes.length, nameFixed.length));

        // 최종 레코드
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(idBytes);
        baos.write("|".getBytes(StandardCharsets.UTF_8));
        baos.write(nameFixed);

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
        insertRecord(1001, "Los Angeles");
        insertRecord(1002, "Long Beach");
        insertRecord(1003, "New York/New Jersey");
        insertRecord(1004, "Savannah");
        insertRecord(1005, "Houston");
        insertRecord(1006, "Seattle");
        insertRecord(1007, "Oakland");
        insertRecord(1008, "Norfolk");
        insertRecord(1009, "Miami");
        insertRecord(1010, "Charleston");

        // 일본
        insertRecord(2001, "도쿄");
        insertRecord(2002, "Yokohama");
        insertRecord(2003, "Nagoya");
        insertRecord(2004, "Kobe");
        insertRecord(2005, "Osaka");
        insertRecord(2006, "Hakata (Fukuoka)");
        insertRecord(2007, "Shimizu");
        insertRecord(2008, "Hiroshima");
        insertRecord(2009, "Kitakyushu");
        insertRecord(2010, "Naha (Okinawa)");

        // 멕시코
        insertRecord(3001, "Veracruz");
        insertRecord(3002, "Manzanillo");
        insertRecord(3003, "Lázaro Cárdenas");
        insertRecord(3004, "Altamira");
        insertRecord(3005, "Tampico");
        insertRecord(3006, "Coatzacoalcos");
        insertRecord(3007, "Progreso");
        insertRecord(3008, "Mazatlán");
        insertRecord(3009, "Ensenada");
        insertRecord(3010, "Guaymas");

        // 검색 예시
        System.out.println("찾은 레코드: " + findRecord(2005)); // Osaka
        System.out.println("찾은 레코드: " + findRecord(3003)); // Lázaro Cárdenas
    }
}
