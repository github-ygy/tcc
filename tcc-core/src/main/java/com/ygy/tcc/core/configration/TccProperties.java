package com.ygy.tcc.core.configration;


import com.ygy.tcc.core.exception.TccException;
import com.ygy.tcc.core.logger.TccLogger;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

public class TccProperties {

    public static final String TCC_APP_ID_FIELD = "tcc_app_id_field";
    public static final String TRYING_STATUS_TRANSFER_TO_ROLL_BACK_TIME_SPAN_FIELD = "trying_status_transfer_to_roll_back_time_span_field";
    public static final String MAX_RECOVERY_TIMES_FIELD = "max_recovery_times_field";
    public static final String VALID_RECOVERY_TIME_SPAN_FIELD = "valid_recovery_time_span_field";
    public static final String TCC_RECOVERY_START_SWITCH_FIELD = "tcc_recovery_start_switch_field";

    public static final String TCC_RECOVERY_PERIOD_SECONDS_FIELD = "tcc_recovery_period_seconds_field";
    public static final String TCC_RECOVERY_INIT_DELAY_SECONDS_FIELD = "tcc_recovery_init_delay_seconds_field";

    private static final ConcurrentHashMap<String, String> props = new ConcurrentHashMap<>();

    public static void setProp(String key, String value) {
        props.put(key, value);
    }

    public static String getPropOrDefault(String key, String defaultValue) {
        return props.getOrDefault(key, defaultValue);
    }

    public static boolean getBooleanPropOrDefault(String key, boolean defaultValue) {
        String propsOrDefault = props.getOrDefault(key, "");
        if (StringUtils.isEmpty(propsOrDefault)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(propsOrDefault);
    }

    public static int getIntPropOrDefault(String key, int defaultValue) {
        String propsOrDefault = props.getOrDefault(key, "");
        if (StringUtils.isEmpty(propsOrDefault)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(propsOrDefault);
        } catch (Exception exception) {
            TccLogger.error("getIntPropOrDefault error", exception);
        }
        throw new TccException("getIntPropOrDefault error:" + key);
    }

    public static long getLongPropOrDefault(String key, long defaultValue) {
        String propsOrDefault = props.getOrDefault(key, "");
        if (StringUtils.isEmpty(propsOrDefault)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(propsOrDefault);
        } catch (Exception exception) {
            TccLogger.error("getLongPropOrDefault error", exception);
        }
        throw new TccException("getLongPropOrDefault error:" + key);
    }

    public static String getPrintProps() {
        return props.toString();
    }

    public static String getTccAppId() {
        return getPropOrDefault(TCC_APP_ID_FIELD, "");
    }
}
