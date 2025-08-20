package me.lyrica.commands.impl;

import me.lyrica.Lyrica;
import me.lyrica.commands.Command;
import me.lyrica.commands.RegisterCommand;
import me.lyrica.modules.Module;
import me.lyrica.settings.impl.BindSetting;
import me.lyrica.utils.chat.ChatUtils;
import me.lyrica.utils.input.KeyboardUtils;

import java.util.List;

@RegisterCommand(name = "bind", tag = "Bind", description = "Changes the toggle keybind of a module.", syntax = "<[module]> <[key]|reset|mode> | <reset|list>", aliases = {"b", "key", "keybind"})
public class BindCommand extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length == 2) {
            Module module = Lyrica.MODULE_MANAGER.getModule(args[0]);
            if (module == null) {
                Lyrica.CHAT_MANAGER.tagged("Could not find the module specified.", getTag(), getName());
                return;
            }

            if (args[1].equalsIgnoreCase("reset")) {
                module.setBind(0);
                Lyrica.CHAT_MANAGER.tagged("Successfully reset the toggle keybind of the " + ChatUtils.getPrimary() + module.getName() + ChatUtils.getSecondary() + " module.", getTag(), getName());
            } else {
                module.setBind(KeyboardUtils.getKeyNumber(args[1]));
                Lyrica.CHAT_MANAGER.tagged("Successfully bound the " + ChatUtils.getPrimary() + module.getName() + ChatUtils.getSecondary() + " module to the " + ChatUtils.getPrimary() + KeyboardUtils.getKeyName(module.getBind()) + ChatUtils.getSecondary() + " key.", getTag(), getName());
            }
        } else if (args.length == 3 && args[1].equalsIgnoreCase("mode")) {
            Module module = Lyrica.MODULE_MANAGER.getModule(args[0]);
            if (module == null) {
                Lyrica.CHAT_MANAGER.tagged("Could not find the module specified.", getTag(), getName());
                return;
            }
            
            if (args[2].equalsIgnoreCase("toggle")) {
                module.setBindMode(BindSetting.BindMode.TOGGLE);
                Lyrica.CHAT_MANAGER.tagged("Successfully set the bind mode of the " + ChatUtils.getPrimary() + module.getName() + ChatUtils.getSecondary() + " module to " + ChatUtils.getPrimary() + "Toggle" + ChatUtils.getSecondary() + ".", getTag(), getName());
            } else if (args[2].equalsIgnoreCase("hold")) {
                module.setBindMode(BindSetting.BindMode.HOLD);
                Lyrica.CHAT_MANAGER.tagged("Successfully set the bind mode of the " + ChatUtils.getPrimary() + module.getName() + ChatUtils.getSecondary() + " module to " + ChatUtils.getPrimary() + "Hold" + ChatUtils.getSecondary() + ".", getTag(), getName());
            } else {
                Lyrica.CHAT_MANAGER.tagged("Invalid bind mode. Valid modes: " + ChatUtils.getPrimary() + "toggle" + ChatUtils.getSecondary() + ", " + ChatUtils.getPrimary() + "hold" + ChatUtils.getSecondary() + ".", getTag(), getName());
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reset")) {
                for (Module module : Lyrica.MODULE_MANAGER.getModules()) {
                    module.setBind(0);
                }
                Lyrica.CHAT_MANAGER.tagged("Successfully reset the toggle keybind of all modules.", getTag(), getName());
            } else if (args[0].equalsIgnoreCase("list")) {
                List<Module> boundModules = Lyrica.MODULE_MANAGER.getModules().stream().filter(m -> m.getBind() != 0).toList();
                if (boundModules.isEmpty()) {
                    Lyrica.CHAT_MANAGER.tagged("No modules are currently bound.", getTag(), getName());
                    return;
                }

                StringBuilder sb = new StringBuilder("Bound modules: ");
                for (int i = 0; i < boundModules.size(); i++) {
                    Module module = boundModules.get(i);
                    sb.append(ChatUtils.getPrimary())
                            .append(module.getName())
                            .append(ChatUtils.getSecondary())
                            .append(" (")
                            .append(ChatUtils.getPrimary())
                            .append(KeyboardUtils.getKeyName(module.getBind()))
                            .append(ChatUtils.getSecondary())
                            .append(" - ")
                            .append(ChatUtils.getPrimary())
                            .append(module.getBindMode().getName())
                            .append(ChatUtils.getSecondary())
                            .append(")");

                    if (i != boundModules.size() - 1) {
                        sb.append(", ");
                    }
                }

                Lyrica.CHAT_MANAGER.tagged(sb.toString(), getTag(), getName());
            } else {
                Lyrica.CHAT_MANAGER.info(getSyntax());
            }
        } else {
            Lyrica.CHAT_MANAGER.info(getSyntax());
        }
    }
}
