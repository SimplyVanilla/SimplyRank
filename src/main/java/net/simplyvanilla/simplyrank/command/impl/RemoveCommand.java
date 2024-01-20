package net.simplyvanilla.simplyrank.command.impl;

import net.simplyvanilla.simplyrank.command.AbstractCommand;
import net.simplyvanilla.simplyrank.command.CommandContext;
import net.simplyvanilla.simplyrank.command.CommandErrorMessages;
import net.simplyvanilla.simplyrank.data.PlayerDataService;
import net.simplyvanilla.simplyrank.data.callback.IOCallback;
import net.simplyvanilla.simplyrank.data.PermissionApplyService;
import net.simplyvanilla.simplyrank.utils.PlayerUtils;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class RemoveCommand extends AbstractCommand {

    public RemoveCommand(
        CommandErrorMessages errorMessages,
        PlayerDataService playerDataService,
        PermissionApplyService permissionApplyService) {
        super(errorMessages, playerDataService, permissionApplyService);
    }

    @Override
    public void execute(CommandContext context) {
        if (context.getArguments().length != 3) {
            context.getSender().sendMessage(text(errorMessages.remCommandFormatError()));
            return;
        }
        UUID uuid = PlayerUtils.resolveUuid(context.getArgument(1));
        if (uuid == null) {
            context.getSender().sendMessage(text(errorMessages.cannotFindPlayerError()));
            return;
        }

        final String group = context.getArgument(2);
        fetchPlayerData(
            context,
            group,
            data -> {
                List<String> groups = data.getGroups();

                if (!groups.contains(group)) {
                    context.getSender().sendMessage(text("does not have group."));
                    return;
                }

                groups.remove(group);

                if (groups.isEmpty()) {
                    groups.add("default");
                }

                data.setGroups(groups);

                // Next, replace the old data with the new one. Both asynchronous to save performance
                playerDataService.savePlayerDataAsync(
                    uuid,
                    data,
                    new IOCallback<>() {
                        @Override
                        public void success(Void data) {
                            context.getSender().sendMessage(text("Group successfully removed!"));

                            Optional.ofNullable(Bukkit.getPlayer(uuid))
                                .ifPresent(permissionApplyService::apply);
                        }

                        @Override
                        public void error(Exception error) {
                            context.getSender().sendMessage(text("Could not remove group!"));
                        }
                    });
            });
    }
}
