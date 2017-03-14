package com.incarcloud.rooster.gather;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App implements CommandLineRunner {
    public static void main(String[] args)
    {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 日志
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        System.out.println("Active log4j config file: " + config.getName());
        this._logger = LoggerFactory.getLogger(App.class);
        this._logger.info("appver: " + (new GitVer()).getVersion());

        // 一个简单的单线程阻塞式模型
        Conveyor conveyor = new Conveyor();
        conveyor.configFrom(this.aliYunConfig);
        conveyor.configTo(this.aliYunConfig);

        // 超级简单的保护机制
        while(true) {
            try {
                this._logger.warn("Conveyor start ...");
                conveyor.blockedTransport();
            }
            catch(Exception ex){
                StringBuilder sbError = new StringBuilder();
                Throwable exx = ex;
                while(exx != null){
                    if(exx != null) sbError.append("\n    ");
                    sbError.append(exx.toString());
                    for(StackTraceElement e: exx.getStackTrace()){
                        sbError.append("\n        ");
                        sbError.append(e.toString().trim());
                    }
                    exx = exx.getCause();
                }
                this._logger.error(sbError.toString());
                this._logger.warn("10秒后重启Conveyor");
                Thread.sleep(1000*10);
            }
        }
    }

    @Autowired
    private AliYunConfig aliYunConfig;

    private Logger _logger;
}
