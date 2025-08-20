package me.lyrica.commands.impl;

import me.lyrica.Lyrica;
import me.lyrica.commands.Command;
import me.lyrica.commands.RegisterCommand;
import me.lyrica.utils.chat.ChatUtils;

import java.util.List;

@RegisterCommand(name = "help", tag = "Help", description = "Shows you a list of all of the client's commands or information about a certain command.", syntax = "empty | <[command]>", aliases = {"cmds"})
public class HelpCommand extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length == 1)  {
            Command command = Lyrica.COMMAND_MANAGER.getCommand(args[0]);
            if (command == null) {
                Lyrica.CHAT_MANAGER.tagged("Could not find the command specified.", getTag(), getName());
                return;
            }

            Lyrica.CHAT_MANAGER.info(command.getTag() + ChatUtils.getPrimary() + " - " + ChatUtils.getSecondary() + command.getName() + " " + command.getSyntax());
            Lyrica.CHAT_MANAGER.info(command.getDescription());
        } else if (args.length == 0) {
            List<Command> commands = Lyrica.COMMAND_MANAGER.getCommands();

            if (commands.isEmpty()) {
                Lyrica.CHAT_MANAGER.tagged("There are currently no registered commands.", getTag(), getName() + "-list");
            } else {
                StringBuilder builder = new StringBuilder();
                int index = 0;

                for (Command command : commands) {
                    index++;
                    builder.append(ChatUtils.getSecondary()).append(command.getName())
                            .append(index == commands.size() ? "" : ", ");
                }

                Lyrica.CHAT_MANAGER.message("Commands " + ChatUtils.getPrimary() + "[" + ChatUtils.getSecondary() + commands.size() + ChatUtils.getPrimary() + "]: " + ChatUtils.getSecondary() + builder, getName() + "-list");
            }
        } else {
            messageSyntax();
        }
    }
}
