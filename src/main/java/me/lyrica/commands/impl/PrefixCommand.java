package me.lyrica.commands.impl;

import me.lyrica.Lyrica;
import me.lyrica.commands.Command;
import me.lyrica.commands.RegisterCommand;
import me.lyrica.utils.chat.ChatUtils;

@RegisterCommand(name = "prefix", tag = "Prefix", description = "Allows you to change the client's command prefix.", syntax = "<[input]> | <reset>")
public class PrefixCommand extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reset")) {
                Lyrica.COMMAND_MANAGER.setPrefix(".");
                Lyrica.CHAT_MANAGER.tagged("Successfully reset the command's prefix back to it's default.", getTag(), getName());
            } else {
                if (args[0].length() > 2) {
                    Lyrica.CHAT_MANAGER.tagged("The specified prefix is longer than the limit of 2 characters.", getTag(), getName());
                    return;
                }

                if (args[0].equalsIgnoreCase("/")) {
                    Lyrica.CHAT_MANAGER.tagged("The specified prefix would interfere with vanilla Minecraft commands.", getTag(), getName());
                    return;
                }

                Lyrica.COMMAND_MANAGER.setPrefix(args[0]);
                Lyrica.CHAT_MANAGER.tagged("Successfully set the client's command prefix to " + ChatUtils.getPrimary() + args[0] + ChatUtils.getSecondary() + ".", getTag(), getName());
            }
        } else {
            messageSyntax();
        }
    }
}
