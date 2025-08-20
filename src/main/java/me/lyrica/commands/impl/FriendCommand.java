package me.lyrica.commands.impl;

import me.lyrica.Lyrica;
import me.lyrica.commands.Command;
import me.lyrica.commands.RegisterCommand;
import me.lyrica.modules.impl.core.FriendModule;
import me.lyrica.utils.chat.ChatUtils;

import java.util.List;

@RegisterCommand(name = "friend", tag = "Friend", description = "Allows you to manage the client's friend list.", syntax = "<add|del> <[player]> | <clear|list>", aliases = {"f", "friends"})
public class FriendCommand extends Command {
    @Override
    public void execute(String[] args) {
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "add" -> {
                    if (!Lyrica.FRIEND_MANAGER.contains(args[1])) {
                        if (Lyrica.MODULE_MANAGER.getModule(FriendModule.class).friendMessage.getValue()) {
                            Lyrica.FRIEND_MANAGER.sendFriendMessage(args[1]);
                        }
                        Lyrica.FRIEND_MANAGER.add(args[1]);
                        Lyrica.CHAT_MANAGER.tagged("Successfully added " + ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " to your friends list.", getTag(), getName());
                    } else {
                        Lyrica.CHAT_MANAGER.tagged(ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " is already on your friends list.", getTag(), getName());
                    }
                }
                case "del" -> {
                    if (Lyrica.FRIEND_MANAGER.contains(args[1])) {
                        Lyrica.FRIEND_MANAGER.remove(args[1]);
                        Lyrica.CHAT_MANAGER.tagged("Successfully removed " + ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " from your friends list.", getTag(), getName());
                    } else {
                        Lyrica.CHAT_MANAGER.tagged(ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " is not on your friends list.", getTag(), getName());
                    }
                }
                default -> messageSyntax();
            }
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "clear" -> {
                    Lyrica.FRIEND_MANAGER.clear();
                    Lyrica.CHAT_MANAGER.tagged("Successfully cleared your friends list.", getTag(), getName() + "-list");
                }
                case "list" -> {
                    List<String> friends = Lyrica.FRIEND_MANAGER.getFriends();

                    if (friends.isEmpty()) {
                        Lyrica.CHAT_MANAGER.tagged("You currently have no friends.", getTag());
                    } else {
                        StringBuilder builder = new StringBuilder();
                        int index = 0;

                        for (String name : friends) {
                            index++;
                            builder.append(ChatUtils.getSecondary()).append(name)
                                    .append(index == friends.size() ? "" : ", ");
                        }

                        Lyrica.CHAT_MANAGER.message("Friends " + ChatUtils.getPrimary() + "[" + ChatUtils.getSecondary() + friends.size() + ChatUtils.getPrimary() + "]: " + ChatUtils.getSecondary() + builder, getName() + "-list");
                    }
                }
                default -> messageSyntax();
            }
        } else {
            messageSyntax();
        }
    }
}