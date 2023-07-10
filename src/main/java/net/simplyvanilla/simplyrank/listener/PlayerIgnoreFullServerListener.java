package net.simplyvanilla.simplyrank.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerIgnoreFullServerListener implements Listener {
    @EventHandler
    public void handleLogin(PlayerLoginEvent event) {
        if (!event.getResult().equals(PlayerLoginEvent.Result.KICK_FULL)
            || !event.getPlayer().hasPermission("simplyrank.joinfullserver")) return;

        event.allow();
    }
}
