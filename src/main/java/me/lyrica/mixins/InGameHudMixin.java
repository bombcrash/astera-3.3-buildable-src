package me.lyrica.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.lyrica.Lyrica;
import me.lyrica.events.impl.RenderOverlayEvent;
import me.lyrica.modules.impl.core.HUDModule;
import me.lyrica.modules.impl.visuals.CrosshairModule;
import me.lyrica.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render", at = @At("HEAD"))
    private void render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info) {
        if (client.options.hudHidden) return;
        Lyrica.EVENT_HANDLER.post(new RenderOverlayEvent(context, tickCounter.getTickDelta(true)));
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void renderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info) {
        if (Lyrica.MODULE_MANAGER != null && Lyrica.MODULE_MANAGER.getModule(HUDModule.class).isToggled() && Lyrica.MODULE_MANAGER.getModule(HUDModule.class).vanillaPotions.getValue().equalsIgnoreCase("Hide")) {
            info.cancel();
        }
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void renderPortalOverlay(DrawContext context, float nauseaStrength, CallbackInfo info) {
        if (Lyrica.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Lyrica.MODULE_MANAGER.getModule(NoRenderModule.class).portalOverlay.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
    private void renderVignetteOverlay(DrawContext context, Entity entity, CallbackInfo info) {
        if (Lyrica.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Lyrica.MODULE_MANAGER.getModule(NoRenderModule.class).vignette.getValue()) {
            info.cancel();
        }
    }

    @WrapWithCondition(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;F)V", ordinal = 0))
    private boolean renderPumpkinOverlay(InGameHud instance, DrawContext context, Identifier texture, float opacity) {
        return !(Lyrica.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Lyrica.MODULE_MANAGER.getModule(NoRenderModule.class).pumpkinOverlay.getValue());
    }

    @WrapWithCondition(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;F)V", ordinal = 1))
    private boolean renderSnowOverlay(InGameHud instance, DrawContext context, Identifier texture, float opacity) {
        return !(Lyrica.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Lyrica.MODULE_MANAGER.getModule(NoRenderModule.class).snowOverlay.getValue());
    }

    @Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
    private void renderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info) {
        if(Lyrica.MODULE_MANAGER.getModule(CrosshairModule.class).isToggled()) info.cancel();
    }
}
