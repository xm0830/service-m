package com.rainbow.manager.service;

import com.rainbow.manager.common.MailSender;
import com.rainbow.manager.config.EmailConfig;
import com.rainbow.manager.config.ServiceConfig;
import com.rainbow.manager.config.TriggerConfig;
import com.rainbow.manager.trigger.Action;
import com.rainbow.manager.trigger.Event;
import com.rainbow.manager.trigger.Trigger;
import com.rainbow.manager.trigger.TriggerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
    private final List<String> runningJobs = Collections.synchronizedList(new ArrayList<String>(20));
    private final Map<String, Future<Integer>> futures = Collections.synchronizedMap(new HashMap<String, Future<Integer>>());

    private Thread timeCycleThread = new Thread(new TimeCycleThread(), "TimeCycleThread");
    private Thread jobCompleteThread = new Thread(new JobCompleteThread(), "JobCompleteThread");
    private Thread submitThread = new Thread(new JobSubmitter(), "JobSubmitThread");
    private Thread statusThread = new Thread(new StatusTracker(), "StatusTrackerThread");
    private ExecutorService service = Executors.newFixedThreadPool(10);

    private String homeDir = null;
    private boolean isStopped = false;

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
        statusThread.start();

        logger.info("正在等待调度符合运行条件的服务...");
    }

    public void stop() {
        isStopped = true;

        timeCycleThread.interrupt();
        jobCompleteThread.interrupt();
        submitThread.interrupt();

        service.shutdown();

        while (!service.isTerminated() || timeCycleThread.isAlive() || jobCompleteThread.isAlive() || submitThread.isAlive() || statusThread.isAlive()) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
        }
    }

    class TimeCycleThread implements Runnable {

        private TriggerManager manager = new TriggerManager();

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
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
                    logger.warn("接收到线程中断异常, 准备退出");
                    Thread.currentThread().interrupt();
                }
            }

        }
    }

    class JobCompleteThread implements Runnable {

        private TriggerManager manager = new TriggerManager();

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
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
                    logger.warn("接收到线程中断异常, 准备退出");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    class JobSubmitter implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String jobId = waitRunJobs.take();
                    runningJobs.add(jobId);

                    Context context = new Context(allJobs.get(jobId), homeDir);
                    Launcher launcher = new Launcher(context);
                    Future<Integer> future = service.submit(launcher);
                    logger.info("开始运行服务: {}, 当前待启动服务数量: {}, 正在运行服务数量: {}", jobId, waitRunJobs.size(), runningJobs.size());

                    futures.put(jobId, future);
                } catch (InterruptedException e) {
                    logger.warn("接收到线程中断异常, 准备退出");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    class StatusTracker implements Runnable {

        @Override
        public void run() {
            List<String> rmIds = new ArrayList<>();
            while (true) {
                try {
                    for (Map.Entry<String, Future<Integer>> entry : futures.entrySet()) {
                        if (entry.getValue().isDone()) {
                            if (entry.getValue().get() == 0) {
                                runningJobs.remove(entry.getKey());
                                logger.info("服务 {} 运行成功, 当前待启动服务数量: {}, 正在运行服务数量: {}", entry.getKey(), waitRunJobs.size(), runningJobs.size());
                                completedJobs.put(entry.getKey());
                            } else {
                                runningJobs.remove(entry.getKey());
                                logger.error("服务 {} 运行失败, 当前待启动服务数量: {}, 正在运行服务数量: {}", entry.getKey(), waitRunJobs.size(), runningJobs.size());

                                EmailConfig config = allJobs.get(entry.getKey()).getEmail();
                                if (config.isEnable()) {
                                    MailSender sender = new MailSender(config);
                                    try {
                                        sender.send("服务：" + entry.getKey() + " 运行失败，请及时检查失败原因！");
                                        logger.info("完成向 {} 发送失败邮件通知！", config.getReceiveEmailUsers());
                                    } catch (Exception e) {
                                        logger.error("发送邮件通知失败", e);
                                    }
                                }
                            }

                            rmIds.add(entry.getKey());
                        }
                    }

                    if (rmIds.size() > 0) {
                        for (String rmId : rmIds) {
                            futures.remove(rmId);
                        }
                        rmIds.clear();
                    }

                    if (isStopped && futures.isEmpty()) {
                        break;
                    }

                    Thread.sleep(3000);
                } catch (ExecutionException e) {
                    logger.error("执行服务异常：", e);
                } catch (InterruptedException e) {
                    logger.error("接收到线程中断异常：", e);
                }
            }
        }
    }

    private void addToWaitJobQueue(Action action, String id) {
        if (action == Action.Run) {
            try {
                if (!waitRunJobs.contains(id) && !runningJobs.contains(id)) {
                    waitRunJobs.put(id);
                    logger.info("发现符合启动条件的服务：{}", id);
                } else {
                    logger.warn("服务：{} 已经待运行或者正在运行，忽略此次调度", id);
                }

            } catch (InterruptedException e) {
                logger.warn("接收到线程中断异常, 准备退出");
                Thread.currentThread().interrupt();
            }
        } else if (action == Action.Ignore) {

        }
    }
}
