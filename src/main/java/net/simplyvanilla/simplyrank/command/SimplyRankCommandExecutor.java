package net.simplyvanilla.simplyrank.command;

import net.simplyvanilla.simplyrank.data.DataManager;
import net.simplyvanilla.simplyrank.data.GroupData;
import net.simplyvanilla.simplyrank.data.IOCallback;
import net.simplyvanilla.simplyrank.data.PlayerData;
import net.simplyvanilla.simplyrank.utils.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.stream.Collectors;

public class SimplyRankCommandExecutor implements CommandExecutor {

    private final DataManager dataManager;

    public SimplyRankCommandExecutor(DataManager dataManager) {
        this.dataManager = dataManager;
    }


    /*
        TODO: Major refactoring of onCommand method is severely necessary
        It will become pretty unmaintainable
     */

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("No permission");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Please use /" + label + " <create|set|add|get>");
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "create" -> {
                if (args.length < 3) {
                    sender.sendMessage("Please use /" + label + " create <RANK_NAME> <COLOR> [PREFIX]");
                    return true;
                }

                String name = args[1];
                try {
                    ChatColor color = ChatColor.valueOf(args[2].toUpperCase(Locale.ROOT));

                    if (dataManager.groupExists(name)) {
                        sender.sendMessage("That group does already exist.");
                        return true;
                    }

                    String prefix = "";
                    if (args.length >= 4) {
                        prefix = Arrays.stream(args, 3, args.length).collect(Collectors.joining(" "));
                    }

                    GroupData groupData = new GroupData(color, prefix);
                    dataManager.saveGroupDataAsync(name, groupData, new IOCallback<>() {
                        @Override
                        public void success(Void data) {
                            sender.sendMessage("Successfully created the group!");
                        }

                        @Override
                        public void error(Exception error) {
                            sender.sendMessage("An error occurred!");
                        }
                    });
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("That color does not exist!");
                    return true;
                }
            }

            case "set" -> {
                String input = args[1];

                if (args.length != 3) {
                    sender.sendMessage("Please use /" + label + " set <PLAYER_NAME> <RANK_NAME>");
                    return true;
                }

                final UUID uuid = PlayerUtils.resolveUUID(input);

                if (uuid == null) {
                    sender.sendMessage(ChatColor.RED
                        + "Could not find player! (Neither by name, nor by UUID");
                    return true;
                }

                String group = args[2];

                if (!dataManager.groupExists(group)) {
                    sender.sendMessage("That group does not exist!");
                    return true;
                }

                String uuidString = uuid.toString();
                dataManager.loadPlayerDataAsync(uuid, new IOCallback<>() {
                    @Override
                    public void success(PlayerData data) {
                        coreSetCommandHandler(sender, group, uuidString, data);
                    }

                    @Override
                    public void error(Exception error) {
                        if (error instanceof FileNotFoundException || error instanceof NoSuchFileException) {
                            coreSetCommandHandler(sender, group, uuidString, new PlayerData(new ArrayList<>()));
                            return;
                        }

                        error.printStackTrace();

                        System.out.println("Retrieving player data failed");
                    }
                });
            }

            case "add" -> {
                String input = args[1];

                if (args.length != 3) {
                    sender.sendMessage("Please use /" + label + " add <PLAYER_NAME> <RANK_NAME>");
                    return true;
                }

                final UUID uuid = PlayerUtils.resolveUUID(input);

                if (uuid == null) {
                    sender.sendMessage(ChatColor.RED
                        + "Could not find player! (Neither by name, nor by UUID");
                    return true;
                }

                String group = args[2];

                if (!dataManager.groupExists(group)) {
                    sender.sendMessage("That group does not exist!");
                    return true;
                }

                String uuidString = uuid.toString();
                dataManager.loadPlayerDataAsync(uuid, new IOCallback<>() {
                    @Override
                    public void success(PlayerData data) {
                        coreAddCommandHandler(sender, group, uuidString, data);
                    }

                    @Override
                    public void error(Exception error) {
                        if (error instanceof FileNotFoundException || error instanceof NoSuchFileException) {
                            coreAddCommandHandler(sender, group, uuidString, new PlayerData(new ArrayList<>()));
                            return;
                        }

                        error.printStackTrace();

                        System.out.println("Retrieving player data failed");
                    }
                });
            }

            case "get" -> {
                String input = args[1];

                if (args.length != 2) {
                    sender.sendMessage("Please use /" + label + " get <PLAYER_NAME>");
                    return true;
                }

                final UUID uuid = PlayerUtils.resolveUUID(input);

                if (uuid == null) {
                    sender.sendMessage(ChatColor.RED
                        + "Could not find player! (Neither by name, nor by UUID");
                    return true;
                }

                dataManager.loadPlayerDataAsync(uuid, new IOCallback<>() {
                    @Override
                    public void success(PlayerData data) {
                        sender.sendMessage("Groups from " + input + ": [" + String.join(", ", data.getGroups()) + "]");
                    }

                    @Override
                    public void error(Exception error) {
                        sender.sendMessage("Could not load player data");
                    }
                });

            }

            case "rem" -> {
                String input = args[1];

                if (args.length != 3) {
                    sender.sendMessage("Please use /" + label + " add <PLAYER_NAME> <RANK_NAME>");
                    return true;
                }


                final String group = args[2]; //Has to be final to be accessible in callback
                final UUID uuid = PlayerUtils.resolveUUID(input);

                if (uuid == null) {
                    sender.sendMessage(ChatColor.RED
                        + "Could not find player! (Neither by name, nor by UUID");
                    return true;
                }

                //First, fetch the current data
                dataManager.loadPlayerDataAsync(uuid, new IOCallback<>() {
                    @Override
                    public void success(PlayerData data) {
                        List<String> groups = data.getGroups();

                        if (!groups.contains(group)) {
                            sender.sendMessage("does not have group.");
                            return;
                        }

                        groups.remove(group);

                        if (groups.isEmpty()) {
                            groups.add("default");
                        }

                        data.setGroups(groups);

                        //Next, replace the old data with the new one. Both asynchronous to save performance
                        dataManager.savePlayerDataAsync(uuid.toString(), data, new IOCallback<>() {
                            @Override
                            public void success(Void data) {
                                sender.sendMessage("Group successfully removed!");
                            }

                            @Override
                            public void error(Exception error) {
                                sender.sendMessage("Could not remove group!");
                            }
                        });
                    }

                    @Override
                    public void error(Exception error) {
                        sender.sendMessage("Player data not found.");
                    }
                });
            }

        }

        return true;
    }



    private void coreSetCommandHandler(CommandSender sender, String group, String uuidString, PlayerData data) {
        List<String> groups = data.getGroups();

        if (!groups.isEmpty() && groups.get(0).equals(group)) {
            sender.sendMessage("Already primary group");
            return;
        }

        if (groups.isEmpty()) {
            groups.add(group);
        }
        else {
            groups.set(0, group);
        }

        data.setGroups(groups);
        dataManager.savePlayerDataAsync(uuidString, data, new IOCallback<>() {
            @Override
            public void success(Void data) {
                sender.sendMessage("Successfully saved");
            }

            @Override
            public void error(Exception error) {
                sender.sendMessage("Saving failed");
            }
        });
    }

    private void coreAddCommandHandler(CommandSender sender, String group, String uuidString, PlayerData data) {
        List<String> groups = data.getGroups();

        if (groups.contains(group)) {
            sender.sendMessage("already has group.");
            return;
        }

        groups.add(group);
        groups.remove("default");
        dataManager.savePlayerDataAsync(uuidString, data, new IOCallback<>() {
            @Override
            public void success(Void data) {
                sender.sendMessage("Sucessfully saved");
            }

            @Override
            public void error(Exception error) {
                sender.sendMessage("Saving failed");
            }
        });
    }

}
