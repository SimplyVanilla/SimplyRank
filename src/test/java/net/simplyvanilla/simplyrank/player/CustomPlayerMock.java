package net.simplyvanilla.simplyrank.player;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CustomPlayerMock extends PlayerMock {
    public CustomPlayerMock(@NotNull ServerMock server, @NotNull String name) {
        super(server, name);
    }

    public CustomPlayerMock(@NotNull ServerMock server, @NotNull String name, @NotNull UUID uuid) {
        super(server, name, uuid);
    }

    @Override
    public void kick(@Nullable Component message, PlayerKickEvent.@NotNull Cause cause) {
        if (!isOnline()) return;
        PlayerKickEvent event =
            new PlayerKickEvent(this,
                Component.text("Plugin"),
                message == null ? net.kyori.adventure.text.Component.empty() : message,
                cause);

        Bukkit.getPluginManager().callEvent(event);
        server.getPlayerList().disconnectPlayer(this);
    }
}
