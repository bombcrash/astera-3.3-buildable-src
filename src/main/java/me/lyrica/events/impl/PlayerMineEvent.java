package me.lyrica.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyrica.events.Event;
import net.minecraft.util.math.BlockPos;

@Getter @AllArgsConstructor
public class PlayerMineEvent extends Event {
    private final int actorID;
    private final BlockPos position;
}
