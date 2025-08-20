package me.lyrica.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.lyrica.Lyrica;
import me.lyrica.events.impl.ChatInputEvent;
import me.lyrica.events.impl.CommandInputEvent;
import me.lyrica.modules.impl.core.HUDModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;
import me.lyrica.commands.Command;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    private int chatPosX = 10, chatPosY = 10;
    private boolean dragging = false;
    //@Shadow private TextFieldWidget messageField;

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "sendMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendChatMessage(Ljava/lang/String;)V"), cancellable = true)
    private void sendMessage(String chatText, boolean addToHistory, CallbackInfo info) {
        ChatInputEvent event = new ChatInputEvent(chatText);
        Lyrica.EVENT_HANDLER.post(event);
        if (event.isCancelled()) info.cancel();
    }

    @Inject(method = "sendMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendChatCommand(Ljava/lang/String;)V"), cancellable = true)
    private void sendCommand(String chatText, boolean addToHistory, CallbackInfo info) {
        CommandInputEvent event = new CommandInputEvent(chatText);
        Lyrica.EVENT_HANDLER.post(event);
        if (event.isCancelled()) info.cancel();
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
    private boolean render(DrawContext instance, int x1, int y1, int x2, int y2, int color) {
        return !Lyrica.MODULE_MANAGER.getModule(HUDModule.class).isToggled();
    }
}
