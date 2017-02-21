package com.incarcloud.rooster;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.*;
import org.slf4j.*;

/**
 * 在启动之前调用
 */
public abstract class BaseTest {
    @BeforeClass
    public static void setup(){
        synchronized (s_locker) {
            if(s_logger != null) return;

            final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            final Configuration config = ctx.getConfiguration();
            System.out.println("Active log4j config file: " + config.getName());

            s_logger = LoggerFactory.getLogger("com.incarcloud.rooster.TEST");
        }
    }
    protected static Logger s_logger;
    private static Object s_locker = new Object();
}
