package net.simplyvanilla.simplyrank.permission;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This service is responsible for managing the permissions of players.
 */
public class PlayerPermissionService {

    private final Map<UUID, PermissionAttachment> attachmentMap = new ConcurrentHashMap<>();

    private final Plugin plugin;
    private final PlayerDataService playerDataService;

    public PlayerPermissionService(Plugin plugin, PlayerDataService playerDataService) {
        this.plugin = plugin;
        this.playerDataService = playerDataService;
    }

    public void setPermission(Player player, String name, boolean value) {
        PermissionAttachment attachment = attachmentMap.get(player.getUniqueId());
        if (attachment == null) {
            attachment = player.addAttachment(plugin);
            attachmentMap.put(player.getUniqueId(), attachment);
        }
        attachment.setPermission(name, value);
    }

    public void clear(Player player) {
        PermissionAttachment attachment = attachmentMap.remove(player.getUniqueId());
        if (attachment != null) {
            player.removeAttachment(attachment);
        }

        this.playerDataService.invalidatePlayerData(player.getUniqueId());
    }
}
