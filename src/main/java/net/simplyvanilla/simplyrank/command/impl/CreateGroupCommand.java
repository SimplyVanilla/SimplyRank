package net.simplyvanilla.simplyrank.command.impl;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.simplyvanilla.simplyrank.command.AbstractCommand;
import net.simplyvanilla.simplyrank.command.CommandContext;
import net.simplyvanilla.simplyrank.command.CommandErrorMessages;
import net.simplyvanilla.simplyrank.data.PlayerDataService;
import net.simplyvanilla.simplyrank.data.database.group.GroupData;
import net.simplyvanilla.simplyrank.data.callback.WrappedCallback;
import net.simplyvanilla.simplyrank.data.PermissionApplyService;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

public class CreateGroupCommand extends AbstractCommand {

    public CreateGroupCommand(
        CommandErrorMessages errorMessages,
        PlayerDataService playerDataService,
        PermissionApplyService permissionApplyService) {
        super(errorMessages, playerDataService, permissionApplyService);
    }

    @Override
    public void execute(CommandContext context) {
        if (context.getArguments().length < 3) {
            context.getSender().sendMessage(text(errorMessages.createCommandFormatError()));
            return;
        }

        String name = context.getArgument(1);
        try {
            TextColor color = NamedTextColor.NAMES.value(context.getArgument(2).toLowerCase(Locale.ROOT));

            if (playerDataService.groupExists(name)) {
                context.getSender().sendMessage(text("That group does already exist."));
                return;
            }

            String prefix = "";
            if (context.getArguments().length >= 4) {
                prefix =
                    Arrays.stream(context.getArguments(), 3, context.getArguments().length)
                        .collect(Collectors.joining(" "));
            }

            GroupData groupData = new GroupData(color, prefix);
            playerDataService.saveGroupDataAsync(
                name,
                groupData,
                WrappedCallback.wrap(
                    o -> context.getSender().sendMessage(text("Successfully created the group!")),
                    e -> context.getSender().sendMessage(text("An error occurred!"))));
        } catch (IllegalArgumentException e) {
            context.getSender().sendMessage(text(errorMessages.colorDoesNotExistError()));
        }
    }
}
