package com.rainbow.manager.trigger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuming on 2017/3/28.
 */
public class TriggerManager {

    private Map<String, Trigger> triggerMap = new HashMap<>();

    public TriggerManager() {
        TimeTrigger timeTrigger = new TimeTrigger();
        CompleteTrigger completeTrigger = new CompleteTrigger();
        triggerMap.put(timeTrigger.getAlias(), timeTrigger);
        triggerMap.put(completeTrigger.getAlias(), completeTrigger);
    }

    public Trigger getTrigger(String type) {
        return triggerMap.get(type);
    }

}
