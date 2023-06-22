package net.simplyvanilla.simplyrank.command.impl;

import static net.kyori.adventure.text.Component.text;

import net.simplyvanilla.simplyrank.command.CommandContext;
import net.simplyvanilla.simplyrank.command.CommandErrorMessages;
import net.simplyvanilla.simplyrank.command.SubCommand;
import net.simplyvanilla.simplyrank.data.DataManager;
import net.simplyvanilla.simplyrank.utils.PermissionApplier;

public class AddCommand implements SubCommand {
  private final CommandErrorMessages errorMessages;
  private final DataManager dataManager;
  private final PermissionApplier permissionApplier;

  public AddCommand(
      CommandErrorMessages errorMessages,
      DataManager dataManager,
      PermissionApplier permissionApplier) {
    this.errorMessages = errorMessages;
    this.dataManager = dataManager;
    this.permissionApplier = permissionApplier;
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

  @Override
  public CommandErrorMessages getErrorMessages() {
    return this.errorMessages;
  }

  @Override
  public DataManager getDataManager() {
    return this.dataManager;
  }

  @Override
  public PermissionApplier getPermissionApplier() {
    return this.permissionApplier;
  }
}
