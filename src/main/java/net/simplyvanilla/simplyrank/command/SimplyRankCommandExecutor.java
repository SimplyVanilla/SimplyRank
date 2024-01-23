package net.simplyvanilla.simplyrank.command;

import net.simplyvanilla.simplyrank.command.impl.*;
import net.simplyvanilla.simplyrank.permission.PlayerDataService;
import net.simplyvanilla.simplyrank.permission.PermissionApplyService;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static net.kyori.adventure.text.Component.text;

public class SimplyRankCommandExecutor implements CommandExecutor {
    private final PlayerDataService playerDataService;
    private final PermissionApplyService permissionApplyService;
    private final CommandErrorMessages errorMessages;
    private final Map<String, SubCommand> subCommandMap = new HashMap<>();

    public SimplyRankCommandExecutor(PlayerDataService playerDataService, PermissionApplyService permissionApplyService) {
        this.playerDataService = playerDataService;
        this.permissionApplyService = permissionApplyService;
        this.errorMessages = new CommandErrorMessages();

        this.subCommandMap.put(
            "create",
            new CreateGroupCommand(this.errorMessages, this.playerDataService, this.permissionApplyService));
        this.subCommandMap.put(
            "set", new SetCommand(this.errorMessages, this.playerDataService, this.permissionApplyService));
        this.subCommandMap.put(
            "add", new AddCommand(this.errorMessages, this.playerDataService, this.permissionApplyService));
        this.subCommandMap.put(
            "rem", new RemoveCommand(this.errorMessages, this.playerDataService, this.permissionApplyService));
        this.subCommandMap.put(
            "get", new GetCommand(this.errorMessages, this.playerDataService, this.permissionApplyService));
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
