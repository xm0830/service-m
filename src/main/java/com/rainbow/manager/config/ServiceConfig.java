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
    private EmailConfig email = null;
    private List<TriggerConfig> triggers = null;

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

    public EmailConfig getEmail() {
        return email;
    }

    public void setEmail(EmailConfig email) {
        this.email = email;
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

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return id.equals(((ServiceConfig) obj).getId());
    }
}
