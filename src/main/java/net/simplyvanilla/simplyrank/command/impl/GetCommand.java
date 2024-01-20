package net.simplyvanilla.simplyrank.command.impl;

import net.simplyvanilla.simplyrank.command.AbstractCommand;
import net.simplyvanilla.simplyrank.command.CommandContext;
import net.simplyvanilla.simplyrank.command.CommandErrorMessages;
import net.simplyvanilla.simplyrank.data.PermissionApplyService;
import net.simplyvanilla.simplyrank.data.PlayerDataService;
import net.simplyvanilla.simplyrank.data.database.player.PlayerData;
import net.simplyvanilla.simplyrank.utils.PlayerUtils;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class GetCommand extends AbstractCommand {

    public GetCommand(
        CommandErrorMessages errorMessages,
        PlayerDataService playerDataService,
        PermissionApplyService permissionApplyService) {
        super(errorMessages, playerDataService, permissionApplyService);
    }

    @Override
    public void execute(CommandContext context) {
        if (context.getArguments().length != 2) {
            context.getSender().sendMessage(text(errorMessages.getCommandFormatError()));
            return;
        }

        String input = context.getArgument(1);

        final UUID uuid = PlayerUtils.resolveUuid(input);

        if (uuid == null) {
            context.getSender().sendMessage(text(this.errorMessages.cannotFindPlayerError()));
            return;
        }

        try {
            PlayerData playerData = playerDataService.loadPlayerData(
                uuid);

            context
                .getSender()
                .sendMessage(
                    text(
                        "Groups from "
                            + input
                            + ": ["
                            + String.join(", ", playerData.getGroups())
                            + "]"));
        } catch (Exception e) {
            context.getSender().sendMessage(text("Could not load player data"));
        }
    }
}
