package me.lyrica.managers;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.TickEvent;
import me.lyrica.mixins.accessors.ChatHudAccessor;
import me.lyrica.modules.impl.core.CommandsModule;
import me.lyrica.modules.impl.miscellaneous.BetterChatModule;
import me.lyrica.utils.IMinecraft;
import me.lyrica.utils.chat.ChatUtils;
import me.lyrica.utils.mixins.IChatHudLine;
import me.lyrica.utils.mixins.IChatHudLineVisible;
import me.lyrica.utils.text.FormattingUtils;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class ChatManager implements IMinecraft {
    private final List<String> awaitMessages = new ArrayList<>();

    public ChatManager() {
        Lyrica.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.inGameHud == null) return;
        if (awaitMessages.isEmpty()) return;

        for (String message : new ArrayList<>(awaitMessages)) {
            addMessage(message);
            awaitMessages.remove(message);
        }
    }

    public void message(String message) {
        if (mc.player == null || mc.inGameHud == null) return;
        addMessage(getWatermark() + " " + ChatUtils.getSecondary() + message);
    }

    public void message(String message, String identifier) {
        if (mc.player == null || mc.inGameHud == null) return;
        deleteMessage(identifier);
        addMessage(getWatermark() + " " + ChatUtils.getSecondary() + message, identifier);
    }

    public void tagged(String message, String tag) {
        if (mc.player == null || mc.inGameHud == null) return;
        addMessage(getWatermark() + Formatting.DARK_AQUA + " [" + Formatting.AQUA + tag + Formatting.DARK_AQUA + "]: " + ChatUtils.getSecondary() + message);
    }

    public void tagged(String message, String tag, String identifier) {
        if (mc.player == null || mc.inGameHud == null) return;
        deleteMessage(identifier);
        addMessage(getWatermark() + Formatting.DARK_AQUA + " [" + Formatting.AQUA + tag + Formatting.DARK_AQUA + "]: " + ChatUtils.getSecondary() + message, identifier);
    }

    public void info$await(String message) {
        awaitMessages.add(getWatermark() + Formatting.DARK_BLUE + " [" + Formatting.BLUE + "?" + Formatting.DARK_BLUE + "] " + ChatUtils.getSecondary() + message);
    }

    public void info(String message) {
        if (mc.player == null || mc.inGameHud == null) return;
        addMessage(getWatermark() + Formatting.DARK_BLUE + " [" + Formatting.BLUE + "?" + Formatting.DARK_BLUE + "] " + ChatUtils.getSecondary() + message);
    }

    public void warn$await(String message) {
        awaitMessages.add(getWatermark() + Formatting.GOLD + " [" + Formatting.YELLOW + "!" + Formatting.GOLD + "] " + ChatUtils.getSecondary() + message);
    }

    public void warn(String message) {
        if (mc.player == null || mc.inGameHud == null) return;
        addMessage(getWatermark() + Formatting.GOLD + " [" + Formatting.YELLOW + "!" + Formatting.GOLD + "] " + ChatUtils.getSecondary() + message);
    }

    public void error$await(String message) {
        awaitMessages.add(getWatermark() + Formatting.DARK_RED + " [" + Formatting.RED + "!!" + Formatting.DARK_RED + "] " + ChatUtils.getSecondary() + message);
    }

    public void error(String message) {
        if (mc.player == null || mc.inGameHud == null) return;
        addMessage(getWatermark() + Formatting.DARK_RED + " [" + Formatting.RED + "!!" + Formatting.DARK_RED + "] " + ChatUtils.getSecondary() + message);
    }

    public void await(String message) {
        awaitMessages.add(getWatermark() + " " + ChatUtils.getSecondary() + message);
    }

    public void await(String message, String tag) {
        awaitMessages.add(getWatermark() + Formatting.DARK_AQUA + " [" + Formatting.AQUA + tag + Formatting.DARK_AQUA + "]: " + ChatUtils.getSecondary() + message);
    }

    public void addMessage(String message) {
        addTextMessage(Text.literal(message), "");
    }

    public void addMessage(String message, String identifier) {
        addTextMessage(Text.literal(message), identifier);
    }

    private void addTextMessage(Text message, String identifier) {
        ChatHudLine line = new ChatHudLine(mc.inGameHud.getTicks(), message, null, MessageIndicator.system());

        ((IChatHudLine) (Object) line).lyrica$setClientMessage(true);
        ((IChatHudLine) (Object) line).lyrica$setClientIdentifier(identifier);

        ((ChatHudAccessor) mc.inGameHud.getChatHud()).invokeLogChatMessage(line);
        ((ChatHudAccessor) mc.inGameHud.getChatHud()).invokeAddMessage(line);

        List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(line.content(), MathHelper.floor((double) mc.inGameHud.getChatHud().getWidth() / mc.inGameHud.getChatHud().getChatScale()), mc.textRenderer);
        for (int j = 0; j < list.size(); ++j) {
            OrderedText orderedText = list.get(j);

            if (mc.inGameHud.getChatHud().isChatFocused() && ((ChatHudAccessor) mc.inGameHud.getChatHud()).getScrolledLines() > 0) {
                ((ChatHudAccessor) mc.inGameHud.getChatHud()).setHasUnreadNewMessages(true);
                mc.inGameHud.getChatHud().scroll(1);
            }

            boolean bl2 = j == list.size() - 1;

            ChatHudLine.Visible visible = new ChatHudLine.Visible(line.creationTick(), orderedText, line.indicator(), bl2);

            ((IChatHudLineVisible) (Object) visible).lyrica$setClientMessage(true);
            ((IChatHudLineVisible) (Object) visible).lyrica$setClientIdentifier(identifier);

            ((ChatHudAccessor) mc.inGameHud.getChatHud()).getVisibleMessages().addFirst(visible);
            if (Lyrica.MODULE_MANAGER.getModule(BetterChatModule.class).isToggled() && Lyrica.MODULE_MANAGER.getModule(BetterChatModule.class).animation.getValue()) Lyrica.MODULE_MANAGER.getModule(BetterChatModule.class).getAnimationMap().put(visible, System.currentTimeMillis());
        }

        while (((ChatHudAccessor) mc.inGameHud.getChatHud()).getVisibleMessages().size() > 100) {
            ((ChatHudAccessor) mc.inGameHud.getChatHud()).getVisibleMessages().removeLast();
        }
    }

    public static void deleteMessage(String identifier) {
        try {
            ArrayList<ChatHudLine> removedLines = new ArrayList<>();
            for (ChatHudLine message : ((ChatHudAccessor) mc.inGameHud.getChatHud()).getMessages()) {
                if (!((IChatHudLine) (Object) message).lyrica$isClientMessage() || ((IChatHudLine) (Object) message).lyrica$getClientIdentifier().isEmpty()) continue;
                if (((IChatHudLine) (Object) message).lyrica$getClientIdentifier().equals(identifier)) {
                    removedLines.add(message);
                }
            }

            ArrayList<ChatHudLine.Visible> removedVisibleLines = new ArrayList<>();
            for (ChatHudLine.Visible message : ((ChatHudAccessor) mc.inGameHud.getChatHud()).getVisibleMessages()) {
                if (!((IChatHudLineVisible) (Object) message).lyrica$isClientMessage() || ((IChatHudLineVisible) (Object) message).lyrica$getClientIdentifier().isEmpty()) continue;
                if (((IChatHudLineVisible) (Object) message).lyrica$getClientIdentifier().equals(identifier)) {
                    removedVisibleLines.add(message);
                }
            }

            ((ChatHudAccessor) mc.inGameHud.getChatHud()).getMessages().removeAll(removedLines);
            ((ChatHudAccessor) mc.inGameHud.getChatHud()).getVisibleMessages().removeAll(removedVisibleLines);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private String getWatermark() {
        return getWatermark(null);
    }

    private String getWatermark(String text) {
        if (!Lyrica.MODULE_MANAGER.getModule(CommandsModule.class).watermark.getValue()) return "";
        return FormattingUtils.getFormatting(Lyrica.MODULE_MANAGER.getModule(CommandsModule.class).secondaryWatermarkColor.getValue()) + Lyrica.MODULE_MANAGER.getModule(CommandsModule.class).opening.getValue() + FormattingUtils.getFormatting(Lyrica.MODULE_MANAGER.getModule(CommandsModule.class).primaryWatermarkColor.getValue()) + (text == null ? Lyrica.MODULE_MANAGER.getModule(CommandsModule.class).watermarkText.getValue() : text) + FormattingUtils.getFormatting(Lyrica.MODULE_MANAGER.getModule(CommandsModule.class).secondaryWatermarkColor.getValue()) + Lyrica.MODULE_MANAGER.getModule(CommandsModule.class).closing.getValue();
    }
}
