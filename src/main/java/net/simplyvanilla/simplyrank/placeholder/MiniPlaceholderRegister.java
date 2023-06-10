package net.simplyvanilla.simplyrank.placeholder;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.api.utils.TagsUtils;
import java.io.IOException;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.simplyvanilla.simplyrank.SimplyRankPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class MiniPlaceholderRegister {
    private final SimplyRankPlugin plugin;

    public MiniPlaceholderRegister(SimplyRankPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        {
            Expansion.Builder builder = Expansion.builder("simplyrank");

            builder.filter(audience -> audience instanceof Player)
                .audiencePlaceholder("code_color", (audience, queue, ctx) -> {
                    Player player = (Player) audience;
                    try {
                        TextColor color = plugin.getDataManager().loadGroupDataSync(
                                plugin.getDataManager().loadPlayerDataSync(player.getUniqueId())
                                    .getPrimaryGroup())
                            .getColor();
                        return Tag.styling(builder1 -> builder1.color(color));
                    } catch (IOException ignored) {
                    }
                    return TagsUtils.EMPTY_TAG;
                })
                .audiencePlaceholder("prefix", (audience, queue, ctx) -> {
                    try {
                        return Tag.inserting(
                            miniMessage().deserialize(plugin.getDataManager().loadGroupDataSync(
                                    plugin.getDataManager()
                                        .loadPlayerDataSync(((Player) audience).getUniqueId())
                                        .getPrimaryGroup())
                                .getPrefix()));
                    } catch (IOException ignored) {
                    }
                    return TagsUtils.EMPTY_TAG;
                })
                .audiencePlaceholder("primary_rank",
                    (audience, queue, ctx) -> Tag.inserting(Component.text(plugin.getDataManager()
                        .loadPlayerDataSync(((Player) audience).getUniqueId())
                        .getPrimaryGroup())));

            builder.build().register();
        }
        {
            Expansion.Builder builder = Expansion.builder("simplyscoreboard");

            builder.filter(audience -> audience instanceof Player)
                .audiencePlaceholder("team_name", (audience, queue, ctx) -> {
                    Player player = (Player) audience;
                    return Tag.selfClosingInserting(
                        Component.text(getTeamValue(player, Team::getName, "")));
                })
                .audiencePlaceholder("team_color", (audience, queue, ctx) -> {
                    Player player = (Player) audience;
                    return Tag.styling(style -> style.color(
                        getTeamValue(player, Team::color, NamedTextColor.WHITE)));
                })
                .audiencePlaceholder("team_prefix", (audience, queue, ctx) -> {
                    Player player = (Player) audience;
                    return Tag.inserting(getTeamValue(player, Team::prefix, Component.text("")));
                })
                .audiencePlaceholder("team_suffix", (audience, queue, ctx) -> {
                    Player player = (Player) audience;
                    return Tag.inserting(getTeamValue(player, Team::suffix, Component.text("")));
                });

            builder.build().register();
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
