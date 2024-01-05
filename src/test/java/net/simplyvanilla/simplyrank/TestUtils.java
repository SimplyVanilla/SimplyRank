package net.simplyvanilla.simplyrank;

import net.simplyvanilla.simplyrank.data.GroupPermissionManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class TestUtils {

    public static GroupPermissionManager createDefaultGroupPermissions() {
        GroupPermissionManager manager = new GroupPermissionManager();
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
