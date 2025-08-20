package me.lyrica.mixins;

import me.lyrica.Lyrica;
import me.lyrica.modules.impl.core.FontModule;
import net.minecraft.client.font.Glyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Glyph.class)
public interface GlyphMixin {
    @Inject(method = "getShadowOffset", at = @At("HEAD"), cancellable = true)
    private void getShadowOffset(CallbackInfoReturnable<Float> info) {
        if (Lyrica.MODULE_MANAGER != null && Lyrica.MODULE_MANAGER.getModule(FontModule.class).isToggled() && !Lyrica.MODULE_MANAGER.getModule(FontModule.class).shadowMode.getValue().equalsIgnoreCase("Default")) {
            info.setReturnValue(Lyrica.FONT_MANAGER.getShadowOffset());
        }
    }
}
