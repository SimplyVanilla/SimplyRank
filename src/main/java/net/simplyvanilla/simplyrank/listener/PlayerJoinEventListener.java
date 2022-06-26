package net.simplyvanilla.simplyrank.listener;

import net.simplyvanilla.simplyrank.utils.PermissionApplier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinEventListener implements Listener {

    private final PermissionApplier permissionApplier;

    public PlayerJoinEventListener(PermissionApplier permissionApplier) {
        this.permissionApplier = permissionApplier;
    }

    @EventHandler
    public void handlePlayerJoinEvent(PlayerJoinEvent event) {
        permissionApplier.apply(event.getPlayer());
    }

}
