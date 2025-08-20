package me.lyrica.events.impl;

import lombok.*;
import me.lyrica.events.Event;

@EqualsAndHashCode(callSuper = true) @Data
public class KeyInputEvent extends Event {
    private final int key, modifiers;
}
