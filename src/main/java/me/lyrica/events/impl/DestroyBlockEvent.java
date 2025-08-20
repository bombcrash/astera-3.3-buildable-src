package me.lyrica.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyrica.events.Event;
import net.minecraft.util.math.BlockPos;

@AllArgsConstructor @Getter
public class DestroyBlockEvent extends Event {
    private final BlockPos position;
}
