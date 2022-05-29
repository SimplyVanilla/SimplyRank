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
        return "zvwild";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.2.4";
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
        if (params.equals("name_color")) {
            try {
                return plugin.getDataManager().loadGroupDataSync(
                                plugin.getDataManager().loadPlayerDataSync(player.getUniqueId()).getPrimaryGroup())
                        .getColor().name();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (params.equals("code_color")) {
            try {
                return "§" + plugin.getDataManager().loadGroupDataSync(
                                plugin.getDataManager().loadPlayerDataSync(player.getUniqueId()).getPrimaryGroup())
                        .getColor().getChar();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (params.equals("prefix")) {
            try {
                return plugin.getDataManager().loadGroupDataSync(
                                plugin.getDataManager().loadPlayerDataSync(player.getUniqueId()).getPrimaryGroup())
                        .getPrefix();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (params.equals("primary_rank")) {
            return plugin.getDataManager().loadPlayerDataSync(player.getUniqueId()).getPrimaryGroup();

        }
        return null;
    }

}
