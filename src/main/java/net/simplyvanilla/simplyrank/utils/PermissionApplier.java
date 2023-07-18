package net.simplyvanilla.simplyrank.utils;

import net.simplyvanilla.simplyrank.data.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PermissionApplier {

    private final DataManager dataManager;
    private final PlayerPermissionManager playerPermissionManager;
    private final GroupPermissionManager groupPermissionManager;

    public PermissionApplier(
        DataManager dataManager,
        PlayerPermissionManager playerPermissionManager,
        GroupPermissionManager groupPermissionManager) {
        this.dataManager = dataManager;
        this.playerPermissionManager = playerPermissionManager;
        this.groupPermissionManager = groupPermissionManager;
    }

    public void apply(Player player) {
        apply(player, () -> {
        });
    }

    public void apply(Player player, Runnable callback) {
        UUID uuid = player.getUniqueId();
        dataManager.loadPlayerDataAsync(
            uuid,
            new IOCallback<>() {
                @Override
                public void success(PlayerData data) {
                    Player player = Bukkit.getPlayer(uuid);

                    if (player == null || data == null) {
                        callback.run();
                        return;
                    }

                    String group = data.getPrimaryGroup();
                    playerPermissionManager.clear(player);
                    groupPermissionManager
                        .getPermissions(group)
                        .forEach((k, v) -> playerPermissionManager.setPermission(player, k, v));

                    player.recalculatePermissions();
                    player.updateCommands();

                    callback.run();
                }

                @Override
                public void error(Exception error) {
                    // No important erros here
                    callback.run();
                }
            });
    }
}
