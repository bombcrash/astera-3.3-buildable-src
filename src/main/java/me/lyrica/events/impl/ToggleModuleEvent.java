package me.lyrica.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyrica.events.Event;
import me.lyrica.modules.Module;

@AllArgsConstructor @Getter
public class ToggleModuleEvent extends Event {
    private final Module module;
    private final boolean state;
}
