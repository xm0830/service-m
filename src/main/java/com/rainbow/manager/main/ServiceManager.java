package com.rainbow.manager.main;

import com.rainbow.manager.common.Result;
import com.rainbow.manager.config.ConfigLoader;
import com.rainbow.manager.config.ServiceConfig;
import com.rainbow.manager.service.Scheduler;
import com.rainbow.manager.config.ConfigCheck;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by xuming on 2017/3/28.
 */
public class ServiceManager {

    public static Logger logger = null;

    public static void main(String[] args) {
        String homeDir = args[0];
        String confDir = homeDir + "/pkg";

        File logDir = new File(homeDir + "/log");
        if (!logDir.exists()) {
            logDir.mkdir();
        }
        try {
            System.setProperty("service.log.file", logDir.getCanonicalPath() + "/service_schedule_log");
        } catch (IOException e) {
            e.printStackTrace();
        }

        PropertyConfigurator.configure(ServiceManager.class.getClassLoader().getResource("log4j.properties"));
        logger = LoggerFactory.getLogger(ServiceManager.class);

        logger.info("开始加载所有服务");
        List<ServiceConfig> configs = null;
        try {
            configs = ConfigLoader.load(confDir);
        } catch (IOException e) {
            logger.error("加载服务出现异常", e);
            return;
        }

        logger.info("一共加载 {} 个服务配置文件，开始检查服务配置文件", configs.size());
        Result result = check(configs);

        if (!result.isSuccess()) {
            logger.error("服务配置文件出现异常: " + result.getMsg());
            return;
        }

        logger.info("开始调度加载完毕的服务");
        Scheduler scheduler = new Scheduler(homeDir);
        scheduler.schedule(configs);

        Thread mainThread = Thread.currentThread();
        Signal.handle(new Signal("TERM"), new StopHandler(scheduler, mainThread));

        while (!mainThread.isInterrupted()) {
            try {
                Thread.sleep(1000 * 10);
            } catch (InterruptedException e) {
                mainThread.interrupt();
            }
        }
    }

    private static Result check(List<ServiceConfig> configs) {
        Result result = new Result(true, null);
        Result temp = null;
        for (ServiceConfig config : configs) {
            temp = ConfigCheck.check(config);
            if (!temp.isSuccess()) {
                result.setSuccess(false);
                result.setMsg(temp.getMsg());

                break;
            }
        }

        return result;
    }
}
