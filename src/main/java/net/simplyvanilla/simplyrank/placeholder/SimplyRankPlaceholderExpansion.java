package net.simplyvanilla.simplyrank.placeholder;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.simplyvanilla.simplyrank.SimplyRankPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class SimplyRankPlaceholderExpansion extends PlaceholderExpansion {

    private SimplyRankPlugin plugin;

    @Override
    public @NotNull String getIdentifier() {
        return "simplyrank";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Simply Vanilla";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.5.1";
    }

    @Override
    public @Nullable String getRequiredPlugin() {
        return "SimplyRank";
    }

    @Override
    public boolean canRegister() {
        return (plugin =
            (SimplyRankPlugin) Bukkit.getPluginManager().getPlugin(getRequiredPlugin())) != null;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        // Can't use async in placeholder api
        if (params.equals("name_color")) {
            try {
                return plugin.getDataManager().loadGroupDataSync(
                        plugin.getDataManager().loadPlayerDataSync(player.getUniqueId())
                            .getPrimaryGroup())
                    .getColor().asHexString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (params.equals("code_color")) {
            try {
                return ChatColor.of(plugin.getDataManager().loadGroupDataSync(
                        plugin.getDataManager().loadPlayerDataSync(player.getUniqueId())
                            .getPrimaryGroup())
                    .getColor().asHexString()).toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (params.equals("prefix")) {
            try {
                return LegacyComponentSerializer.legacySection()
                    .serialize(miniMessage().deserialize(plugin.getDataManager().loadGroupDataSync(
                            plugin.getDataManager().loadPlayerDataSync(player.getUniqueId())
                                .getPrimaryGroup())
                        .getPrefix()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (params.equals("primary_rank")) {
            return plugin.getDataManager().loadPlayerDataSync(player.getUniqueId())
                .getPrimaryGroup();

        }
        return null;
    }

}
