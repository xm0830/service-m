package com.rainbow.manager.config;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by xuming on 2017/3/28.
 */
public class ConfigLoader {
    public static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    public static List<ServiceConfig> load(String confDir) throws IOException {
        List<ServiceConfig> configs = new ArrayList<>();

        Set<String> confDirs = getConfDirs(confDir, 0);
        for (String dir : confDirs) {
            File[] files = new File(dir).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".json");
                }
            });

            if (files.length == 1) {
                logger.info("发现配置文件：" + files[0]);
                ServiceConfig config = JSON.parseObject(new FileInputStream(files[0]), ServiceConfig.class);
                config.setPkgDir(dir);
                if (configs.contains(config)) {
                    throw new RuntimeException("发现Id为同一个值：" + config.getId() + " 的多个服务！");
                }
                configs.add(config);
            } else {
                logger.error("目录应只包含一个json配置文件: " + dir);
            }
        }

        return configs;
    }

    private static Set<String> getConfDirs(String confDir, int depth) throws IOException {
        Set<String> dirs = new HashSet<>();
        File file = new File(confDir);
        File[] childFiles = file.listFiles();
        if (childFiles != null) {
            for (File childFile : childFiles) {
                if (childFile.isDirectory()) {
                    dirs.addAll(getConfDirs(childFile.getCanonicalPath(), depth + 1));
                } else {
                    if (depth != 0 && childFile.getCanonicalPath().endsWith(".json")) {
                        dirs.add(childFile.getParent());
                    }
                }
            }
        }

        return dirs;
    }
}
