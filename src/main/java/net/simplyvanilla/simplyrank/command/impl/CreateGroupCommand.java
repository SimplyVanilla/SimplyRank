package net.simplyvanilla.simplyrank.command.impl;

import static net.kyori.adventure.text.Component.text;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.simplyvanilla.simplyrank.command.AbstractCommand;
import net.simplyvanilla.simplyrank.command.CommandContext;
import net.simplyvanilla.simplyrank.command.CommandErrorMessages;
import net.simplyvanilla.simplyrank.data.DataManager;
import net.simplyvanilla.simplyrank.data.GroupData;
import net.simplyvanilla.simplyrank.data.WrappedCallback;
import net.simplyvanilla.simplyrank.utils.PermissionApplier;

public class CreateGroupCommand extends AbstractCommand {

  public CreateGroupCommand(
      CommandErrorMessages errorMessages,
      DataManager dataManager,
      PermissionApplier permissionApplier) {
    super(errorMessages, dataManager, permissionApplier);
  }

  @Override
  public void execute(CommandContext context) {
    if (context.getArguments().length < 3) {
      context.getSender().sendMessage(text(errorMessages.createCommandFormatError()));
      return;
    }

    String name = context.getArgument(1);
    try {
      TextColor color = NamedTextColor.NAMES.value(context.getArgument(1).toUpperCase(Locale.ROOT));

      if (dataManager.groupExists(name)) {
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
      dataManager.saveGroupDataAsync(
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
