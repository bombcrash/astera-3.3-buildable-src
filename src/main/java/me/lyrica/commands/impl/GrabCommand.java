package me.lyrica.commands.impl;

import me.lyrica.Lyrica;
import me.lyrica.commands.Command;
import me.lyrica.commands.RegisterCommand;
import me.lyrica.utils.chat.ChatUtils;

@RegisterCommand(name = "grab", description = "Lets you copy various things to your clipboard.", syntax = "<ip|coords|name>")
public class GrabCommand extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length == 1) {
            switch (args[0]) {
                case "ip" -> copy(Lyrica.SERVER_MANAGER.getServer());
                case "coords" -> copy("[" + (int) mc.player.getX() + ", " + (int) mc.player.getY() + ", " + (int) mc.player.getZ() + "]");
                case "name" -> copy(mc.player.getName().getString());
                default -> messageSyntax();
            }
        } else {
            messageSyntax();
        }
    }

    private void copy(String text) {
    mc.keyboard.setClipboard(text);
        Lyrica.CHAT_MANAGER.tagged("Successfully copied " + ChatUtils.getPrimary() + text + ChatUtils.getSecondary() + " to your clipboard.", getTag(), getName());
    }
}
