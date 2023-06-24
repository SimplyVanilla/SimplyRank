package net.simplyvanilla.simplyrank.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerUtils {
    private PlayerUtils() {
    }

    public static UUID resolveUuid(String input) {

    /*
       Checking, whether a literal UUID was given as an input
    */
        String regex =
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"; // According to the web,
        // I'll admit
        // Using regex to check for UUIDs (lower case to make it case-insensitive)
        if (input.length() == 36 && input.toLowerCase().matches(regex)) return UUID.fromString(input);

    /*
       Checking, whether a player was referenced
    */

        UUID uuid = null;
        Player target = Bukkit.getPlayer(input);
        if (target != null) {
            uuid = target.getUniqueId();
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(input);
            if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                uuid = offlinePlayer.getUniqueId();
            }
        }

        return uuid;
    }
}
