package me.lyrica.mixins;

import me.lyrica.Lyrica;
import me.lyrica.modules.impl.visuals.AtmosphereModule;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.Properties.class)
public class PropertiesMixin {
    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void getTimeOfDay(CallbackInfoReturnable<Long> info) {
        if (Lyrica.MODULE_MANAGER.getModule(AtmosphereModule.class).isToggled() && Lyrica.MODULE_MANAGER.getModule(AtmosphereModule.class).modifyTime.getValue()) {
            info.setReturnValue(Lyrica.MODULE_MANAGER.getModule(AtmosphereModule.class).time.getValue().longValue() * 100L);
        }
    }
}
