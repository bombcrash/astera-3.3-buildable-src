package me.lyrica.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyrica.events.Event;
import net.minecraft.util.math.Vec3d;

@Getter @AllArgsConstructor
public class PlayerTravelEvent extends Event {
    private final Vec3d movementInput;
}
