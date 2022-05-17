package com.rasturize.anticheat.api.config;

import com.rasturize.anticheat.Apex;

import java.util.Arrays;

public enum ConfigValue {
    ALERT_TIMEOUT("alert.timeout", 500),
    REACH_CANCEL("reach.cancel", 3.0),
    REACH_BAN("reach.flag", 3.01),
    BLOCK_HITS_TIME("block.hits.time", 10),
    ANTICHEAT_NAME("anticheat.name", "&eApex"),
    ALERT_VIOLATIONS("alert.violations", "&7{vl}&8/&7{max}&8/&7{total}"),
    COMMAND_BAN("command.ban", "null"),
    COMMAND_ALERT("command.alert", "null"),;

    public String id;
    public Object value;
    public Class<?> type;

    ConfigValue(String id, Object value) {
        this.id = id;
        this.type = value.getClass();

        try {
            populate(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void populate(Object value) {
        if (Apex.storage.getString(id) == null) {
            Apex.storage.set(id, value);
        }
        if (value instanceof String) {
            this.value = Apex.storage.getString(id, (String) value);
        } else if (value instanceof Boolean) {
            this.value = Apex.storage.getBoolean(id, (Boolean) value);
        } else if (value instanceof Integer) {
            this.value = (int) Apex.storage.getInt(id, (Integer) value);
        } else if (value instanceof Double) {
            this.value = Apex.storage.getDouble(id, (Double) value);
        }
    }

    public String asString() {
        return (String) value;
    }

    public Boolean asBoolean() {
        return (Boolean) value;
    }

    public Double asDouble() {
        return (Double) value;
    }

    public Integer asInteger() {
        return (Integer) value;
    }

    public static ConfigValue get(String val) {
        return Arrays.stream(ConfigValue.values()).filter(value -> value.id.equalsIgnoreCase(val)).findFirst().orElse(null);
    }
}
