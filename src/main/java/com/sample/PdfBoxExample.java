package com.sample;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class PdfBoxExample {
    // ✅ Logger 선언
    private static final Logger logger = LogManager.getLogger(PdfBoxExample.class);

    public static void main(String[] args) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            // 윈도우 기본 한글 폰트 (맑은 고딕)
            File fontFile = new File("C:\\Windows\\Fonts\\malgun.ttf");
            var font = PDType0Font.load(document, fontFile);

            Calendar calendar = Calendar.getInstance();
            Date now = new Date();

            String calendarStr = String.format("Calendar: %tF %tT", calendar, calendar);
            String dateStr = "Date: " + now.toString();
            String welcomeStr = "환영합니다";

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 14);
                contentStream.setLeading(20f);
                contentStream.newLineAtOffset(100, 700);

                contentStream.showText(calendarStr);
                contentStream.newLine();
                contentStream.showText(dateStr);
                contentStream.newLine();
                contentStream.showText(welcomeStr);

                contentStream.endText();
            }

            String outputFile = "example_korean.pdf";
            document.save(outputFile);

            // ✅ Logger 활용
            logger.info("PDF 생성 완료: {}", outputFile);

        } catch (IOException e) {
            logger.error("PDF 생성 중 오류 발생", e);
        }
    }
}
