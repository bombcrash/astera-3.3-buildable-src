package me.lyrica.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyrica.events.Event;

@Getter
@AllArgsConstructor
public class MouseInputEvent extends Event {
    private final int button;
}