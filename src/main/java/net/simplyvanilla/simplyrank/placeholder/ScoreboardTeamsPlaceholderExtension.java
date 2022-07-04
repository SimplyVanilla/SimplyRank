package net.simplyvanilla.simplyrank.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ScoreboardTeamsPlaceholderExtension extends PlaceholderExpansion {

    private final String teamNameDecoratedFormat;

    public ScoreboardTeamsPlaceholderExtension(String teamNameDecoratedFormat) {
        this.teamNameDecoratedFormat = teamNameDecoratedFormat;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "simplyscoreboard";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Simply Vanilla";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.3.1";
    }

    @Override
    public @NotNull String getName() {
        return "SimplyRank-ScoreboardTeams";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offPlayer, @NotNull String params) {
        if (offPlayer instanceof Player player) {
            return switch (params) {
                case "team_name" -> getTeamValue(player, Team::getName, "");
                case "team_name_decorated" -> getTeamValue(player, t -> String.format(PlaceholderAPI.setPlaceholders(player, teamNameDecoratedFormat), t.getName()), "");
                case "team_color" -> getTeamValue(player, Team::getColor, ChatColor.WHITE).toString();
                case "team_prefix" -> LegacyComponentSerializer.legacySection()
                    .serialize(getTeamValue(player, Team::prefix, Component.text("")));
                case "team_suffix" -> LegacyComponentSerializer.legacySection()
                    .serialize(getTeamValue(player, Team::suffix, Component.text("")));
                default -> null;
            };
        } else {
            return null;
        }
    }

    private static <T> T getTeamValue(Player player, Function<Team, T> f, T def) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getEntityTeam(player);

        if (team == null) {
            return def;
        } else {
            return f.apply(team);
        }
    }

}
