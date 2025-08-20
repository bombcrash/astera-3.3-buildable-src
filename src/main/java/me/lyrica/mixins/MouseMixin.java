package me.lyrica.mixins;

import me.lyrica.Lyrica;
import me.lyrica.events.impl.MouseInputEvent;
import me.lyrica.events.impl.UnfilteredMouseInputEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo info) {
        Lyrica.EVENT_HANDLER.post(new UnfilteredMouseInputEvent(button, action, mods));
        if (window == client.getWindow().getHandle() && action == 1 && client.currentScreen == null) {
            Lyrica.EVENT_HANDLER.post(new MouseInputEvent(button));
        }
    }
}
