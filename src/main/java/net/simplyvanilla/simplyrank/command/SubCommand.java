package net.simplyvanilla.simplyrank.command;

import static net.kyori.adventure.text.Component.text;

import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.simplyvanilla.simplyrank.data.DataManager;
import net.simplyvanilla.simplyrank.data.PlayerData;
import net.simplyvanilla.simplyrank.data.WrappedCallback;
import net.simplyvanilla.simplyrank.utils.PermissionApplier;
import net.simplyvanilla.simplyrank.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface SubCommand {
  void execute(CommandContext context);

  CommandErrorMessages getErrorMessages();

  DataManager getDataManager();

  PermissionApplier getPermissionApplier();

  default void fetchPlayerData(CommandContext context, String group, Consumer<PlayerData> action) {
    String input = context.getArgument(1);
    final UUID uuid = PlayerUtils.resolveUuid(input);

    if (uuid == null) {
      context.getSender().sendMessage(text(getErrorMessages().cannotFindPlayerError()));
      return;
    }

    if (!getDataManager().groupExists(group)) {
      context.getSender().sendMessage(text("That group does not exist!"));
      return;
    }

    getDataManager()
        .loadPlayerDataAsync(
            uuid,
            WrappedCallback.wrap(
                action,
                exception -> {
                  if (exception instanceof FileNotFoundException
                      || exception instanceof NoSuchFileException) {
                    action.accept(new PlayerData(new ArrayList<>()));
                    return;
                  }

                  exception.printStackTrace();
                }));
  }

  default void coreSetCommandHandler(
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
    getDataManager()
        .savePlayerDataAsync(
            uuidString,
            data,
            WrappedCallback.wrap(
                unused -> {
                  sender.sendMessage(text("Successfully saved"));

                  UUID uuid = UUID.fromString(uuidString);
                  Player player = Bukkit.getPlayer(uuid);

                  if (player != null) {
                    getPermissionApplier().apply(player);
                  }
                },
                exception -> sender.sendMessage(text("Saving failed"))));
  }

  default void coreAddCommandHandler(
      CommandSender sender, String group, String uuidString, PlayerData data) {
    List<String> groups = data.getGroups();

    if (groups.contains(group)) {
      sender.sendMessage(text("already has group."));
      return;
    }

    groups.add(group);
    groups.remove("default");
    getDataManager()
        .savePlayerDataAsync(
            uuidString,
            data,
            WrappedCallback.wrap(
                unused -> {
                  sender.sendMessage(text("Sucessfully saved"));

                  UUID uuid = UUID.fromString(uuidString);
                  Player player = Bukkit.getPlayer(uuid);

                  if (player != null) {
                    getPermissionApplier().apply(player);
                  }
                },
                exception -> sender.sendMessage(text("Saving failed"))));
  }
}
