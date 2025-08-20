package me.lyrica.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.lyrica.events.Event;

@AllArgsConstructor @Getter @Setter
public class KeyboardTickEvent extends Event {
    private float movementForward;
    private float movementSideways;
}
