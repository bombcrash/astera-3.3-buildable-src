package me.lyrica.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyrica.events.Event;
import net.minecraft.entity.projectile.FireworkRocketEntity;

@Getter @AllArgsConstructor
public class RemoveFireworkEvent extends Event {
    private final FireworkRocketEntity entity;
}
