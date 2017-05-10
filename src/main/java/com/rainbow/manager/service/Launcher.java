package com.rainbow.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.Callable;

/**
 * Created by xuming on 2017/5/8.
 */
public class Launcher implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    private Context context = null;
    private LogWriter writer = null;

    public Launcher(Context context) {
        this.context = context;
        this.writer = new LogWriter(context.getLogDir(), context.getServiceId());
    }

    @Override
    public Integer call() throws Exception {
        InputStream errorStream = null;
        InputStream inputStream = null;
        Process process = null;
        try {
            StringBuilder newCmd = new StringBuilder();
            newCmd.append(context.getPkgDir()).append("/").append(context.getScript());
            newCmd.append(" ").append(context.getDataDir()).append("/").append(context.getServiceId()).append(".properties");
            process = Runtime.getRuntime().exec(newCmd.toString(), null, new File(context.getPkgDir()));
            inputStream = process.getInputStream();
            errorStream = process.getErrorStream();

            log(errorStream);
            log(inputStream);

            Thread.sleep(3000);

            int code = process.waitFor();

            process.destroy();

            return code;
        } catch (IOException e) {
            logger.error("提交服务失败: " + context.getServiceId(), e);
        } finally {
            if (errorStream != null) {
                errorStream.close();
            }

            if (inputStream != null) {
                inputStream.close();
            }

            if (writer != null) {
                writer.close();
            }
        }

        return 1;
    }

    private void log(final InputStream is) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = null;

                try {
                    while ((line = br.readLine()) != null) {
                        writer.write(line);
                    }
                } catch (IOException e) {
                    logger.warn("读取服务" + context.getServiceId() + "的运行日志失败: ", e);
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        logger.warn("关闭服务" + context.getServiceId() + "日志读取流失败: ", e);
                    }
                }
            }
        }).start();
    }

}
