package net.simplyvanilla.simplyrank.data;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerPermissionManager {

    private final Map<UUID, PermissionAttachment> attachmentMap = new HashMap<>();

    private final Plugin plugin;

    public PlayerPermissionManager(Plugin plugin) {
        this.plugin = plugin;
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
    }
}
