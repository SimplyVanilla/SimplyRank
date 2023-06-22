package net.simplyvanilla.simplyrank.command.impl;

import static net.kyori.adventure.text.Component.text;

import java.util.List;
import java.util.UUID;
import net.simplyvanilla.simplyrank.command.AbstractCommand;
import net.simplyvanilla.simplyrank.command.CommandContext;
import net.simplyvanilla.simplyrank.command.CommandErrorMessages;
import net.simplyvanilla.simplyrank.data.DataManager;
import net.simplyvanilla.simplyrank.data.IOCallback;
import net.simplyvanilla.simplyrank.utils.PermissionApplier;
import org.bukkit.Bukkit;

public class RemoveCommand extends AbstractCommand {

  public RemoveCommand(
      CommandErrorMessages errorMessages,
      DataManager dataManager,
      PermissionApplier permissionApplier) {
    super(errorMessages, dataManager, permissionApplier);
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

                  context
                      .getOptionalArgument(1)
                      .map(UUID::fromString)
                      .map(Bukkit::getPlayer)
                      .ifPresent(permissionApplier::apply);
                }

                @Override
                public void error(Exception error) {
                  context.getSender().sendMessage(text("Could not remove group!"));
                }
              });
        });
  }
}
