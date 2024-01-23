package net.simplyvanilla.simplyrank.listener;

import net.simplyvanilla.simplyrank.permission.PlayerPermissionService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitEventListener implements Listener {

    private final PlayerPermissionService playerPermissionService;

    public PlayerQuitEventListener(PlayerPermissionService playerPermissionService) {
        this.playerPermissionService = playerPermissionService;
    }

    @EventHandler
    public void handlePlayerQuitEvent(PlayerQuitEvent event) {
        playerPermissionService.clear(event.getPlayer());
    }
}
