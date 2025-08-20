package me.lyrica.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyrica.events.Event;

@Getter @AllArgsConstructor
public class UnfilteredMouseInputEvent extends Event {
    private final int button;
    private final int action;
    private final int mods;
}
