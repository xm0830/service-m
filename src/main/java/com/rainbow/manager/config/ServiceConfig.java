package com.rainbow.manager.config;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuming on 2017/3/28.
 */
public class ServiceConfig {

    private String id = "";
    private String name = "";
    private String desc = "";
    private String script = "";
    private List<TriggerConfig> triggers = new ArrayList<>();

    @JSONField(serialize = false)
    private String pkgDir = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public List<TriggerConfig> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<TriggerConfig> triggers) {
        this.triggers = triggers;
    }

    public String getPkgDir() {
        return pkgDir;
    }

    public void setPkgDir(String pkgDir) {
        this.pkgDir = pkgDir;
    }
}
