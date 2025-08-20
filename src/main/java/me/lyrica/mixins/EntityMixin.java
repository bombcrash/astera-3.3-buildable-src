package me.lyrica.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.lyrica.Lyrica;
import me.lyrica.events.impl.ChangePitchEvent;
import me.lyrica.events.impl.ChangeYawEvent;
import me.lyrica.events.impl.UpdateVelocityEvent;
import me.lyrica.modules.impl.movement.NoSlowModule;
import me.lyrica.modules.impl.movement.VelocityModule;
import me.lyrica.utils.IMinecraft;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements IMinecraft {
    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void pushAwayFrom(Entity entity, CallbackInfo info) {
        if ((Object) this == mc.player && Lyrica.MODULE_MANAGER.getModule(VelocityModule.class).isToggled() && Lyrica.MODULE_MANAGER.getModule(VelocityModule.class).antiPush.getValue()) {
            info.cancel();
        }
    }

    @ModifyExpressionValue(method = "updateMovementInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getVelocity()Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d updateMovementInFluid(Vec3d vec3d) {
        if ((Object) this == mc.player && Lyrica.MODULE_MANAGER.getModule(VelocityModule.class).isToggled() && Lyrica.MODULE_MANAGER.getModule(VelocityModule.class).antiLiquidPush.getValue()) {
            return new Vec3d(0, 0, 0);
        }

        return vec3d;
    }

    @Inject(method = "updateVelocity", at = @At("HEAD"), cancellable = true)
    private void updateVelocity(float speed, Vec3d movementInput, CallbackInfo info) {
        if ((Object) this != mc.player) return;

        UpdateVelocityEvent event = new UpdateVelocityEvent(movementInput, speed);
        Lyrica.EVENT_HANDLER.post(event);
        if (event.isCancelled()) {
            info.cancel();
            mc.player.setVelocity(mc.player.getVelocity().add(event.getVelocity()));
        }
    }

    @Inject(method = "setYaw", at = @At("HEAD"), cancellable = true)
    private void setYaw(float yaw, CallbackInfo info) {
        if ((Object) this != mc.player) return;
        Lyrica.EVENT_HANDLER.post(new ChangeYawEvent(yaw));
    }

    @Inject(method = "setPitch", at = @At("HEAD"), cancellable = true)
    private void setPitch(float pitch, CallbackInfo info) {
        if ((Object) this != mc.player) return;
        Lyrica.EVENT_HANDLER.post(new ChangePitchEvent(pitch));
    }

    @ModifyExpressionValue(method = "getVelocityMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"))
    private Block getVelocityMultiplier(Block original) {
        if (Lyrica.MODULE_MANAGER.getModule(NoSlowModule.class).isToggled()) {
            if ((original == Blocks.SOUL_SAND && Lyrica.MODULE_MANAGER.getModule(NoSlowModule.class).soulSand.getValue()) || (original == Blocks.HONEY_BLOCK && Lyrica.MODULE_MANAGER.getModule(NoSlowModule.class).honeyBlocks.getValue())) {
                return Blocks.STONE;
            }
        }

        return original;
    }
}
