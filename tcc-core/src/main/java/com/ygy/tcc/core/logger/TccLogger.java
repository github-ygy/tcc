package com.ygy.tcc.core.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TccLogger {

    private static final Logger logger = LoggerFactory.getLogger(TccLogger.class);

    public static void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    public static void info(String msg, Throwable e) {
        logger.info(msg, e);
    }

    public static void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    public static void warn(String msg, Throwable e) {
        logger.warn(msg, e);
    }

    public static void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    public static void debug(String msg, Throwable e) {
        logger.debug(msg, e);
    }

    public static void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    public static void error(String msg, Throwable e) {
        logger.error(msg, e);
    }

    private TccLogger() {
    }

}
