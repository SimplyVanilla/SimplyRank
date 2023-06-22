package net.simplyvanilla.simplyrank.command;

import static net.kyori.adventure.text.Component.text;

import java.util.*;
import net.simplyvanilla.simplyrank.command.impl.AddCommand;
import net.simplyvanilla.simplyrank.command.impl.CreateGroupCommand;
import net.simplyvanilla.simplyrank.command.impl.GetCommand;
import net.simplyvanilla.simplyrank.command.impl.RemoveCommand;
import net.simplyvanilla.simplyrank.command.impl.SetCommand;
import net.simplyvanilla.simplyrank.data.DataManager;
import net.simplyvanilla.simplyrank.utils.PermissionApplier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public class SimplyRankCommandExecutor implements CommandExecutor {
  private final DataManager dataManager;
  private final PermissionApplier permissionApplier;
  private final CommandErrorMessages errorMessages;
  private final Map<String, SubCommand> subCommandMap = new HashMap<>();

  public SimplyRankCommandExecutor(DataManager dataManager, PermissionApplier permissionApplier) {
    this.dataManager = dataManager;
    this.permissionApplier = permissionApplier;
    this.errorMessages = new CommandErrorMessages();

    this.subCommandMap.put(
        "create",
        new CreateGroupCommand(this.errorMessages, this.dataManager, this.permissionApplier));
    this.subCommandMap.put(
        "set", new SetCommand(this.errorMessages, this.dataManager, this.permissionApplier));
    this.subCommandMap.put(
        "add", new AddCommand(this.errorMessages, this.dataManager, this.permissionApplier));
    this.subCommandMap.put(
        "rem", new RemoveCommand(this.errorMessages, this.dataManager, this.permissionApplier));
    this.subCommandMap.put(
        "get", new GetCommand(this.errorMessages, this.dataManager, this.permissionApplier));
  }

  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      @NotNull Command command,
      @NotNull String label,
      @NotNull String[] args) {
    if (!(sender instanceof ConsoleCommandSender)) {
      sender.sendMessage(text(errorMessages.noPermission()));
      return true;
    }

    if (args.length == 0) {
      sender.sendMessage(text(errorMessages.useCorrectFormat()));
      return true;
    }

    String subCommand = args[0].toLowerCase(Locale.ROOT);

    if (!subCommandMap.containsKey(subCommand)) {
      sender.sendMessage(text(errorMessages.useCorrectFormat()));
      return true;
    }

    subCommandMap.get(subCommand).execute(new CommandContext(sender, args));
    return true;
  }
}
