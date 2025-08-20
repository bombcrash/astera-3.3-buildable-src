package me.lyrica.commands.impl;

import me.lyrica.Lyrica;
import me.lyrica.commands.Command;
import me.lyrica.commands.RegisterCommand;
import me.lyrica.modules.Module;
import me.lyrica.settings.Setting;
import me.lyrica.settings.impl.*;
import me.lyrica.utils.chat.ChatUtils;
import me.lyrica.utils.color.ColorUtils;
import me.lyrica.utils.input.KeyboardUtils;
import me.lyrica.utils.minecraft.IdentifierUtils;
import me.lyrica.utils.miscellaneous.ListUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.awt.*;
import java.util.Arrays;

@RegisterCommand(name = "module", tag = "Module", description = "Allows you to view and change this module's settings.", syntax = "<[setting]> <[...]> | <list|reset>")
public class ModuleCommand extends Command {
    private final Module module;

    public ModuleCommand(Module module) {
        this.module = module;

        setName(module.getName().toLowerCase());
        setTag(module.getName());
    }

    @Override
    public void execute(String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("list")) {
                if (module.getSettings().isEmpty()) {
                    Lyrica.CHAT_MANAGER.tagged("This module currently has no registered settings.", module.getName(), "module-cmd-" + getName() + "-list");
                } else {
                    StringBuilder builder = new StringBuilder();
                    int index = 0;

                    for (Setting setting : module.getSettings()) {
                        if (setting instanceof CategorySetting) continue;
                        builder.append(ChatUtils.getSecondary()).append(setting.getName()).append(ChatUtils.getPrimary()).append(" [").append(ChatUtils.getSecondary());

                        if (setting instanceof BooleanSetting) builder.append(((BooleanSetting) setting).getValue());
                        if (setting instanceof NumberSetting) builder.append(((NumberSetting) setting).getValue());
                        if (setting instanceof ModeSetting) builder.append(((ModeSetting) setting).getValue());
                        if (setting instanceof StringSetting) builder.append(((StringSetting) setting).getValue());
                        if (setting instanceof BindSetting) builder.append(KeyboardUtils.getKeyName(((BindSetting) setting).getValue()).toUpperCase());
                        if (setting instanceof WhitelistSetting) builder.append("...");
                        if (setting instanceof ColorSetting) builder.append("RGBA(").append(((ColorSetting) setting).getColor().getRed()).append(ChatUtils.getPrimary()).append(", ").append(ChatUtils.getSecondary()).append(((ColorSetting) setting).getColor().getGreen()).append(ChatUtils.getPrimary()).append(", ").append(ChatUtils.getSecondary()).append(((ColorSetting) setting).getColor().getBlue()).append(ChatUtils.getPrimary()).append(", ").append(ChatUtils.getSecondary()).append(((ColorSetting) setting).getColor().getAlpha()).append(")").append(ChatUtils.getPrimary()).append(",").append(ChatUtils.getSecondary()).append(" SyRa(").append(((ColorSetting) setting).isSync()).append(ChatUtils.getPrimary()).append(", ").append(ChatUtils.getSecondary()).append(((ColorSetting) setting).isRainbow()).append(")");

                        builder.append(ChatUtils.getPrimary()).append("]").append(ChatUtils.getSecondary()).append(index + 1 == module.getSettings().size() ? "" : ", ");
                        index++;
                    }

                    Lyrica.CHAT_MANAGER.message(ChatUtils.getSecondary() + module.getName() + " " + ChatUtils.getPrimary() + "[" + ChatUtils.getSecondary() + module.getSettings().size() + ChatUtils.getPrimary() + "]: " + ChatUtils.getSecondary() + builder, "module-cmd-" + getName() + "-list");
                }
            } else if (args[0].equalsIgnoreCase("reset")) {
                module.resetValues();
                Lyrica.CHAT_MANAGER.tagged("Successfully reset all of the module's settings.", module.getName(), "module-cmd-" + getName());
            } else {
                Setting uncastedSetting = module.getSetting(args[0]);
                if (uncastedSetting == null) {
                    Lyrica.CHAT_MANAGER.tagged("Could not find the setting specified.", module.getName(), "module-cmd-" + getName());
                    return;
                }

                args = Arrays.copyOfRange(args, 1, args.length);

                switch (uncastedSetting) {
                    case BooleanSetting setting -> {
                        if (setting == module.chatNotify) {
                            Lyrica.CHAT_MANAGER.tagged("This setting is the module's main ChatNotify setting. Please use the " + ChatUtils.getPrimary() + "chatnotify" + ChatUtils.getSecondary() + " command instead.", module.getName(), getName());
                            return;
                        }

                        if (setting == module.drawn) {
                            Lyrica.CHAT_MANAGER.tagged("This setting is the module's main Drawn setting. Please use the " + ChatUtils.getPrimary() + "drawn" + ChatUtils.getSecondary() + " command instead.", module.getName(), getName());
                            return;
                        }

                        if (args.length == 1) {
                            if (args[0].equalsIgnoreCase("reset")) {
                                setting.resetValue();
                                Lyrica.CHAT_MANAGER.tagged("Successfully reset the " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " setting.", module.getName(), "module-cmd-" + getName());
                            } else {
                                setting.setValue(Boolean.parseBoolean(args[0]));
                                Lyrica.CHAT_MANAGER.tagged("Successfully set " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " to " + ChatUtils.getPrimary() + setting.getValue() + ChatUtils.getSecondary() + ".", module.getName(), "module-cmd-" + getName());
                            }
                        } else {
                            Lyrica.CHAT_MANAGER.info(module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <[value]|reset>");
                        }
                    }

                    case NumberSetting setting -> {
                        if (args.length == 1) {
                            if (args[0].equalsIgnoreCase("reset")) {
                                setting.resetValue();
                                Lyrica.CHAT_MANAGER.tagged("Successfully reset the " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " setting.", module.getName(), "module-cmd-" + getName());
                            } else {
                                try {
                                    switch (setting.getType()) {
                                        case LONG -> setting.setValue(Long.parseLong(args[0]));
                                        case DOUBLE -> setting.setValue(Double.parseDouble(args[0]));
                                        case FLOAT -> setting.setValue(Float.parseFloat(args[0]));
                                        default -> setting.setValue(Integer.parseInt(args[0]));
                                    }

                                    Lyrica.CHAT_MANAGER.tagged("Successfully set " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " to " + ChatUtils.getPrimary() + setting.getValue() + ChatUtils.getSecondary() + ".", module.getName(), "module-cmd-" + getName());
                                } catch (NumberFormatException exception) {
                                    Lyrica.CHAT_MANAGER.tagged("Please input a valid " + ChatUtils.getPrimary() + setting.getType().name().toLowerCase() + ChatUtils.getSecondary() + " number.", module.getName(), "module-cmd-" + getName());
                                }
                            }
                        } else {
                            Lyrica.CHAT_MANAGER.info(module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <[value]|reset>");
                        }
                    }

                    case ModeSetting setting -> {
                        if (args.length >= 1) {
                            if ((args[0].equalsIgnoreCase("reset") && setting.getModes().stream().noneMatch("reset"::equalsIgnoreCase)) || args[0].equalsIgnoreCase("force-reset")) {
                                setting.resetValue();
                                Lyrica.CHAT_MANAGER.tagged("Successfully reset the " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " setting.", module.getName(), "module-cmd-" + getName());
                            } else if ((args[0].equalsIgnoreCase("list") && setting.getModes().stream().noneMatch("list"::equalsIgnoreCase)) || args[0].equalsIgnoreCase("force-list")) {
                                if (setting.getModes().isEmpty()) {
                                    Lyrica.CHAT_MANAGER.tagged(ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " currently has no values registered.", module.getName(), "module-cmd-" + getName());
                                } else {
                                    StringBuilder modesString = new StringBuilder();
                                    int index = 0;

                                    for (String str : setting.getModes()) {
                                        modesString.append(ChatUtils.getSecondary()).append(str).append(index + 1 == setting.getModes().size() ? "" : ", ");
                                        index++;
                                    }

                                    Lyrica.CHAT_MANAGER.tagged(ChatUtils.getSecondary() + setting.getName() + " " + ChatUtils.getPrimary() + "[" + ChatUtils.getSecondary() + setting.getModes().size() + ChatUtils.getPrimary() + "]: " + ChatUtils.getSecondary() + modesString, module.getName(), "module-cmd-" + getName());
                                }
                            } else {
                                if (args[0].equalsIgnoreCase("reset") && setting.getModes().stream().anyMatch("reset"::equalsIgnoreCase)) Lyrica.CHAT_MANAGER.info("If you would like to reset this setting's value, write \"" + ChatUtils.getPrimary() + "force-reset" + ChatUtils.getSecondary() + "\" instead.");
                                if (args[0].equalsIgnoreCase("list") && setting.getModes().stream().anyMatch("list"::equalsIgnoreCase)) Lyrica.CHAT_MANAGER.info("If you would like to view a list of valid values for this setting, write \"" + ChatUtils.getPrimary() + "force-list" + ChatUtils.getSecondary() + "\" instead.");

                                StringBuilder builder = new StringBuilder();
                                int index = 0;

                                for (String str : args) {
                                    builder.append(str).append(index + 1 == args.length ? "" : " ");
                                    index++;
                                }

                                if (setting.getModes().stream().anyMatch(builder.toString()::equalsIgnoreCase)) {
                                    setting.setValue(setting.getModes().get(ListUtils.getIndex(setting.getModes(), builder.toString())));
                                    Lyrica.CHAT_MANAGER.tagged("Successfully set " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " to " + ChatUtils.getPrimary() + setting.getValue() + ChatUtils.getSecondary() + ".", module.getName(), "module-cmd-" + getName());
                                } else {
                                    Lyrica.CHAT_MANAGER.tagged("Please input a valid value for this setting.", module.getName(), "module-cmd-" + getName());
                                }
                            }
                        } else {
                            Lyrica.CHAT_MANAGER.info(module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <[value]|reset|list>");
                        }
                    }

                    case StringSetting setting -> {
                        if (args.length >= 1) {
                            if (args[0].equalsIgnoreCase("force-reset")) {
                                setting.setValue(setting.getDefaultValue());
                                Lyrica.CHAT_MANAGER.tagged("Successfully reset the " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " setting.", module.getName(), "module-cmd-" + getName());
                            } else {
                                if (args[0].equalsIgnoreCase("reset")) Lyrica.CHAT_MANAGER.info("If you would like to reset this setting's value, write \"" + ChatUtils.getPrimary() + "force-reset" + ChatUtils.getSecondary() + "\" instead.");

                                StringBuilder builder = new StringBuilder();
                                int index = 0;

                                for (String str : args) {
                                    builder.append(str).append(index + 1 == args.length ? "" : " ");
                                    index++;
                                }

                                setting.setValue(builder.toString());
                                Lyrica.CHAT_MANAGER.tagged("Successfully set " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " to " + ChatUtils.getPrimary() + setting.getValue() + ChatUtils.getSecondary() + ".", module.getName(), "module-cmd-" + getName());
                            }
                        } else {
                            Lyrica.CHAT_MANAGER.info(module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <[value]|reset>");
                        }
                    }

                    case BindSetting setting -> {
                        if (uncastedSetting == module.bind) {
                            Lyrica.CHAT_MANAGER.tagged("This setting is the module's main toggle keybind. Please use the " + ChatUtils.getPrimary() + "bind" + ChatUtils.getSecondary() + " command instead.", module.getName(), "module-cmd-" + getName());
                            return;
                        }

                        if (args.length == 1) {
                            if (args[0].equalsIgnoreCase("reset")) {
                                setting.resetValue();
                                Lyrica.CHAT_MANAGER.tagged("Successfully reset the " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " setting.", module.getName(), "module-cmd-" + getName());
                            } else {
                                int key = 0;
                                try {
                                    key = KeyboardUtils.getKeyNumber(args[0]);
                                } catch (IllegalArgumentException ignored) {
                                }

                                setting.setValue(key);
                                Lyrica.CHAT_MANAGER.tagged("Successfully set " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " to " + ChatUtils.getPrimary() + KeyboardUtils.getKeyName(setting.getValue()).toUpperCase() + ChatUtils.getSecondary() + ".", module.getName(), "module-cmd-" + getName());
                            }
                        } else {
                            Lyrica.CHAT_MANAGER.info(module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <[value]|reset>");
                        }
                    }

                    case ColorSetting setting -> {
                        if (args.length == 2) {
                            int parsedValue = 0;
                            if (args[0].equalsIgnoreCase("red") || args[0].equalsIgnoreCase("green") || args[0].equalsIgnoreCase("blue") || args[0].equalsIgnoreCase("alpha")) {
                                try {
                                    parsedValue = Math.clamp(Integer.parseInt(args[1]), 0, 255);
                                } catch (NumberFormatException ignored) {
                                    Lyrica.CHAT_MANAGER.tagged("Please input a valid number for the " + ChatUtils.getPrimary() + args[0].toLowerCase() + ChatUtils.getSecondary() + " value.", module.getName());
                                    return;
                                }
                            }

                            String valueName = "";
                            String newValue = "";

                            if (args[0].equalsIgnoreCase("red")) {
                                setting.setColor(new Color(parsedValue, setting.getValue().getColor().getGreen(), setting.getValue().getColor().getBlue(), setting.getValue().getColor().getAlpha()));
                                valueName = "red";
                                newValue = String.valueOf(setting.getValue().getColor().getRed());
                            } else if (args[0].equalsIgnoreCase("green")) {
                                setting.setColor(new Color(setting.getValue().getColor().getRed(), parsedValue, setting.getValue().getColor().getBlue(), setting.getValue().getColor().getAlpha()));
                                valueName = "green";
                                newValue = String.valueOf(setting.getValue().getColor().getGreen());
                            } else if (args[0].equalsIgnoreCase("blue")) {
                                setting.setColor(new Color(setting.getValue().getColor().getRed(), setting.getValue().getColor().getGreen(), parsedValue, setting.getValue().getColor().getAlpha()));
                                valueName = "blue";
                                newValue = String.valueOf(setting.getValue().getColor().getBlue());
                            } else if (args[0].equalsIgnoreCase("alpha")) {
                                setting.setColor(new Color(setting.getValue().getColor().getRed(), setting.getValue().getColor().getGreen(), setting.getValue().getColor().getBlue(), parsedValue));
                                valueName = "alpha";
                                newValue = String.valueOf(setting.getValue().getColor().getAlpha());
                            } else if (args[0].equalsIgnoreCase("sync")) {
                                setting.setSync(Boolean.parseBoolean(args[1]));
                                valueName = "sync";
                                newValue = String.valueOf(setting.isSync());
                            } else if (args[0].equalsIgnoreCase("rainbow")) {
                                setting.setRainbow(Boolean.parseBoolean(args[1]));
                                valueName = "rainbow";
                                newValue = String.valueOf(setting.isRainbow());
                            } else if (args[0].equalsIgnoreCase("code")) {
                                if (!ColorUtils.isValidColorCode(args[1])) {
                                    Lyrica.CHAT_MANAGER.tagged("Please input a valid color code.", module.getName(), "module-cmd-" + getName());
                                    return;
                                }

                                try {
                                    Color decoded = Color.decode((args[1].startsWith("#") ? "" : "#") + args[1]);
                                    setting.setColor(new Color(decoded.getRed(), decoded.getGreen(), decoded.getBlue()));
                                } catch (NumberFormatException exception) {
                                    Lyrica.CHAT_MANAGER.tagged("Please input a valid color code.", module.getName(), "module-cmd-" + getName());
                                    return;
                                }

                                valueName = "color";
                                newValue = "rgba(" + setting.getValue().getColor().getRed() + ", " + setting.getValue().getColor().getGreen() + ", " + setting.getValue().getColor().getBlue() + ", " + setting.getValue().getColor().getAlpha() + ")";
                            } else {
                                Lyrica.CHAT_MANAGER.info(module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>");
                            }

                            Lyrica.CHAT_MANAGER.tagged("Successfully set the " + ChatUtils.getPrimary() + valueName + ChatUtils.getSecondary() + " value to " + ChatUtils.getPrimary() + newValue + ChatUtils.getSecondary() + ".", module.getName(), "module-cmd-" + getName());
                        } else if (args.length == 1) {
                            if (args[0].equalsIgnoreCase("reset")) {
                                setting.resetValue();
                                Lyrica.CHAT_MANAGER.tagged("Successfully reset the " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " setting.", module.getName(), "module-cmd-" + getName());
                            } else {
                                Lyrica.CHAT_MANAGER.info(module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>");
                            }
                        } else {
                            Lyrica.CHAT_MANAGER.info(module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <red|green|blue|alpha|sync|rainbow|code> <[input]> | <reset>");
                        }
                    }

                    case WhitelistSetting setting -> {
                        if (args.length == 2) {
                            if (args[0].equalsIgnoreCase("add")) {
                                if (setting.getType() == WhitelistSetting.Type.ITEMS) {
                                    Item item = IdentifierUtils.getItem(args[1]);
                                    if (item == null) {
                                        Lyrica.CHAT_MANAGER.tagged("Please input a valid item ID.", module.getName(), "module-cmd-" + getName());
                                        return;
                                    }

                                    if (setting.isWhitelistContains(item)) {
                                        Lyrica.CHAT_MANAGER.tagged(ChatUtils.getPrimary() + item.getName().getString() + ChatUtils.getSecondary() + " is already on the whitelist.", module.getName(), "module-cmd-" + getName());
                                    } else {
                                        setting.add(item);
                                        Lyrica.CHAT_MANAGER.tagged("Successfully added " + ChatUtils.getPrimary() + item.getName().getString() + ChatUtils.getSecondary() + " to the whitelist.", module.getName(), "module-cmd-" + getName());
                                    }
                                } else if (setting.getType() == WhitelistSetting.Type.BLOCKS) {
                                    Block block = IdentifierUtils.getBlock(args[1]);
                                    if (block == null) {
                                        Lyrica.CHAT_MANAGER.tagged("Please input a valid block ID.", module.getName(), "module-cmd-" + getName());
                                        return;
                                    }

                                    if (setting.isWhitelistContains(block)) {
                                        Lyrica.CHAT_MANAGER.tagged(ChatUtils.getPrimary() + block.getName().getString() + ChatUtils.getSecondary() + " is already on the whitelist.", module.getName(), "module-cmd-" + getName());
                                    } else {
                                        setting.add(block);
                                        Lyrica.CHAT_MANAGER.tagged("Successfully added " + ChatUtils.getPrimary() + block.getName().getString() + ChatUtils.getSecondary() + " to the whitelist.", module.getName(), "module-cmd-" + getName());
                                    }
                                } else {
                                    Lyrica.CHAT_MANAGER.error("Something went wrong while detecting the setting's type.");
                                }
                            } else if (args[0].equalsIgnoreCase("del")) {
                                Item item; Block block;
                                if (setting.getType() == WhitelistSetting.Type.ITEMS && (item = IdentifierUtils.getItem(args[1])) != null && setting.isWhitelistContains(item)) {
                                    setting.remove(item);
                                    Lyrica.CHAT_MANAGER.tagged("Successfully removed " + ChatUtils.getPrimary() + item.getName().getString() + ChatUtils.getSecondary() + " from the whitelist.", module.getName(), "module-cmd-" + getName());
                                } else if (setting.getType() == WhitelistSetting.Type.BLOCKS && (block = IdentifierUtils.getBlock(args[1])) != null && setting.isWhitelistContains(block)) {
                                    setting.remove(block);
                                    Lyrica.CHAT_MANAGER.tagged("Successfully removed " + ChatUtils.getPrimary() + block.getName().getString() + ChatUtils.getSecondary() + " from the whitelist.", module.getName(), "module-cmd-" + getName());
                                } else {
                                    Lyrica.CHAT_MANAGER.tagged(ChatUtils.getPrimary() + args[1] + ChatUtils.getSecondary() + " is not on the whitelist.", module.getName(), "module-cmd-" + getName());
                                }
                            } else {
                                Lyrica.CHAT_MANAGER.info(module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <add|del> <[id]> | <list|clear>");
                            }
                        } else if (args.length == 1) {
                            if (args[0].equalsIgnoreCase("clear")) {
                                setting.getWhitelist().clear();
                                Lyrica.CHAT_MANAGER.tagged("Successfully cleared the " + ChatUtils.getPrimary() + setting.getName() + ChatUtils.getSecondary() + " whitelist.", module.getName(), "module-cmd-" + getName());
                            } else if (args[0].equalsIgnoreCase("list")) {
                                if (setting.getWhitelist().isEmpty()) {
                                    Lyrica.CHAT_MANAGER.tagged("There are currently no " + (setting.getType() == WhitelistSetting.Type.ITEMS ? "items" : "blocks") + " on the whitelist.", module.getName(), "module-cmd-" + getName());
                                } else {
                                    StringBuilder whitelist = new StringBuilder();
                                    int index = 0;

                                    for (Object object : setting.getWhitelist()) {
                                        whitelist.append(ChatUtils.getSecondary());

                                        switch (object) {
                                            case Block block -> whitelist.append(block.getName().getString());
                                            case Item item -> whitelist.append(item.getName().getString());
                                            default -> whitelist.append("Invalid");
                                        }

                                        whitelist.append(ChatUtils.getPrimary()).append(index + 1 == setting.getWhitelist().size() ? "" : ", ");
                                        index++;
                                    }

                                    Lyrica.CHAT_MANAGER.message(ChatUtils.getSecondary() + setting.getName() + " " + ChatUtils.getPrimary() + "[" + ChatUtils.getSecondary() + setting.getWhitelist().size() + ChatUtils.getPrimary() + "]: " + ChatUtils.getSecondary() + whitelist, "module-cmd-" + getName());
                                }
                            } else {
                                Lyrica.CHAT_MANAGER.info(module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <add|del> <[id]> | <clear|list>");
                            }
                        } else {
                            Lyrica.CHAT_MANAGER.info(module.getName().toLowerCase() + " " + setting.getName().toLowerCase() + " <add|del> <[id]> | <clear|list>");
                        }
                    }

                    default -> messageSyntax();
                }
            }
        } else {
            messageSyntax();
        }
    }
}
