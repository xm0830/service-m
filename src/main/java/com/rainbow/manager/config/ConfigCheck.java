package com.rainbow.manager.config;

import com.rainbow.manager.common.Result;

/**
 * Created by xuming on 2017/3/28.
 */
public class ConfigCheck {

    public static Result check(ServiceConfig config) {
        if (config.getId() == null || "".equals(config.getId().trim())) {
            return new Result(false, "发现id为空的服务");
        }

        if (config.getScript() == null || "".equals(config.getScript().trim())) {
            return new Result(false, "发现script为空的服务");
        }


        return new Result(true, "");
    }
}
