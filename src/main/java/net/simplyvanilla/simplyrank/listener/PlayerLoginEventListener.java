package net.simplyvanilla.simplyrank.listener;

import net.simplyvanilla.simplyrank.utils.PermissionApplier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginEventListener implements Listener {

    private final PermissionApplier permissionApplier;

    public PlayerLoginEventListener(PermissionApplier permissionApplier) {
        this.permissionApplier = permissionApplier;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleLogin(PlayerLoginEvent event) {
        if (!event.getResult().equals(PlayerLoginEvent.Result.KICK_FULL)) return;

        this.permissionApplier.apply(event.getPlayer());

        if (!event.getPlayer().hasPermission("simplyrank.joinfullserver")) return;

        event.allow();
    }
}
