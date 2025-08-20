package me.lyrica.settings.impl;

import lombok.Getter;
import me.lyrica.Lyrica;
import me.lyrica.events.impl.SettingChangeEvent;
import me.lyrica.settings.Setting;

@Getter
public class NumberSetting extends Setting {
    private Number value;
    private final Number defaultValue;
    private final Number minimum;
    private final Number maximum;

    public NumberSetting(String name, String description, Number value, Number minimum, Number maximum) {
        super(name, name, description, new Setting.Visibility());
        this.value = value;
        this.defaultValue = value;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public NumberSetting(String name, String tag, String description, Number value, Number minimum, Number maximum) {
        super(name, tag, description, new Setting.Visibility());
        this.value = value;
        this.defaultValue = value;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public NumberSetting(String name, String description, Setting.Visibility visibility, Number value, Number minimum, Number maximum) {
        super(name, name, description, visibility);
        this.value = value;
        this.defaultValue = value;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public NumberSetting(String name, String tag, String description, Setting.Visibility visibility, Number value, Number minimum, Number maximum) {
        super(name, tag, description, visibility);
        this.value = value;
        this.defaultValue = value;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public void setValue(Number value) {
        double v = value.doubleValue();
        double min = minimum.doubleValue();
        double max = maximum.doubleValue();
        if (v < min) v = min;
        if (v > max) v = max;
        switch (getType()) {
            case LONG -> this.value = (long) v;
            case DOUBLE -> this.value = v;
            case FLOAT -> this.value = (float) v;
            default -> this.value = (int) v;
        }
        Lyrica.EVENT_HANDLER.post(new SettingChangeEvent(this));
    }

    public void resetValue() {
        value = defaultValue;
    }

    public Type getType() {
        if (defaultValue.getClass() == Long.class) {
            return Type.LONG;
        } else if (defaultValue.getClass() == Double.class) {
            return Type.DOUBLE;
        } else if (defaultValue.getClass() == Float.class) {
            return Type.FLOAT;
        } else {
            return Type.INTEGER;
        }
    }

    public void increment() {
        switch (getType()) {
            case LONG -> setValue(getValue().longValue() + 1);
            case DOUBLE -> setValue(getValue().doubleValue() + 1);
            case FLOAT -> setValue(getValue().floatValue() + 1);
            default -> setValue(getValue().intValue() + 1);
        }
    }

    public void decrement() {
        switch (getType()) {
            case LONG -> setValue(getValue().longValue() - 1);
            case DOUBLE -> setValue(getValue().doubleValue() - 1);
            case FLOAT -> setValue(getValue().floatValue() - 1);
            default -> setValue(getValue().intValue() - 1);
        }
    }

    public static class Visibility extends Setting.Visibility {
        private final NumberSetting value;
        private final Number targetValue;
        private final Condition condition;

        public Visibility(NumberSetting value, Number targetValue, Condition condition) {
            super(value);
            this.value = value;
            this.targetValue = targetValue;
            this.condition = condition;
        }

        @Override
        public void update() {
            if (value.getVisibility() != null) {
                value.getVisibility().update();
                if (!value.getVisibility().isVisible()) {
                    setVisible(false);
                    return;
                }
            }

            if (value.getType() == NumberSetting.Type.INTEGER) setVisible(condition == Condition.EQUALS ? value.getValue().intValue() == targetValue.intValue() : condition == Condition.SMALLER ? value.getValue().intValue() < targetValue.intValue() : value.getValue().intValue() > targetValue.intValue());
            if (value.getType() == NumberSetting.Type.LONG) setVisible(condition == Condition.EQUALS ? value.getValue().longValue() == targetValue.longValue() : condition == Condition.SMALLER ? value.getValue().longValue() < targetValue.longValue() : value.getValue().longValue() > targetValue.longValue());
            if (value.getType() == NumberSetting.Type.DOUBLE) setVisible(condition == Condition.EQUALS ? value.getValue().doubleValue() == targetValue.doubleValue() : condition == Condition.SMALLER ? value.getValue().doubleValue() < targetValue.doubleValue() : value.getValue().doubleValue() > targetValue.doubleValue());
            if (value.getType() == NumberSetting.Type.FLOAT) setVisible(condition == Condition.EQUALS ? value.getValue().floatValue() == targetValue.floatValue() : condition == Condition.SMALLER ? value.getValue().floatValue() < targetValue.floatValue() : value.getValue().floatValue() > targetValue.floatValue());
        }

        public enum Condition {
            EQUALS, SMALLER, BIGGER
        }
    }

    public enum Type {
        INTEGER, LONG, DOUBLE, FLOAT
    }
}
