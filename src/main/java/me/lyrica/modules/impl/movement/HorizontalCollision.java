package me.lyrica.modules.impl.movement;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;

@RegisterModule(name = "HorizontalCollision", description = "Prevents you from colliding with blocks", category = Module.Category.MOVEMENT)
public class HorizontalCollision extends Module {
    public HorizontalCollision() {
    }
}