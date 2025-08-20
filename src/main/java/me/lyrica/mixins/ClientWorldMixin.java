package me.lyrica.mixins;

import me.lyrica.Lyrica;
import me.lyrica.events.impl.EntitySpawnEvent;
import me.lyrica.modules.impl.visuals.AtmosphereModule;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void getSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Integer> info) {
        if (Lyrica.MODULE_MANAGER.getModule(AtmosphereModule.class).isToggled() && Lyrica.MODULE_MANAGER.getModule(AtmosphereModule.class).modifyFog.getValue()) {
            info.setReturnValue(Lyrica.MODULE_MANAGER.getModule(AtmosphereModule.class).fogColor.getColor().getRGB());
        }
    }

    @Inject(method = "addEntity", at = @At(value = "HEAD"))
    private void addEntity(Entity entity, CallbackInfo info) {
        Lyrica.EVENT_HANDLER.post(new EntitySpawnEvent(entity));
    }
}
