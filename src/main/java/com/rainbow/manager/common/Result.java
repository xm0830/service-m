package com.rainbow.manager.common;

/**
 * Created by xuming on 2017/3/28.
 */
public class Result {

    private boolean isSuccess = false;
    private String msg = "";

    public Result(boolean isSuccess, String msg) {
        this.isSuccess = isSuccess;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
