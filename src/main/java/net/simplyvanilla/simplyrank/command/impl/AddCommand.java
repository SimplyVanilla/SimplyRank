package net.simplyvanilla.simplyrank.command.impl;

import net.simplyvanilla.simplyrank.command.AbstractCommand;
import net.simplyvanilla.simplyrank.command.CommandContext;
import net.simplyvanilla.simplyrank.command.CommandErrorMessages;
import net.simplyvanilla.simplyrank.permission.PlayerDataService;
import net.simplyvanilla.simplyrank.permission.PermissionApplyService;

import static net.kyori.adventure.text.Component.text;

public class AddCommand extends AbstractCommand {

    public AddCommand(
        CommandErrorMessages errorMessages,
        PlayerDataService playerDataService,
        PermissionApplyService permissionApplyService) {
        super(errorMessages, playerDataService, permissionApplyService);
    }

    @Override
    public void execute(CommandContext context) {
        if (context.getArguments().length != 3) {
            context.getSender().sendMessage(text(errorMessages.addCommandFormatError()));
            return;
        }

        fetchPlayerData(
            context,
            context.getArgument(2),
            data ->
                coreAddCommandHandler(
                    context.getSender(), context.getArgument(2), context.getArgument(1), data));
    }
}
