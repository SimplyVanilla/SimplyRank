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
        if (!(sender instanceof Player player) || !player.isOp()) {
            sender.sendMessage("No permission");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("Please use /" + label + " <create|set|add|get>");
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "create" -> {
                if (args.length < 3) {
                    player.sendMessage("Please use /" + label + " create <RANK_NAME> <COLOR> [PREFIX]");
                    return true;
                }

                String name = args[1];
                try {
                    ChatColor color = ChatColor.valueOf(args[2].toUpperCase(Locale.ROOT));

                    if (dataManager.groupExists(name)) {
                        player.sendMessage("That group does already exist.");
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
                            player.sendMessage("Successfully created the group!");
                        }

                        @Override
                        public void error(IOException error) {
                            player.sendMessage("An error occurred!");
                        }
                    });
                } catch (IllegalArgumentException e) {
                    player.sendMessage("That color does not exist!");
                    return true;
                }
            }

            case "set" -> {
                String name = args[1];

                if (args.length != 3) {
                    player.sendMessage("Please use /" + label + " set <PLAYER_NAME> <RANK_NAME>");
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
                    player.sendMessage("Could not find player");
                    return true;
                }

                String group = args[2];

                if (!dataManager.groupExists(group)) {
                    player.sendMessage("That group does not exist!");
                    return true;
                }

                String uuidString = uuid.toString();
                dataManager.loadPlayerData(uuid, new IOCallback<>() {
                    @Override
                    public void success(PlayerData data) {
                        coreSetCommandHandler(player, group, uuidString, data);
                    }

                    @Override
                    public void error(IOException error) {
                        if (error instanceof FileNotFoundException || error instanceof NoSuchFileException) {
                            coreSetCommandHandler(player, group, uuidString, new PlayerData(new ArrayList<>()));
                        }

                        error.printStackTrace();

                        System.out.println("Retrieving player data failed");
                    }
                });
            }

            case "add" -> {
                String name = args[1];

                if (args.length != 3) {
                    player.sendMessage("Please use /" + label + " add <PLAYER_NAME> <RANK_NAME>");
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
                    player.sendMessage("Could not find player");
                    return true;
                }

                String group = args[2];

                if (!dataManager.groupExists(group)) {
                    player.sendMessage("That group does not exist!");
                    return true;
                }

                String uuidString = uuid.toString();
                dataManager.loadPlayerData(uuid, new IOCallback<>() {
                    @Override
                    public void success(PlayerData data) {
                        coreAddCommandHandler(player, group, uuidString, data);
                    }

                    @Override
                    public void error(IOException error) {
                        if (error instanceof FileNotFoundException || error instanceof NoSuchFileException) {
                            coreAddCommandHandler(player, group, uuidString, new PlayerData(new ArrayList<>()));
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
                    player.sendMessage("Please use /" + label + " get <PLAYER_NAME>");
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
                    player.sendMessage("Could not find player");
                    return true;
                }

                dataManager.loadPlayerData(uuid, new IOCallback<>() {
                    @Override
                    public void success(PlayerData data) {
                        player.sendMessage("Groups from " + name + ": [" + String.join(", ", data.getGroups()) + "]");
                    }

                    @Override
                    public void error(IOException error) {
                        player.sendMessage("Could not load player data");
                    }
                });

            }

            case "rem" -> {
                String name = args[1];

                if (args.length != 3) {
                    player.sendMessage("Please use /" + label + " add <PLAYER_NAME> <RANK_NAME>");
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
                    player.sendMessage("Could not find player");
                    return true;
                }

                String group = args[2];

                dataManager.loadPlayerData(uuid, new IOCallback<>() {
                    @Override
                    public void success(PlayerData data) {
                        List<String> groups = data.getGroups();

                        if (!groups.contains(group)) {
                            player.sendMessage("does not have group.");
                            return;
                        }

                        groups.remove(group);
                        player.sendMessage("Successfully removed group " + group);
                    }

                    @Override
                    public void error(IOException error) {
                        player.sendMessage("Player data not found.");
                    }
                });
            }

        }

        return true;
    }

    private void coreSetCommandHandler(Player player, String group, String uuidString, PlayerData data) {
        List<String> groups = data.getGroups();

        if (!groups.isEmpty() && groups.get(0).equals(group)) {
            player.sendMessage("Already primary group");
            return;
        }

        groups.remove(group);

        List<String> newGroups = new ArrayList<>();
        newGroups.add(group);
        newGroups.addAll(groups);

        data.setGroups(newGroups);
        dataManager.savePlayerData(uuidString, data, new IOCallback<>() {
            @Override
            public void success(Void data) {
                player.sendMessage("Sucessfully saved");
            }

            @Override
            public void error(IOException error) {
                player.sendMessage("Saving failed");
            }
        });
    }

    private void coreAddCommandHandler(Player player, String group, String uuidString, PlayerData data) {
        List<String> groups = data.getGroups();

        if (groups.contains(group)) {
            player.sendMessage("already has group.");
            return;
        }

        groups.add(group);
        dataManager.savePlayerData(uuidString, data, new IOCallback<>() {
            @Override
            public void success(Void data) {
                player.sendMessage("Sucessfully saved");
            }

            @Override
            public void error(IOException error) {
                player.sendMessage("Saving failed");
            }
        });
    }

}
