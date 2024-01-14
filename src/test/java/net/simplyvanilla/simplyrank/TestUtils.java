package net.simplyvanilla.simplyrank;

import net.simplyvanilla.simplyrank.data.GroupPermissionService;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class TestUtils {

    public static void sleep(int ticks) {
        try {
            Thread.sleep(ticks * 50L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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
