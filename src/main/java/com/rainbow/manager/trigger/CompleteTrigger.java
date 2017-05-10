package com.rainbow.manager.trigger;

/**
 * Created by xuming on 2017/5/8.
 */
public class CompleteTrigger extends Trigger {
    CompleteTrigger() {
        super("job");
    }

    @Override
    public Action onTimeCycleEvent(Event e) {
        return null;
    }

    @Override
    public Action onCompleteEvent(Event e) {
        String type = e.config.getType();
        String rule = e.config.getRule();
        if (type.equals(getAlias()) && rule.equals(e.pServiceId)) {
            return Action.Run;
        }
        return Action.Ignore;
    }
}
