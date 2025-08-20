package me.lyrica.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.lyrica.Lyrica;
import me.lyrica.events.impl.PlayerTravelEvent;
import me.lyrica.modules.impl.movement.SafeWalkModule;
import me.lyrica.modules.impl.movement.SpeedModule;
import me.lyrica.modules.impl.movement.VelocityModule;
import me.lyrica.modules.impl.player.ReachModule;
import me.lyrica.utils.IMinecraft;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IMinecraft {
    private final ReachModule reachModule = new ReachModule();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyReturnValue(method = "isPushedByFluids", at = @At("RETURN"))
    private boolean isPushedByFluids(boolean original) {
        if ((Object) this == mc.player && Lyrica.MODULE_MANAGER.getModule(VelocityModule.class).isToggled() && Lyrica.MODULE_MANAGER.getModule(VelocityModule.class).antiLiquidPush.getValue()) return false;
        return original;
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V", shift = At.Shift.AFTER))
    private void attack(CallbackInfo callbackInfo) {
        // KeepSprintModule ile ilgili kod kaldırıldı
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void travel(Vec3d movementInput, CallbackInfo info) {
        PlayerTravelEvent event = new PlayerTravelEvent(movementInput);
        Lyrica.EVENT_HANDLER.post(event);

        if (event.isCancelled()) {
            move(MovementType.SELF, getVelocity());
            info.cancel();
        }
    }

    @Inject(method = "getBlockInteractionRange", at = @At("HEAD"), cancellable = true)
    private void getBlockInteractionRange(CallbackInfoReturnable<Double> info) {
        if (reachModule.isToggled()) {
            info.setReturnValue(reachModule.amount.getValue().doubleValue());
        }
    }

    @Inject(method = "getEntityInteractionRange", at = @At("HEAD"), cancellable = true)
    private void getEntityInteractionRange(CallbackInfoReturnable<Double> info) {
        if (reachModule.isToggled()) {
            info.setReturnValue(reachModule.amount.getValue().doubleValue());
        }
    }

    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    private void clipAtLedge(CallbackInfoReturnable<Boolean> info) {
        if (Lyrica.MODULE_MANAGER.getModule(SafeWalkModule.class).isToggled()) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "getMovementSpeed", at = @At("HEAD"), cancellable = true)
    private void getMovementSpeed(CallbackInfoReturnable<Float> info) {
        if (Lyrica.MODULE_MANAGER.getModule(SpeedModule.class).isToggled() && Lyrica.MODULE_MANAGER.getModule(SpeedModule.class).mode.getValue().equalsIgnoreCase("Vanilla")) {
            info.setReturnValue(Lyrica.MODULE_MANAGER.getModule(SpeedModule.class).vanillaSpeed.getValue().floatValue());
        }
    }
}
