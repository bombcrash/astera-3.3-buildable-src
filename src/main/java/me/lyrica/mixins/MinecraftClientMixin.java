package me.lyrica.mixins;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.lyrica.Lyrica;
import me.lyrica.events.impl.GameLoopEvent;
import me.lyrica.events.impl.TickEvent;
import me.lyrica.modules.impl.combat.NoHitDelayModule;
import me.lyrica.modules.impl.miscellaneous.AutoRespawnModule;
import me.lyrica.modules.impl.player.FastPlaceModule;
import me.lyrica.modules.impl.player.MultiTaskModule;
import me.lyrica.utils.IMinecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements IMinecraft {
    @Shadow @Nullable public ClientPlayerEntity player;

    @Shadow private int itemUseCooldown;

    @Shadow @Final public GameOptions options;

    @Shadow public int attackCooldown;

    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setOverlay(Lnet/minecraft/client/gui/screen/Overlay;)V", shift = At.Shift.BEFORE))
    private void init(RunArgs args, CallbackInfo info) {
        Lyrica.onPostInitialize();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;runTasks()V", shift = At.Shift.AFTER))
    private void runTickHook(boolean tick, CallbackInfo info) {
        Lyrica.EVENT_HANDLER.post(new GameLoopEvent());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo info) {
        Lyrica.EVENT_HANDLER.post(new TickEvent());
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z", ordinal = 0, shift = At.Shift.BEFORE))
    private void doItemUse(CallbackInfo info) {
        if (Lyrica.MODULE_MANAGER != null && Lyrica.MODULE_MANAGER.getModule(FastPlaceModule.class).isToggled() && Lyrica.MODULE_MANAGER.getModule(FastPlaceModule.class).isValidItem(player.getMainHandStack().getItem())) {
            itemUseCooldown = Lyrica.MODULE_MANAGER.getModule(FastPlaceModule.class).ticks.getValue().intValue();
        }
    }

    @ModifyExpressionValue(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean handleBlockBreaking(boolean original) {
        if (Lyrica.MODULE_MANAGER != null && Lyrica.MODULE_MANAGER.getModule(MultiTaskModule.class).isToggled()) return false;
        return original;
    }

    @ModifyExpressionValue(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z"))
    private boolean handleInputEvents(boolean original) {
        if (Lyrica.MODULE_MANAGER != null && Lyrica.MODULE_MANAGER.getModule(MultiTaskModule.class).isToggled()) return false;
        return original;
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void setScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof DeathScreen && player != null && Lyrica.MODULE_MANAGER.getModule(AutoRespawnModule.class).isToggled()) {
            player.requestRespawn();
            info.cancel();
        }

        if (screen instanceof TitleScreen);
    }

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void doAttack(CallbackInfoReturnable<Boolean> info) {
        if (Lyrica.MODULE_MANAGER != null && Lyrica.MODULE_MANAGER.getModule(NoHitDelayModule.class).isToggled()) {
            attackCooldown = 0;
        }
    }
}
