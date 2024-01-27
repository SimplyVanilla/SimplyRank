package net.simplyvanilla.simplyrank.command.address;

import net.simplyvanilla.simplyrank.addresswhitelist.AddressWhitelistService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class AddressWhitelistCommand implements CommandExecutor {
    private static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final AddressWhitelistService service;

    public AddressWhitelistCommand(AddressWhitelistService service) {
        this.service = service;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        UUID senderId = sender instanceof Player player ? player.getUniqueId() : CONSOLE_UUID;

        if (args.length != 2) {
            this.printHelp(sender);
        } else if ("add".equals(args[0])) {
            this.handleAddCommand(sender, args[1], senderId);
        } else if ("remove".equals(args[0])) {
            this.handleRemoveCommand(sender, args[1]);
        } else {
            this.printHelp(sender);
        }
        return true;
    }

    private void printHelp(CommandSender sender) {
        sender.sendMessage(text("Usage: /vpn-whitelist <add|remove> <address>"));
    }

    private boolean validateAddress(String address) {
        // check if address is valid ipv4 or ipv6 address
        if (address.contains(":")) {
            // ipv6
            return address.matches("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
        } else {
            // ipv4
            return address.matches("^(?:\\d{1,3}\\.){3}\\d{1,3}$");
        }
    }

    private void handleRemoveCommand(CommandSender sender, String address) {
        this.service.removeAddress(address);
        sender.sendMessage(text("Removed address " + address));
    }

    private void handleAddCommand(CommandSender sender, String address, UUID invokerId) {
        if (!this.validateAddress(address)) {
            sender.sendMessage(text("Invalid address " + address));
            return;
        }
        this.service.addAddress(address, invokerId);
        sender.sendMessage(text("Added address " + address));
    }
}
