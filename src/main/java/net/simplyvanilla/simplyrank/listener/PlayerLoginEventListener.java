package net.simplyvanilla.simplyrank.listener;

import net.kyori.adventure.text.Component;
import net.simplyvanilla.simplyrank.SimplyRankPlugin;
import net.simplyvanilla.simplyrank.addresswhitelist.AddressWhitelistService;
import net.simplyvanilla.simplyrank.permission.PermissionApplyService;
import net.simplyvanilla.simplyrank.proxy.ProxyService;
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
    private final ProxyService proxyService;
    private final AddressWhitelistService addressWhitelistService;

    public PlayerLoginEventListener(JavaPlugin javaPlugin, PermissionApplyService permissionApplyService, ProxyService proxyService, AddressWhitelistService addressWhitelistService) {
        this.javaPlugin = javaPlugin;
        this.permissionApplyService = permissionApplyService;
        this.proxyService = proxyService;
        this.addressWhitelistService = addressWhitelistService;
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

            if (!player.hasPermission("simplyrank.bypass.vpn") && !this.addressWhitelistService.isWhitelisted(player) && this.proxyService.isDenied(player)) {
                this.kickPlayer(player, Component.translatable("You are not allowed to join this server."));
                return;
            }

            if (Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers()
                && !player.hasPermission("simplyrank.joinfullserver")) {
                this.kickPlayer(player, Component.translatable("Server is full."));
            }
        });
    }

    private void kickPlayer(Player player, Component reason) {
        if (SimplyRankPlugin.isFolia()) {
            player.getScheduler().run(this.javaPlugin, scheduledTask -> player.kick(reason), () -> {
            });
        } else {
            Bukkit.getScheduler().runTask(this.javaPlugin, () -> player.kick(reason));
        }
    }
}
