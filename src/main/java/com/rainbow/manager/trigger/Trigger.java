package com.rainbow.manager.trigger;

/**
 * Created by xuming on 2017/3/28.
 */
public abstract class Trigger {

    private String alias = null;

    Trigger(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public abstract Action onTimeCycleEvent(Event e);
    public abstract Action onCompleteEvent(Event e);
}
