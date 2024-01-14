package net.simplyvanilla.simplyrank.command;

import net.simplyvanilla.simplyrank.data.PlayerDataService;
import net.simplyvanilla.simplyrank.data.database.player.PlayerData;
import net.simplyvanilla.simplyrank.data.callback.WrappedCallback;
import net.simplyvanilla.simplyrank.data.PermissionApplyService;
import net.simplyvanilla.simplyrank.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.text;

public interface SubCommand {
    void execute(CommandContext context);

    CommandErrorMessages getErrorMessages();

    PlayerDataService getDataManager();

    PermissionApplyService getPermissionApplier();

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
                        }
                    }));
    }

    default void coreSetCommandHandler(
        CommandSender sender, String group, String input, PlayerData data) {
        UUID uuid = PlayerUtils.resolveUuid(input);
        if (uuid == null) {
            sender.sendMessage(text(this.getErrorMessages().cannotFindPlayerError()));
            return;
        }

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
                uuid,
                data,
                WrappedCallback.wrap(
                    unused -> {
                        sender.sendMessage(text("Successfully saved"));

                        Player player = Bukkit.getPlayer(uuid);

                        if (player != null) {
                            getPermissionApplier().apply(player);
                        }
                    },
                    exception -> sender.sendMessage(text("Saving failed"))));
    }

    default void coreAddCommandHandler(
        CommandSender sender, String group, String input, PlayerData data) {
        UUID uuid = PlayerUtils.resolveUuid(input);
        if (uuid == null) {
            sender.sendMessage(text(this.getErrorMessages().cannotFindPlayerError()));
            return;
        }
        List<String> groups = data.getGroups();

        if (groups.contains(group)) {
            sender.sendMessage(text("already has group."));
            return;
        }

        groups.add(group);
        groups.remove("default");
        getDataManager()
            .savePlayerDataAsync(
                uuid,
                data,
                WrappedCallback.wrap(
                    unused -> {
                        sender.sendMessage(text("Sucessfully saved"));

                        Player player = Bukkit.getPlayer(uuid);

                        if (player != null) {
                            getPermissionApplier().apply(player);
                        }
                    },
                    exception -> sender.sendMessage(text("Saving failed"))));
    }
}
