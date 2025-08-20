package me.lyrica.commands.impl;

import me.lyrica.Lyrica;
import me.lyrica.commands.Command;
import me.lyrica.commands.RegisterCommand;
import net.minecraft.util.Util;

import java.io.File;

@RegisterCommand(name = "folder", description = "Opens the clients folder.")
public class FolderCommand extends Command {
    @Override
    public void execute(String[] args) {
        File folder = new File(Lyrica.MOD_NAME);
        if (folder.exists()) {
            Util.getOperatingSystem().open(folder);
        } else {
            Lyrica.CHAT_MANAGER.info("Could not find the client's configuration folder.");
        }
    }
}
