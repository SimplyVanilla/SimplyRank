package net.simplyvanilla.simplyrank.listener;

import net.kyori.adventure.text.Component;
import net.simplyvanilla.simplyrank.permission.PermissionApplyService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerLoginEventListener implements Listener {

    private final JavaPlugin javaPlugin;
    private final PermissionApplyService permissionApplyService;

    public PlayerLoginEventListener(JavaPlugin javaPlugin, PermissionApplyService permissionApplyService) {
        this.javaPlugin = javaPlugin;
        this.permissionApplyService = permissionApplyService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleLogin(PlayerLoginEvent event) {
        if (!event.getResult().equals(PlayerLoginEvent.Result.KICK_FULL)) return;
        event.allow();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getAsyncScheduler().runNow(this.javaPlugin, (task) -> {
            this.permissionApplyService.apply(player);

            if (Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers()
                && !player.hasPermission("simplyrank.joinfullserver")) {
                player.kick(Component.translatable("Server is full."));
            }
        });

    }
}
