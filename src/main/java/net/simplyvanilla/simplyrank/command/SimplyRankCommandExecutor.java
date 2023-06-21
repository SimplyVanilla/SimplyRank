package net.simplyvanilla.simplyrank.command;

import static net.kyori.adventure.text.Component.text;

import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.stream.Collectors;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.simplyvanilla.simplyrank.data.DataManager;
import net.simplyvanilla.simplyrank.data.GroupData;
import net.simplyvanilla.simplyrank.data.IOCallback;
import net.simplyvanilla.simplyrank.data.PlayerData;
import net.simplyvanilla.simplyrank.utils.PermissionApplier;
import net.simplyvanilla.simplyrank.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SimplyRankCommandExecutor implements CommandExecutor {
  private final DataManager dataManager;
  private final PermissionApplier permissionApplier;
  private final CommandErrorMessages errorMessages;

  public SimplyRankCommandExecutor(DataManager dataManager, PermissionApplier permissionApplier) {
    this.dataManager = dataManager;
    this.permissionApplier = permissionApplier;
    this.errorMessages = new CommandErrorMessages();
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    if (!(sender instanceof ConsoleCommandSender)) {
      sender.sendMessage(text(errorMessages.noPermission()));
      return true;
    }

    if (args.length == 0) {
      sender.sendMessage(text(errorMessages.useCorrectFormat(label)));
      return true;
    }

    String subCommand = args[0].toLowerCase(Locale.ROOT);

    switch (subCommand) {
      case "create" -> createCommandHandler(sender, label, args);
      case "set" -> setCommandHandler(sender, label, args);
      case "add" -> addCommandHandler(sender, label, args);
      case "get" -> getCommandHandler(sender, label, args);
      case "rem" -> remCommandHandler(sender, label, args);
      default -> {
        return true;
      }
    }

    return true;
  }

  private void createCommandHandler(CommandSender sender, String label, String[] args) {
    if (args.length < 3) {
      sender.sendMessage(text(errorMessages.createCommandFormatError(label)));
      return;
    }

    String name = args[1];
    try {
      TextColor color = NamedTextColor.NAMES.value(args[2].toUpperCase(Locale.ROOT));

      if (dataManager.groupExists(name)) {
        sender.sendMessage(text("That group does already exist."));
        return;
      }

      String prefix = "";
      if (args.length >= 4) {
        prefix = Arrays.stream(args, 3, args.length).collect(Collectors.joining(" "));
      }

      GroupData groupData = new GroupData(color, prefix);
      dataManager.saveGroupDataAsync(
          name,
          groupData,
          new IOCallback<>() {
            @Override
            public void success(Void data) {
              sender.sendMessage(text("Successfully created the group!"));
            }

            @Override
            public void error(Exception error) {
              sender.sendMessage(text("An error occurred!"));
            }
          });
    } catch (IllegalArgumentException e) {
      sender.sendMessage(text(errorMessages.colorDoesNotExistError()));
    }
  }

  private void setCommandHandler(CommandSender sender, String label, String[] args) {
    String input = args[1];

    if (args.length != 3) {
      sender.sendMessage(text(errorMessages.setCommandFormatError(label)));
      return;
    }

    final UUID uuid = PlayerUtils.resolveUuid(input);

    if (uuid == null) {
      sender.sendMessage(text(this.errorMessages.cannotFindPlayerError()));
      return;
    }

    String group = args[2];

    if (!dataManager.groupExists(group)) {
      sender.sendMessage(text("That group does not exist!"));
      return;
    }

    String uuidString = uuid.toString();
    dataManager.loadPlayerDataAsync(
        uuid,
        new IOCallback<>() {
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
          }
        });
  }

  private void addCommandHandler(CommandSender sender, String label, String[] args) {
    String input = args[1];

    if (args.length != 3) {
      sender.sendMessage(text(errorMessages.addCommandFormatError(label)));
      return;
    }

    final UUID uuid = PlayerUtils.resolveUuid(input);

    if (uuid == null) {
      sender.sendMessage(text(this.errorMessages.cannotFindPlayerError()));
      return;
    }

    String group = args[2];

    if (!dataManager.groupExists(group)) {
      sender.sendMessage(text("That group does not exist!"));
      return;
    }

    String uuidString = uuid.toString();
    dataManager.loadPlayerDataAsync(
        uuid,
        new IOCallback<>() {
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
          }
        });
  }

  private void getCommandHandler(CommandSender sender, String label, String[] args) {
    String input = args[1];

    if (args.length != 2) {
      sender.sendMessage(text(errorMessages.getCommandFormatError(label)));
      return;
    }

    final UUID uuid = PlayerUtils.resolveUuid(input);

    if (uuid == null) {
      sender.sendMessage(text(this.errorMessages.cannotFindPlayerError()));
      return;
    }

    dataManager.loadPlayerDataAsync(
        uuid,
        new IOCallback<>() {
          @Override
          public void success(PlayerData data) {
            sender.sendMessage(
                text("Groups from " + input + ": [" + String.join(", ", data.getGroups()) + "]"));
          }

          @Override
          public void error(Exception error) {
            sender.sendMessage(text("Could not load player data"));
          }
        });
  }

  private void remCommandHandler(CommandSender sender, String label, String[] args) {
    String input = args[1];

    if (args.length != 3) {
      sender.sendMessage(text(errorMessages.remCommandFormatError(label)));
      return;
    }

    final String group = args[2]; // Has to be final to be accessible in callback
    final UUID uuid = PlayerUtils.resolveUuid(input);

    if (uuid == null) {
      sender.sendMessage(text(this.errorMessages.cannotFindPlayerError()));
      return;
    }

    // First, fetch the current data
    dataManager.loadPlayerDataAsync(
        uuid,
        new IOCallback<>() {
          @Override
          public void success(PlayerData data) {
            List<String> groups = data.getGroups();

            if (!groups.contains(group)) {
              sender.sendMessage(text("does not have group."));
              return;
            }

            groups.remove(group);

            if (groups.isEmpty()) {
              groups.add("default");
            }

            data.setGroups(groups);

            // Next, replace the old data with the new one. Both asynchronous to save
            // performance
            dataManager.savePlayerDataAsync(
                uuid.toString(),
                data,
                new IOCallback<>() {
                  @Override
                  public void success(Void data) {
                    sender.sendMessage(text("Group successfully removed!"));

                    Player player = Bukkit.getPlayer(uuid);

                    if (player != null) {
                      permissionApplier.apply(player);
                    }
                  }

                  @Override
                  public void error(Exception error) {
                    sender.sendMessage(text("Could not remove group!"));
                  }
                });
          }

          @Override
          public void error(Exception error) {
            sender.sendMessage(text("Player data not found."));
          }
        });
  }

  private void coreSetCommandHandler(
      CommandSender sender, String group, String uuidString, PlayerData data) {
    List<String> groups = data.getGroups();

    if (!groups.isEmpty() && groups.get(0).equals(group)) {
      sender.sendMessage(text("Already primary group"));
      return;
    }

    if (groups.isEmpty()) {
      groups.add(group);
    } else {
      groups.set(0, group);
    }

    data.setGroups(groups);
    dataManager.savePlayerDataAsync(
        uuidString,
        data,
        new IOCallback<>() {
          @Override
          public void success(Void data) {
            sender.sendMessage(text("Successfully saved"));

            UUID uuid = UUID.fromString(uuidString);
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
              permissionApplier.apply(player);
            }
          }

          @Override
          public void error(Exception error) {
            sender.sendMessage(text("Saving failed"));
          }
        });
  }

  private void coreAddCommandHandler(
      CommandSender sender, String group, String uuidString, PlayerData data) {
    List<String> groups = data.getGroups();

    if (groups.contains(group)) {
      sender.sendMessage(text("already has group."));
      return;
    }

    groups.add(group);
    groups.remove("default");
    dataManager.savePlayerDataAsync(
        uuidString,
        data,
        new IOCallback<>() {
          @Override
          public void success(Void data) {
            sender.sendMessage(text("Sucessfully saved"));

            UUID uuid = UUID.fromString(uuidString);
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
              permissionApplier.apply(player);
            }
          }

          @Override
          public void error(Exception error) {
            sender.sendMessage(text("Saving failed"));
          }
        });
  }
}
