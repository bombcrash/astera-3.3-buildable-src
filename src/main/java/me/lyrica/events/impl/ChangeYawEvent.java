package me.lyrica.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyrica.events.Event;

@Getter @AllArgsConstructor
public class ChangeYawEvent extends Event {
    private final float yaw;
}
