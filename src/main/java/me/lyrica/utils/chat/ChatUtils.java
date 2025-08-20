package me.lyrica.utils.chat;

import me.lyrica.Lyrica;
import me.lyrica.modules.impl.core.CommandsModule;
import me.lyrica.utils.text.FormattingUtils;
import net.minecraft.util.StringIdentifiable;

public class ChatUtils {
    public static StringIdentifiable getPrimary() {
        return FormattingUtils.getFormatting(Lyrica.MODULE_MANAGER.getModule(CommandsModule.class).primaryMessageColor.getValue());
    }

    public static StringIdentifiable getSecondary() {
        return FormattingUtils.getFormatting(Lyrica.MODULE_MANAGER.getModule(CommandsModule.class).secondaryMessageColor.getValue());
    }
}
