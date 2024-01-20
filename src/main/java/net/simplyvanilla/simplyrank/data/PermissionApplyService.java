package net.simplyvanilla.simplyrank.data;

import net.simplyvanilla.simplyrank.data.callback.IOCallback;
import net.simplyvanilla.simplyrank.data.database.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PermissionApplyService {

    private final PlayerDataService playerDataService;
    private final PlayerPermissionService playerPermissionService;
    private final GroupPermissionService groupPermissionService;

    public PermissionApplyService(
        PlayerDataService playerDataService,
        PlayerPermissionService playerPermissionService,
        GroupPermissionService groupPermissionService) {
        this.playerDataService = playerDataService;
        this.playerPermissionService = playerPermissionService;
        this.groupPermissionService = groupPermissionService;
    }

    public void apply(Player player) {
        this.apply(player, () -> {
        });
    }

    public void apply(Player player, Runnable callback) {
        UUID uuid = player.getUniqueId();
        playerDataService.loadPlayerDataAsync(
            uuid,
            new IOCallback<>() {
                @Override
                public void success(PlayerData data) {
                    Player player = Bukkit.getPlayer(uuid);

                    if (player == null || data == null) {
                        callback.run();
                        return;
                    }
                    applyPermission(player, data);
                    callback.run();
                }

                @Override
                public void error(Exception error) {
                    // No important erros here
                    callback.run();
                }
            });
    }

    public void applyPermission(Player player, PlayerData data) {
        String group = data.getPrimaryGroup();
        playerPermissionService.clear(player);
        groupPermissionService
            .getPermissions(group)
            .forEach((k, v) -> playerPermissionService.setPermission(player, k, v));

        player.recalculatePermissions();
        player.updateCommands();
    }
}
