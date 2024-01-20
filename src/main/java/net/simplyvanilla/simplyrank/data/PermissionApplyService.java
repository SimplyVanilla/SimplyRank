package net.simplyvanilla.simplyrank.data;

import net.simplyvanilla.simplyrank.data.database.player.PlayerData;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class PermissionApplyService {
    private final static Logger LOGGER = LoggerFactory.getLogger(PermissionApplyService.class);

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
        UUID uuid = player.getUniqueId();
        try {
            PlayerData playerData = this.playerDataService.loadPlayerData(uuid);

            if (playerData == null) {
                return;
            }

            this.applyPermission(player, playerData);
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
