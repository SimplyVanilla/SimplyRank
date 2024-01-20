package net.simplyvanilla.simplyrank.data;

import net.simplyvanilla.simplyrank.SimplyRankPlugin;
import net.simplyvanilla.simplyrank.data.database.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class PermissionApplyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionApplyService.class);

    private final JavaPlugin javaPlugin;
    private final PlayerDataService playerDataService;
    private final PlayerPermissionService playerPermissionService;
    private final GroupPermissionService groupPermissionService;

    public PermissionApplyService(
        JavaPlugin javaPlugin,
        PlayerDataService playerDataService,
        PlayerPermissionService playerPermissionService,
        GroupPermissionService groupPermissionService) {
        this.javaPlugin = javaPlugin;
        this.playerDataService = playerDataService;
        this.playerPermissionService = playerPermissionService;
        this.groupPermissionService = groupPermissionService;
    }

    public void apply(Player player) {
        UUID uuid = player.getUniqueId();
        try {
            PlayerData playerData = this.playerDataService.loadPlayerData(uuid);

            if (playerData == null) {
                return;
            }

            // We have to check if the server is running folia because paper doesn't support player based schedulers.
            if (SimplyRankPlugin.isFolia()) {
                // We have to run this on the thread of the player, otherwise it will not work properly
                player.getScheduler().run(this.javaPlugin, task -> this.applyPermission(player, playerData), () -> {
                });
            } else {
                this.applyPermission(player, playerData);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load data for player {}", uuid, e);
        }
    }

    public void applyPermission(Player player, PlayerData data) {
        String group = data.getPrimaryGroup();
        this.playerPermissionService.clear(player);
        this.groupPermissionService
            .getPermissions(group)
            .forEach((k, v) -> this.playerPermissionService.setPermission(player, k, v));

        player.recalculatePermissions();
        player.updateCommands();
    }
}
