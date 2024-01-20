package net.simplyvanilla.simplyrank.placeholder;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.simplyvanilla.simplyrank.SimplyRankPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.function.Function;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public class MiniPlaceholderRegister {
    private final SimplyRankPlugin plugin;

    public MiniPlaceholderRegister(SimplyRankPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        registerRankExpansion();
        registerScoreboardExpansion();
    }

    private static void registerScoreboardExpansion() {
        Expansion.Builder builder = Expansion.builder("simplyscoreboard");

        builder
            .filter(Player.class::isInstance)
            .audiencePlaceholder(
                "team_name",
                (audience, queue, ctx) -> {
                    Player player = (Player) audience;
                    return Tag.selfClosingInserting(
                        Component.text(getTeamValue(player, Team::getName, "")));
                })
            .audiencePlaceholder(
                "team_color",
                (audience, queue, ctx) -> {
                    Player player = (Player) audience;
                    return Tag.styling(
                        style -> style.color(getTeamValue(player, Team::color, NamedTextColor.WHITE)));
                })
            .audiencePlaceholder(
                "team_prefix",
                (audience, queue, ctx) -> {
                    Player player = (Player) audience;
                    return Tag.inserting(getTeamValue(player, Team::prefix, Component.text("")));
                })
            .audiencePlaceholder(
                "team_suffix",
                (audience, queue, ctx) -> {
                    Player player = (Player) audience;
                    return Tag.inserting(getTeamValue(player, Team::suffix, Component.text("")));
                });

        builder.build().register();
    }

    private void registerRankExpansion() {
        Expansion.Builder builder = Expansion.builder("simplyrank");

        builder
            .filter(Player.class::isInstance)
            .audiencePlaceholder(
                "code_color",
                (audience, queue, ctx) -> {
                    Player player = (Player) audience;
                    TextColor color =
                        this.plugin
                            .getDataManager()
                            .loadGroupData(
                                this.plugin
                                    .getDataManager()
                                    .loadPlayerData(player.getUniqueId())
                                    .getPrimaryGroup())
                            .getColor();
                    return Tag.styling(builder1 -> builder1.color(color));
                })
            .audiencePlaceholder(
                "prefix",
                (audience, queue, ctx) ->
                    Tag.inserting(
                        miniMessage()
                            .deserialize(
                                this.plugin
                                    .getDataManager()
                                    .loadGroupData(
                                        this.plugin
                                            .getDataManager()
                                            .loadPlayerData(((Player) audience).getUniqueId())
                                            .getPrimaryGroup())
                                    .getPrefix())))
            .audiencePlaceholder(
                "primary_rank",
                (audience, queue, ctx) ->
                    Tag.inserting(
                        Component.text(
                            this.plugin
                                .getDataManager()
                                .loadPlayerData(((Player) audience).getUniqueId())
                                .getPrimaryGroup())));

        builder.build().register();
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
