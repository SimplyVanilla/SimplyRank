package net.simplyvanilla.simplyrank.listener;

import net.simplyvanilla.simplyrank.data.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinEventListener implements Listener {

    private final DataManager dataManager;
    private final PlayerPermissionManager playerPermissionManager;
    private final GroupPermissionManager groupPermissionManager;

    public PlayerJoinEventListener(DataManager dataManager, PlayerPermissionManager playerPermissionManager, GroupPermissionManager groupPermissionManager) {
        this.dataManager = dataManager;
        this.playerPermissionManager = playerPermissionManager;
        this.groupPermissionManager = groupPermissionManager;
    }

    @EventHandler
    public void handlePlayerJoinEvent(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        dataManager.loadPlayerDataAsync(uuid, new IOCallback<>() {
            @Override
            public void success(PlayerData data) {
                Player player = Bukkit.getPlayer(uuid);

                if (player == null) {
                    return;
                }

                String group = data.getPrimaryGroup();
                groupPermissionManager.getPermissions(group).forEach((k, v) ->
                    playerPermissionManager.setPermission(player, k, v));
            }

            @Override
            public void error(Exception error) {
                // No important erros here
            }
        });
    }

}
