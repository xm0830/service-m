package com.rainbow.manager.trigger;

import com.rainbow.manager.config.TriggerConfig;

/**
 * Created by xuming on 2017/5/9.
 */
public class Event {
    public String serviceId = null;
    public String pServiceId = null;
    public TriggerConfig config = null;

    public Event(String serviceId, String pServiceId, TriggerConfig config) {
        this.serviceId = serviceId;
        this.pServiceId = pServiceId;
        this.config = config;
    }
}
