package net.simplyvanilla.simplyrank;

import net.simplyvanilla.simplyrank.permission.GroupPermissionService;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class TestUtils {

    public static GroupPermissionService createDefaultGroupPermissions() {
        GroupPermissionService manager = new GroupPermissionService();
        manager.setPermission("default", "minecraft.command.me", true);
        manager.setPermission("default", "minecraft.command.tell", false);
        manager.setPermission("default", "minecraft.command.help", true);
        return manager;
    }

    public static CompletableFuture<Void> waitFor(int ticks, Runnable runnable, JavaPlugin plugin) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            runnable.run();
            future.complete(null);
        }, ticks);
        return future;
    }
}
