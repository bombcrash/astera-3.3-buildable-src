package me.lyrica.commands.impl;

import me.lyrica.Lyrica;
import me.lyrica.commands.Command;
import me.lyrica.commands.RegisterCommand;
import me.lyrica.utils.chat.ChatUtils;
import me.lyrica.utils.system.FileUtils;

import java.io.IOException;

@RegisterCommand(name = "config", tag = "Config", description = "Allows you to manage the client's configuration system.", syntax = "<load|save> <[name]> | <reload|save|current>")
public class ConfigCommand extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "load" -> {
                    if (!FileUtils.fileExists(Lyrica.MOD_NAME + "/Configs/" + args[1] + ".json")) {
                        Lyrica.CHAT_MANAGER.tagged("The specified configuration does not exist.", getTag(), getName());
                        return;
                    }

                    try {
                        Lyrica.CONFIG_MANAGER.loadModules(args[1]);
                        Lyrica.CHAT_MANAGER.tagged("Successfully loaded the " + ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " configuration.", getTag(), getName());
                    } catch (IOException exception) {
                        Lyrica.CHAT_MANAGER.tagged("Failed to load the " + ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " configuration.", getTag(), getName());
                    }
                }
                case "save" -> {
                    try {
                        Lyrica.CONFIG_MANAGER.saveModules(args[1]);
                        Lyrica.CHAT_MANAGER.tagged("Successfully saved the configuration to " + ChatUtils.getPrimary() + args[1] + ".json" + ChatUtils.getSecondary() + ".", getTag(), getName());
                    } catch (IOException exception) {
                        Lyrica.CHAT_MANAGER.tagged("Failed to save the " + ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " configuration.", getTag(), getName());
                    }
                }
                default -> messageSyntax();
            }
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "reload" -> {
                    Lyrica.CONFIG_MANAGER.loadConfig();
                    Lyrica.CHAT_MANAGER.tagged("Successfully reloaded the current configuration.", getTag(), getName());
                }
                case "save" -> {
                    Lyrica.CONFIG_MANAGER.saveConfig();
                    Lyrica.CHAT_MANAGER.tagged("Successfully saved the current configuration.", getTag(), getName());
                }
                case "current" -> Lyrica.CHAT_MANAGER.tagged("The client is currently using the " + ChatUtils.getPrimary() + Lyrica.CONFIG_MANAGER.getCurrentConfig() + ChatUtils.getSecondary() + " configuration.", getTag(), getName());
                default -> messageSyntax();
            }
        } else {
            messageSyntax();
        }
    }
}
