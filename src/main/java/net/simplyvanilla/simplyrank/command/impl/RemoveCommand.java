package net.simplyvanilla.simplyrank.command.impl;

import static net.kyori.adventure.text.Component.text;

import java.util.List;
import java.util.UUID;
import net.simplyvanilla.simplyrank.command.CommandContext;
import net.simplyvanilla.simplyrank.command.CommandErrorMessages;
import net.simplyvanilla.simplyrank.command.SubCommand;
import net.simplyvanilla.simplyrank.data.DataManager;
import net.simplyvanilla.simplyrank.data.IOCallback;
import net.simplyvanilla.simplyrank.utils.PermissionApplier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RemoveCommand implements SubCommand {
  private final CommandErrorMessages errorMessages;
  private final DataManager dataManager;
  private final PermissionApplier permissionApplier;

  public RemoveCommand(
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
      context.getSender().sendMessage(text(errorMessages.remCommandFormatError()));
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
          dataManager.savePlayerDataAsync(
              context.getArgument(1),
              data,
              new IOCallback<>() {
                @Override
                public void success(Void data) {
                  context.getSender().sendMessage(text("Group successfully removed!"));

                  UUID uuid = context.getOptionalArgument(1).map(UUID::fromString).orElse(null);
                  if (uuid == null) return;
                  Player player = Bukkit.getPlayer(uuid);

                  if (player != null) {
                    permissionApplier.apply(player);
                  }
                }

                @Override
                public void error(Exception error) {
                  context.getSender().sendMessage(text("Could not remove group!"));
                }
              });
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
