package net.simplyvanilla.simplyrank.command;

import net.simplyvanilla.simplyrank.command.impl.*;
import net.simplyvanilla.simplyrank.data.DataManager;
import net.simplyvanilla.simplyrank.utils.PermissionApplier;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static net.kyori.adventure.text.Component.text;

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
        if (!(sender instanceof ConsoleCommandSender) && !(sender instanceof RemoteConsoleCommandSender)) {
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
