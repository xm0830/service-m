package com.rainbow.manager.service;

import com.rainbow.manager.config.ServiceConfig;
import com.rainbow.manager.config.TriggerConfig;
import com.rainbow.manager.trigger.Action;
import com.rainbow.manager.trigger.Event;
import com.rainbow.manager.trigger.Trigger;
import com.rainbow.manager.trigger.TriggerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by xuming on 2017/4/10.
 */
public class Scheduler {
    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    private Map<String, ServiceConfig> allJobs = new HashMap<>();
    private Map<String, List<TriggerConfig> > allJobTriggers = new HashMap<>();

    private ArrayBlockingQueue<String> waitRunJobs = new ArrayBlockingQueue<>(1000);
    private ArrayBlockingQueue<String> completedJobs = new ArrayBlockingQueue<>(1000);
    private List<String> runningJobs = new ArrayList<>(20);

    private Thread timeCycleThread = new Thread(new TimeCycleThread());
    private Thread jobCompleteThread = new Thread(new JobCompleteThread());
    private Thread submitThread = new Thread(new JobSubmitter());
    private ExecutorService service = Executors.newFixedThreadPool(10);

    private String homeDir = null;

    public Scheduler(String homeDir) {
        this.homeDir = homeDir;
    }

    public void schedule(List<ServiceConfig> configs) {
        for (ServiceConfig config : configs) {
            String id = config.getId();
            allJobs.put(id, config);

            for (TriggerConfig triggerConfig : config.getTriggers()) {
                if (allJobTriggers.containsKey(id)) {
                    allJobTriggers.get(id).add(triggerConfig);
                } else {
                    List<TriggerConfig> values = new ArrayList<>();
                    values.add(triggerConfig);

                    allJobTriggers.put(id, values);
                }
            }
        }

        logger.info("启动时间循环监听器和服务完成监听器");
        timeCycleThread.start();
        jobCompleteThread.start();
        submitThread.start();
    }

    class TimeCycleThread implements Runnable {

        private TriggerManager manager = new TriggerManager();

        @Override
        public void run() {
            while (true) {
                for (Map.Entry<String, List<TriggerConfig>> entry : allJobTriggers.entrySet()) {
                    List<TriggerConfig> triggers = entry.getValue();

                    for (TriggerConfig triggerConfig : triggers) {
                        Trigger trigger = manager.getTrigger(triggerConfig.getType());

                        if (trigger != null) {
                            Action action = trigger.onTimeCycleEvent(new Event(entry.getKey(), null, triggerConfig));
                            addToWaitJobQueue(action, entry.getKey());
                        } else {
                            logger.error("未找到符合条件{}的触发器，服务ID：{}", triggerConfig.getType(), entry.getKey());
                        }
                    }
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    class JobCompleteThread implements Runnable {

        private TriggerManager manager = new TriggerManager();

        @Override
        public void run() {
            while (true) {
                try {
                    String id = completedJobs.take();
                    for (Map.Entry<String, List<TriggerConfig>> entry : allJobTriggers.entrySet()) {
                        List<TriggerConfig> triggers = entry.getValue();

                        for (TriggerConfig triggerConfig : triggers) {
                            Trigger trigger = manager.getTrigger(triggerConfig.getType());

                            if (trigger != null) {
                                Action action = trigger.onCompleteEvent(new Event(entry.getKey(), id, triggerConfig));
                                addToWaitJobQueue(action, entry.getKey());
                            } else {
                                logger.error("未找到符合条件 {} 的触发器，服务ID：{}", triggerConfig.getType(), entry.getKey());
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    logger.error("接收到线程中断异常：", e);
                }
            }
        }
    }

    class JobSubmitter implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    String jobId = waitRunJobs.take();
                    logger.info("获取待启动的服务：{}，当前待启动队列长度：{}", jobId, waitRunJobs.size());

                    runningJobs.add(jobId);
                    logger.info("添加服务到运行队列：{}，当前运行队列长度：{}", jobId, runningJobs.size());

                    Context context = new Context(allJobs.get(jobId), homeDir);
                    Launcher launcher = new Launcher(context);
                    Future<Integer> future = service.submit(launcher);
                    logger.info("开始运行服务: {}", jobId);

                    try {
                        runningJobs.remove(jobId);
                        if (future.get() == 0) {
                            logger.info("服务 {} 运行成功, 当前运行队列长度：{}", jobId, runningJobs.size());

                            completedJobs.put(jobId);
                        } else {
                            logger.error("服务 {} 运行失败, 当前运行队列长度：{}", jobId, runningJobs.size());
                        }
                    } catch (ExecutionException e) {
                        logger.error("执行服务异常：", e);
                    }
                } catch (InterruptedException e) {
                    logger.error("接收到线程中断异常：", e);
                }
            }
        }
    }

    private synchronized void addToWaitJobQueue(Action action, String id) {
        if (action == Action.Run) {
            try {
                waitRunJobs.put(id);
                logger.info("发现符合启动条件的服务：{}, 添加到待启动队列，当前待启动队列长度：{}", id, waitRunJobs.size());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (action == Action.Ignore) {

        }
    }
}
