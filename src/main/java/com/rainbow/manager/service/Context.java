package com.rainbow.manager.service;

import com.rainbow.manager.config.ServiceConfig;

import java.io.File;

/**
 * Created by xuming on 2017/5/8.
 */
public class Context {

    private ServiceConfig config = null;
    private String homeDir = null;

    public Context(ServiceConfig config, String homeDir) {
        this.config = config;
        this.homeDir = homeDir;
    }

    public String getServiceId() {
        return config.getId();
    }

    public String getScript() {
        return config.getScript();
    }

    public String getDataDir() {
        String dataDir = homeDir + "/data";
        File f = new File(dataDir);
        if (!f.exists()) {
            f.mkdir();
        }
        return dataDir;
    }

    public String getPkgDir() {
        return config.getPkgDir();
    }

    public String getLogDir() {
        String logDir = homeDir + "/log";
        File f = new File(logDir);
        if (!f.exists()) {
            f.mkdir();
        }
        return logDir;
    }
}
