package com.rainbow.manager.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

/**
 * Created by xuming on 2017/3/28.
 */
public class TimeTrigger extends Trigger {

    public static final Logger logger = LoggerFactory.getLogger(TimeTrigger.class);


    public TimeTrigger() {
        super("time");
    }

    @Override
    public Action onTimeCycleEvent(Event e) {
        String type = e.config.getType();
        String rule = e.config.getRule();

        rule = rule.replace("-", "");
        
        check(rule, e.serviceId);

        if (type.equals(getAlias())) {
            Calendar calendar = Calendar.getInstance();
            int nMonth = calendar.get(Calendar.MONTH)+1;
            int nDay = calendar.get(Calendar.DATE);
            int nHour = calendar.get(Calendar.HOUR_OF_DAY);
            int nMin = calendar.get(Calendar.MINUTE);
            int nWeek = calendar.get(Calendar.DAY_OF_WEEK)-1;
            int nSec = calendar.get(Calendar.SECOND);

            String monthStr = rule.substring(0, 2);
            String dayStr = rule.substring(2, 4);
            String hourStr = rule.substring(4, 6);
            String minStr = rule.substring(6, 8);
            String weekStr = rule.substring(8, 9);

            int month = getValue(monthStr, nMonth);
            int day = getValue(dayStr, nDay);
            int hour = getValue(hourStr, nHour);
            int min = getValue(minStr, nMin);
            int week = getValue(weekStr, nWeek);

            if (nMonth== month && nDay== day && nHour== hour && nMin== min && nWeek== week && nSec < 3) {
                return Action.Run;
            }
        }

        return Action.Ignore;
    }

    private int getValue(String value, int defaultValue) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (c == 'x' || c == 'X') {
                return defaultValue;
            }
        }

        return Integer.parseInt(value);
    }

    private boolean check(String rule, String id) {

        if (rule.length() != 9) {
            logger.error("错误格式的服务 {} 触发规则：{}", id, rule);
            return false;
        }

        String minStr = rule.substring(6, 8);
        if (minStr.toLowerCase().contains("x")) {
            logger.error("服务 {} 的触发规则 {} 中分钟部分不能是X或x", id, rule);
            return false;
        }

        boolean flag = true;
        for (int i = 0; i < rule.length() - 1; i++) {
            char c = rule.charAt(i);
            if (c != 'X' && c != 'x') {
                flag = false;
            }
        }

        if (flag) {
            logger.error("服务 {} 的触发规则 {} 不能全部是X或x", id, rule);
            return false;
        }

        return true;
    }

    @Override
    public Action onCompleteEvent(Event e) {
        return null;
    }

}
