package me.lyrica.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyrica.events.Event;

import java.util.UUID;

@AllArgsConstructor @Getter
public class PlayerDisconnectEvent extends Event {
    private final UUID id;
}
