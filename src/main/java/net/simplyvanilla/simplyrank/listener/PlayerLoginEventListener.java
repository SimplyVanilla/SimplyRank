package net.simplyvanilla.simplyrank.listener;

import net.kyori.adventure.text.Component;
import net.simplyvanilla.simplyrank.utils.PermissionApplier;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginEventListener implements Listener {

    private final PermissionApplier permissionApplier;

    public PlayerLoginEventListener(PermissionApplier permissionApplier) {
        this.permissionApplier = permissionApplier;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleLogin(PlayerLoginEvent event) {
        if (!event.getResult().equals(PlayerLoginEvent.Result.KICK_FULL)) return;
        event.allow();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleJoin(PlayerJoinEvent event) {
        this.permissionApplier.apply(event.getPlayer());

        if (Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers() && !event.getPlayer().hasPermission("simplyrank.joinfullserver")) {
            event.getPlayer().kick(Component.translatable("Server is full."));
        }
    }
}
