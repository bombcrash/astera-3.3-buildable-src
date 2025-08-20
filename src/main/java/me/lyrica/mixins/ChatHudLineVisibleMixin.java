package me.lyrica.mixins;

import me.lyrica.utils.mixins.IChatHudLineVisible;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.Visible.class)
public class ChatHudLineVisibleMixin implements IChatHudLineVisible {
    @Unique private boolean clientMessage = false;
    @Unique private String clientIdentifier = "";

    @Override
    public boolean lyrica$isClientMessage() {
        return clientMessage;
    }

    @Override
    public void lyrica$setClientMessage(boolean clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public String lyrica$getClientIdentifier() {
        return clientIdentifier;
    }

    @Override
    public void lyrica$setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }
}