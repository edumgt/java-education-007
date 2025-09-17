package com.sample;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

public class DeadlockPdfExample {
    private static final Logger logger = LogManager.getLogger(DeadlockPdfExample.class);

    private static final ReentrantLock lock1 = new ReentrantLock();
    private static final ReentrantLock lock2 = new ReentrantLock();

    public static void main(String[] args) {
        // 쓰레드 A
        Thread t1 = new Thread(() -> generatePdfWithLocks("Thread-A", lock1, lock2));
        // 쓰레드 B
        Thread t2 = new Thread(() -> generatePdfWithLocks("Thread-B", lock2, lock1));

        t1.start();
        t2.start();

        // Deadlock 모니터링 쓰레드
        Thread monitor = new Thread(() -> {
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            while (true) {
                long[] deadlockedThreads = bean.findDeadlockedThreads();
                if (deadlockedThreads != null) {
                    ThreadInfo[] infos = bean.getThreadInfo(deadlockedThreads);
                    logger.error("⚠️ Deadlock 감지됨! 교착 상태에 빠진 쓰레드:");
                    for (ThreadInfo info : infos) {
                        logger.error(" - {}", info.getThreadName());
                    }
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        monitor.setDaemon(true);
        monitor.start();
    }

    private static void generatePdfWithLocks(String threadName, ReentrantLock first, ReentrantLock second) {
        try {
            logger.info("{}: 첫 번째 lock 획득 시도", threadName);
            first.lock();
            Thread.sleep(500); // 일부러 지연 -> Deadlock 유발 확률 ↑

            logger.info("{}: 두 번째 lock 획득 시도", threadName);
            second.lock();

            // PDF 생성 (단순 내용)
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                File fontFile = new File("C:\\Windows\\Fonts\\malgun.ttf");
                var font = PDType0Font.load(document, fontFile);

                Calendar calendar = Calendar.getInstance();
                Date now = new Date();

                String text = threadName + " - " + String.format("%tF %tT", calendar, calendar);

                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                    cs.beginText();
                    cs.setFont(font, 14);
                    cs.newLineAtOffset(100, 700);
                    cs.showText(text);
                    cs.endText();
                }

                String filename = threadName + "_output.pdf";
                document.save(filename);
                logger.info("{}: PDF 생성 완료 -> {}", threadName, filename);
            }

        } catch (IOException | InterruptedException e) {
            logger.error("{}: 오류 발생", threadName, e);
        } finally {
            if (second.isHeldByCurrentThread()) second.unlock();
            if (first.isHeldByCurrentThread()) first.unlock();
        }
    }
}
