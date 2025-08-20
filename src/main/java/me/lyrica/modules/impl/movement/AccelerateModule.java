package me.lyrica.modules.impl.movement;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.PlayerMoveEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;  
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.utils.minecraft.MovementUtils;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;

@RegisterModule(name = "FastAccel", description = "Gives you more precise movement instantly.", category = Module.Category.MOVEMENT)
public class AccelerateModule extends Module {
    public BooleanSetting air = new BooleanSetting("NonTerrain", "Increases your speed while off ground.", true);
    public BooleanSetting speedInWater = new BooleanSetting("Water", "Increases your speed while in water.", false);

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent event) {
        if(getNull() || (Lyrica.MODULE_MANAGER.getModule(HoleSnapModule.class).isToggled() && Lyrica.MODULE_MANAGER.getModule(HoleSnapModule.class).hole != null) || Lyrica.MODULE_MANAGER.getModule(SpeedModule.class).isToggled()) return;

        if (mc.player.fallDistance >= 5.0f || mc.player.isSneaking() || mc.player.isClimbing() || mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.COBWEB || mc.player.getAbilities().flying || mc.player.isGliding())
            return;

        if(!mc.player.isOnGround() && !air.getValue()) return;

        if((mc.player.isTouchingWater() || mc.player.isInLava()) && !speedInWater.getValue()) return;

        Vector2d velocity = MovementUtils.forward(MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED));
        event.setMovement(new Vec3d(velocity.x, event.getMovement().getY(), event.getMovement().getZ()));
        event.setMovement(new Vec3d(event.getMovement().getX(), event.getMovement().getY(), velocity.y));
        event.setCancelled(true);
    }
}
