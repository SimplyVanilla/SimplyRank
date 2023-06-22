package net.simplyvanilla.simplyrank.command.impl;

import static net.kyori.adventure.text.Component.text;

import java.util.UUID;
import net.simplyvanilla.simplyrank.command.CommandContext;
import net.simplyvanilla.simplyrank.command.CommandErrorMessages;
import net.simplyvanilla.simplyrank.command.SubCommand;
import net.simplyvanilla.simplyrank.data.DataManager;
import net.simplyvanilla.simplyrank.data.IOCallback;
import net.simplyvanilla.simplyrank.data.PlayerData;
import net.simplyvanilla.simplyrank.utils.PermissionApplier;
import net.simplyvanilla.simplyrank.utils.PlayerUtils;

public class GetCommand implements SubCommand {
  private final CommandErrorMessages errorMessages;
  private final DataManager dataManager;
  private final PermissionApplier permissionApplier;

  public GetCommand(
      CommandErrorMessages errorMessages,
      DataManager dataManager,
      PermissionApplier permissionApplier) {
    this.errorMessages = errorMessages;
    this.dataManager = dataManager;
    this.permissionApplier = permissionApplier;
  }

  @Override
  public void execute(CommandContext context) {
    String input = context.getArgument(1);

    if (context.getArguments().length != 2) {
      context.getSender().sendMessage(text(errorMessages.getCommandFormatError()));
      return;
    }

    final UUID uuid = PlayerUtils.resolveUuid(input);

    if (uuid == null) {
      context.getSender().sendMessage(text(this.errorMessages.cannotFindPlayerError()));
      return;
    }

    dataManager.loadPlayerDataAsync(
        uuid,
        new IOCallback<>() {
          @Override
          public void success(PlayerData data) {
            context
                .getSender()
                .sendMessage(
                    text(
                        "Groups from "
                            + input
                            + ": ["
                            + String.join(", ", data.getGroups())
                            + "]"));
          }

          @Override
          public void error(Exception error) {
            context.getSender().sendMessage(text("Could not load player data"));
          }
        });
  }

  @Override
  public CommandErrorMessages getErrorMessages() {
    return errorMessages;
  }

  @Override
  public DataManager getDataManager() {
    return dataManager;
  }

  @Override
  public PermissionApplier getPermissionApplier() {
    return permissionApplier;
  }
}
