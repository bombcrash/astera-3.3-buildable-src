package me.lyrica.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyrica.events.Event;
import me.lyrica.settings.Setting;

@Getter @AllArgsConstructor
public class SettingChangeEvent extends Event {
    private final Setting setting;
}
