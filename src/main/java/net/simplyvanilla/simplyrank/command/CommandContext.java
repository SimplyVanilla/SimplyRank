package net.simplyvanilla.simplyrank.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class CommandContext {
    private final CommandSender sender;
    private final String[] arguments;

    public CommandContext(CommandSender sender, String[] arguments) {
        this.sender = sender;
        this.arguments = arguments;
    }

    public Optional<String> getOptionalArgument(int index) {
        if (index >= arguments.length) {
            return Optional.empty();
        }
        return Optional.of(arguments[index]);
    }

    public String getArgument(int index) {
        return arguments[index];
    }

    public CommandSender getSender() {
        return sender;
    }

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public Player getAsPlayer() {
        return (Player) sender;
    }

    public String[] getArguments() {
        return arguments;
    }
}
