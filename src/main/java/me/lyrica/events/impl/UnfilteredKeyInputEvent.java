package me.lyrica.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyrica.events.Event;

@Getter @AllArgsConstructor
public class UnfilteredKeyInputEvent extends Event {
    private final int key;
    private final int scancode;
    private final int action;
    private final int modifiers;
}
