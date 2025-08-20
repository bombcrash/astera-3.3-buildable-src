package me.lyrica.mixins;


import me.lyrica.modules.impl.visuals.NoRenderModule;
import me.lyrica.modules.impl.visuals.AmbienceModule;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.Color;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @Shadow @Final private SimpleFramebuffer lightmapFramebuffer;

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/SimpleFramebuffer;endWrite()V", shift = At.Shift.BEFORE))
    private void update$endWrite(float delta, CallbackInfo info) {
        
        if (AmbienceModule.INSTANCE.isToggled() && AmbienceModule.INSTANCE.worldColor.getValue() != null && AmbienceModule.INSTANCE.worldColor.getVisibility().isVisible()) {
            Color c = AmbienceModule.INSTANCE.worldColor.getColor();
            lightmapFramebuffer.setClearColor(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0f);
            lightmapFramebuffer.clear();
        }
    }

    @Inject(method = "getDarknessFactor(F)F", at = @At("HEAD"), cancellable = true)
    private void getDarknessFactor(float delta, CallbackInfoReturnable<Float> info) {
        if (NoRenderModule.INSTANCE.isToggled() && NoRenderModule.INSTANCE.blindness.getValue()) {
            info.setReturnValue(0.0f);
        }
    }
}
