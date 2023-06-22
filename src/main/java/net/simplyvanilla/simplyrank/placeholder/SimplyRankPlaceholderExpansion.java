package net.simplyvanilla.simplyrank.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.simplyvanilla.simplyrank.SimplyRankPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

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
        return "0.6.0";
    }

    @Override
    public @Nullable String getRequiredPlugin() {
        return "SimplyRank";
    }

    @Override
    public boolean canRegister() {
        return (plugin = (SimplyRankPlugin) Bukkit.getPluginManager().getPlugin(getRequiredPlugin()))
            != null;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        // Can't use async in placeholder api
        if (params.equals("name_color")) {
            return plugin
                .getDataManager()
                .loadGroupDataSync(
                    plugin.getDataManager().loadPlayerDataSync(player.getUniqueId()).getPrimaryGroup())
                .getColor()
                .asHexString();
        } else if (params.equals("code_color")) {
            return ChatColor.of(
                    plugin
                        .getDataManager()
                        .loadGroupDataSync(
                            plugin
                                .getDataManager()
                                .loadPlayerDataSync(player.getUniqueId())
                                .getPrimaryGroup())
                        .getColor()
                        .asHexString())
                .toString();
        } else if (params.equals("prefix")) {
            return LegacyComponentSerializer.legacySection()
                .serialize(
                    miniMessage()
                        .deserialize(
                            plugin
                                .getDataManager()
                                .loadGroupDataSync(
                                    plugin
                                        .getDataManager()
                                        .loadPlayerDataSync(player.getUniqueId())
                                        .getPrimaryGroup())
                                .getPrefix()));
        } else if (params.equals("primary_rank")) {
            return plugin.getDataManager().loadPlayerDataSync(player.getUniqueId()).getPrimaryGroup();
        }
        return null;
    }
}
