package me.lyrica.modules.impl.movement;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.PlayerUpdateEvent;
import me.lyrica.mixins.accessors.ClientPlayerEntityAccessor;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.ModeSetting;
import net.minecraft.entity.effect.StatusEffects;

@RegisterModule(name = "Sprint", description = "Makes it so that you are always sprinting when possible.", category = Module.Category.MOVEMENT)
public class SprintModule extends Module {
    public static SprintModule INSTANCE;
    
    public SprintModule() {
        INSTANCE = this;
    }

    public ModeSetting mode = new ModeSetting("Mode", "The limits to when you can be sprinting.", "Rage", new String[]{"Legit", "Rage"});

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (mc.player == null) return;

        if (shouldSprint()) {
            mc.player.setSprinting(true);
        }
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        mc.player.setSprinting(shouldSprint());
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.setSprinting(false);
    }

    @Override
    public String getMetaData() {
        return mode.getValue();
    }

    public boolean shouldSprint() {
        if (!((ClientPlayerEntityAccessor) mc.player).invokeCanSprint()) return false;
        if (mc.player.isTouchingWater() && !mc.player.isSubmergedInWater()) return false;
        if (mc.player.isSwimming() && !mc.player.isOnGround() && !mc.player.input.playerInput.sneak() && !mc.player.isTouchingWater()) return false;

        if (mode.getValue().equalsIgnoreCase("Rage")) {
            return mc.player.isSubmergedInWater() ? (mc.player.input.playerInput.forward() || mc.player.input.playerInput.backward() || mc.player.input.playerInput.left() || mc.player.input.playerInput.right()) : (mc.player.input.movementForward >= 0.8 || mc.player.input.movementForward <= -0.8 || mc.player.input.movementSideways >= 0.8 || mc.player.input.movementSideways <= -0.8);
        } else {
            if (!((ClientPlayerEntityAccessor) mc.player).invokeIsWalking()) return false;
            if (mc.player.isUsingItem() && (!Lyrica.MODULE_MANAGER.getModule(NoSlowModule.class).isToggled() || !Lyrica.MODULE_MANAGER.getModule(NoSlowModule.class).items.getValue())) return false;
            if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) return false;
            if (mc.player.isGliding()) return false;
            if (mc.player.horizontalCollision && !mc.player.collidedSoftly) return false;
            return mc.player.input.hasForwardMovement();
        }
    }
}
