package net.simplyvanilla.simplyrank.listener;

import net.simplyvanilla.simplyrank.data.PlayerPermissionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitEventListener implements Listener {

    private final PlayerPermissionManager playerPermissionManager;

    public PlayerQuitEventListener(PlayerPermissionManager playerPermissionManager) {
        this.playerPermissionManager = playerPermissionManager;
    }

    @EventHandler
    public void handlePlayerQuitEvent(PlayerQuitEvent event) {
        playerPermissionManager.clear(event.getPlayer());
    }
}
