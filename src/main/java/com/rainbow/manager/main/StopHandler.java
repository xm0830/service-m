package com.rainbow.manager.main;

import com.rainbow.manager.service.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * Created by xuming on 2017/5/10.
 */
public class StopHandler implements SignalHandler {
    public static final Logger logger = LoggerFactory.getLogger(StopHandler.class);

    private Scheduler scheduler = null;
    private Thread thread = null;

    public StopHandler(Scheduler scheduler, Thread main) {
        this.scheduler = scheduler;
        this.thread = main;
    }


    @Override
    public void handle(Signal signal) {
        logger.info("接收到进程停止信号");
        scheduler.stop();
        thread.interrupt();
    }
}
