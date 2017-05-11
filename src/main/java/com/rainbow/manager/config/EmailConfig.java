package com.rainbow.manager.config;

/**
 * Created by xuming on 2017/5/11.
 */
public class EmailConfig {
    private boolean enable = false;
    private String protocol = null;
    private String server = null;
    private String sendEmailUser = null;
    private String sendEmailPwd = null;
    private String receiveEmailUsers = null;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getSendEmailUser() {
        return sendEmailUser;
    }

    public void setSendEmailUser(String sendEmailUser) {
        this.sendEmailUser = sendEmailUser;
    }

    public String getSendEmailPwd() {
        return sendEmailPwd;
    }

    public void setSendEmailPwd(String sendEmailPwd) {
        this.sendEmailPwd = sendEmailPwd;
    }

    public String getReceiveEmailUsers() {
        return receiveEmailUsers;
    }

    public void setReceiveEmailUsers(String receiveEmailUsers) {
        this.receiveEmailUsers = receiveEmailUsers;
    }
}
