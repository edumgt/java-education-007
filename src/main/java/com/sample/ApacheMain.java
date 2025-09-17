package com.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApacheMain {
    private static final Logger logger = LogManager.getLogger(ApacheMain.class);

    public static void main(String[] args) {
        logger.debug("디버그 메시지");
        logger.info("정보 메시지");
        logger.error("에러 메시지" );
    }
}
