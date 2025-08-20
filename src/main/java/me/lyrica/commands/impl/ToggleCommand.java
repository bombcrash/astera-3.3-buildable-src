package me.lyrica.commands.impl;

import me.lyrica.Lyrica;
import me.lyrica.commands.Command;
import me.lyrica.commands.RegisterCommand;
import me.lyrica.modules.Module;
import me.lyrica.settings.Setting;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.utils.chat.ChatUtils;
import net.minecraft.util.Formatting;

@RegisterCommand(name = "toggle", tag = "Toggle", description = "Toggles a specified module or a setting on and off.", syntax = "<[module]> | <[module]> <[setting]>", aliases = {"t"})
public class ToggleCommand extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length == 1 || args.length == 2) {
            Module module = Lyrica.MODULE_MANAGER.getModule(args[0]);
            if (module == null) {
                Lyrica.CHAT_MANAGER.tagged("Could not find the module specified.", getTag(), getName());
                return;
            }

            if (args.length == 1) {
                if (module.isPersistent()) {
                    Lyrica.CHAT_MANAGER.tagged("Cannot toggle a persistent module.", getTag(), getName());
                    return;
                }

                module.setToggled(!module.isToggled(), false);
                Lyrica.CHAT_MANAGER.tagged(ChatUtils.getPrimary() + module.getName() + ChatUtils.getSecondary() + " has been toggled " + (module.isToggled() ? Formatting.GREEN + "on" : Formatting.RED + "off") + ChatUtils.getSecondary() + ".", getTag(), getName() + "-cmd-" + module.getName());
            }

            if (args.length == 2) {
                Setting setting = module.getSetting(args[1]);
                if (setting == null) {
                    Lyrica.CHAT_MANAGER.tagged("Could not find the setting specified.", getTag(), getName());
                    return;
                }

                if (!(setting instanceof BooleanSetting booleanSetting)) {
                    Lyrica.CHAT_MANAGER.tagged("This command only works for " + ChatUtils.getPrimary() + "boolean" + ChatUtils.getSecondary() + " settings.", getTag(), getName());
                    return;
                }

                booleanSetting.setValue(!booleanSetting.getValue());
                Lyrica.CHAT_MANAGER.tagged(ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " has been toggled " + (booleanSetting.getValue() ? Formatting.GREEN + "on" : Formatting.RED + "off") + ChatUtils.getSecondary() + " for " + ChatUtils.getPrimary() + module.getName() + ChatUtils.getSecondary() + ".", getTag(), getName() + "-cmd-" + module.getName() + "-" + setting.getName() );

            }
        } else {
            messageSyntax();
        }
    }
}
