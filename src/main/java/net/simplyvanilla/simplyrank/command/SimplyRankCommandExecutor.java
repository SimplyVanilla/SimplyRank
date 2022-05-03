package net.simplyvanilla.simplyrank.command;

import net.simplyvanilla.simplyrank.data.DataManager;
import net.simplyvanilla.simplyrank.data.GroupData;
import net.simplyvanilla.simplyrank.data.IOCallback;
import net.simplyvanilla.simplyrank.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.stream.Collectors;

public class SimplyRankCommandExecutor implements CommandExecutor {

    private final DataManager dataManager;

    public SimplyRankCommandExecutor(DataManager dataManager) {
        this.dataManager = dataManager;
    }

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
                    dataManager.saveGroupData(name, groupData, new IOCallback<>() {
                        @Override
                        public void success(Void data) {
                            sender.sendMessage("Successfully created the group!");
                        }

                        @Override
                        public void error(IOException error) {
                            sender.sendMessage("An error occurred!");
                        }
                    });
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("That color does not exist!");
                    return true;
                }
            }

            case "set" -> {
                String name = args[1];

                if (args.length != 3) {
                    sender.sendMessage("Please use /" + label + " set <PLAYER_NAME> <RANK_NAME>");
                    return true;
                }

                UUID uuid = null;
                Player target = Bukkit.getPlayer(name);
                if (target != null) {
                    uuid = target.getUniqueId();
                } else {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name);
                    if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                        uuid = offlinePlayer.getUniqueId();
                    }
                }

                if (uuid == null) {
                    sender.sendMessage("Could not find player");
                    return true;
                }

                String group = args[2];

                if (!dataManager.groupExists(group)) {
                    sender.sendMessage("That group does not exist!");
                    return true;
                }

                String uuidString = uuid.toString();
                dataManager.loadPlayerData(uuid, new IOCallback<>() {
                    @Override
                    public void success(PlayerData data) {
                        coreSetCommandHandler(sender, group, uuidString, data);
                    }

                    @Override
                    public void error(IOException error) {
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
                String name = args[1];

                if (args.length != 3) {
                    sender.sendMessage("Please use /" + label + " add <PLAYER_NAME> <RANK_NAME>");
                    return true;
                }

                UUID uuid = null;
                Player target = Bukkit.getPlayer(name);
                if (target != null) {
                    uuid = target.getUniqueId();
                } else {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name);
                    if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                        uuid = offlinePlayer.getUniqueId();
                    }
                }

                if (uuid == null) {
                    sender.sendMessage("Could not find player");
                    return true;
                }

                String group = args[2];

                if (!dataManager.groupExists(group)) {
                    sender.sendMessage("That group does not exist!");
                    return true;
                }

                String uuidString = uuid.toString();
                dataManager.loadPlayerData(uuid, new IOCallback<>() {
                    @Override
                    public void success(PlayerData data) {
                        coreAddCommandHandler(sender, group, uuidString, data);
                    }

                    @Override
                    public void error(IOException error) {
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
                String name = args[1];

                if (args.length != 2) {
                    sender.sendMessage("Please use /" + label + " get <PLAYER_NAME>");
                    return true;
                }

                UUID uuid = null;
                Player target = Bukkit.getPlayer(name);
                if (target != null) {
                    uuid = target.getUniqueId();
                } else {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name);
                    if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                        uuid = offlinePlayer.getUniqueId();
                    }
                }

                if (uuid == null) {
                    sender.sendMessage("Could not find player");
                    return true;
                }

                dataManager.loadPlayerData(uuid, new IOCallback<>() {
                    @Override
                    public void success(PlayerData data) {
                        sender.sendMessage("Groups from " + name + ": [" + String.join(", ", data.getGroups()) + "]");
                    }

                    @Override
                    public void error(IOException error) {
                        sender.sendMessage("Could not load player data");
                    }
                });

            }

            case "rem" -> {
                String name = args[1];

                if (args.length != 3) {
                    sender.sendMessage("Please use /" + label + " rem <PLAYER_NAME> <RANK_NAME>");
                    return true;
                }

                UUID uuid = null;
                Player target = Bukkit.getPlayer(name);
                if (target != null) {
                    uuid = target.getUniqueId();
                } else {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name);
                    if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                        uuid = offlinePlayer.getUniqueId();
                    }
                }

                if (uuid == null) {
                    sender.sendMessage("Could not find player");
                    return true;
                }

                String group = args[2];

                UUID finalUuid = uuid;
                dataManager.loadPlayerData(uuid, new IOCallback<>() {
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

                        sender.sendMessage("Successfully removed group " + group);

                        dataManager.savePlayerData(finalUuid.toString(), data, new IOCallback<>() {
                            @Override
                            public void success(Void data) {
                                sender.sendMessage("Sucessfully saved");
                            }

                            @Override
                            public void error(IOException error) {
                                sender.sendMessage("Saving failed");
                            }
                        });
                    }

                    @Override
                    public void error(IOException error) {
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

        groups.remove(group);

        List<String> newGroups = new ArrayList<>();
        newGroups.add(group);
        newGroups.addAll(groups);

        newGroups.remove("default");

        data.setGroups(newGroups);
        dataManager.savePlayerData(uuidString, data, new IOCallback<>() {
            @Override
            public void success(Void data) {
                sender.sendMessage("Sucessfully saved");
            }

            @Override
            public void error(IOException error) {
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
        dataManager.savePlayerData(uuidString, data, new IOCallback<>() {
            @Override
            public void success(Void data) {
                sender.sendMessage("Sucessfully saved");
            }

            @Override
            public void error(IOException error) {
                sender.sendMessage("Saving failed");
            }
        });
    }

}
