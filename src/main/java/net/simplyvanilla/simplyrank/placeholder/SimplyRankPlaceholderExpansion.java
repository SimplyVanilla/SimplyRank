package net.simplyvanilla.simplyrank.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
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
        return "0.5.0";
    }

    @Override
    public @Nullable String getRequiredPlugin() {
        return "SimplyRank";
    }

    @Override
    public boolean canRegister() {
        return (plugin = (SimplyRankPlugin) Bukkit.getPluginManager().getPlugin(getRequiredPlugin())) != null;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        // Can't use async in placeholder api
        switch (params) {
            case "name_color":
                try {
                    return plugin.getDataManager().loadGroupDataSync(
                            plugin.getDataManager().loadPlayerDataSync(player.getUniqueId()).getPrimaryGroup())
                        .getColor().name();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "code_color":
                try {
                    return "§" + plugin.getDataManager().loadGroupDataSync(
                            plugin.getDataManager().loadPlayerDataSync(player.getUniqueId()).getPrimaryGroup())
                        .getColor().getChar();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "prefix":
                try {
                    return plugin.getDataManager().loadGroupDataSync(
                            plugin.getDataManager().loadPlayerDataSync(player.getUniqueId()).getPrimaryGroup())
                        .getPrefix();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "primary_rank":
                return plugin.getDataManager().loadPlayerDataSync(player.getUniqueId()).getPrimaryGroup();

        }

        return null;
    }

}
