package me.lyrica.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyrica.events.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@AllArgsConstructor @Getter
public class AttackBlockEvent extends Event {
    private final BlockPos position;
    private final Direction direction;
}
