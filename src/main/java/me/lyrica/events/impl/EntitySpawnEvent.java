package me.lyrica.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyrica.events.Event;
import net.minecraft.entity.Entity;

@AllArgsConstructor @Getter
public class EntitySpawnEvent extends Event {
    private final Entity entity;
}
