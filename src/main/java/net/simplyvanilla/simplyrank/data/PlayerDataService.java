package net.simplyvanilla.simplyrank.data;

import net.simplyvanilla.simplyrank.data.callback.IOCallback;
import net.simplyvanilla.simplyrank.data.database.group.GroupData;
import net.simplyvanilla.simplyrank.data.database.group.GroupRepository;
import net.simplyvanilla.simplyrank.data.database.player.PlayerData;
import net.simplyvanilla.simplyrank.data.database.player.PlayerDataRepository;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service which handles the loading and saving of player data.
 */
public final class PlayerDataService {

    private final JavaPlugin javaPlugin;
    private final GroupRepository groupRepository;
    private final PlayerDataRepository playerDataRepository;

    private final Map<UUID, PlayerData> playerDataCache = new HashMap<>();
    private final Map<String, GroupData> groupDataCache = new HashMap<>();

    public PlayerDataService(JavaPlugin javaPlugin, GroupRepository groupRepository, PlayerDataRepository playerDataRepository) {
        this.javaPlugin = javaPlugin;
        this.groupRepository = groupRepository;
        this.playerDataRepository = playerDataRepository;
    }

    /**
     * Loads the data from the specified user asynchronously and then runs the given callback on the
     * main thread.
     *
     * @param uuid     The player's uuid as a string
     * @param callback The callback to give the caller the ability to handle success and failure
     * @return
     */
    public void loadPlayerDataAsync(UUID uuid, IOCallback<PlayerData, Exception> callback) {
        PlayerData cached = this.playerDataCache.get(uuid);

        if (cached != null) {
            callback.success(cached);
            return;
        }

        // Go into async
        Bukkit.getScheduler()
            .runTaskAsynchronously(
                this.javaPlugin,
                () -> {
                    try {
                        var playerData = this.playerDataRepository.findById(uuid);

                        // Switch back to sync
                        Bukkit.getScheduler()
                            .runTask(
                                this.javaPlugin,
                                () -> {
                                    this.playerDataCache.put(uuid, playerData);
                                    callback.success(playerData);
                                });
                    } catch (Exception e) {
                        callback.error(e);
                    }
                });
    }

    public PlayerData loadPlayerDataSync(UUID uuid) {
        return this.playerDataCache.computeIfAbsent(uuid, this.playerDataRepository::findById);
    }

    public GroupData loadGroupDataSync(String groupName) {
        return this.groupDataCache.computeIfAbsent(groupName, s -> this.groupRepository.findByName(groupName).orElse(null));
    }

    public void savePlayerDataAsync(
        UUID playerId, PlayerData playerData, IOCallback<Void, Exception> callback) {
        this.playerDataCache.put(playerId, playerData);
        // Go into async
        Bukkit.getScheduler()
            .runTaskAsynchronously(
                this.javaPlugin,
                () -> {
                    try {
                        this.playerDataRepository.save(
                            playerId, playerData);
                        callback.success(null);
                    } catch (Exception e) {
                        callback.error(e);
                    }
                });
    }

    public void saveGroupDataAsync(
        String groupName, GroupData groupData, IOCallback<Void, Exception> callback) {
        this.groupDataCache.put(groupName, groupData);
        // Go into async
        Bukkit.getScheduler()
            .runTaskAsynchronously(
                this.javaPlugin,
                () -> {
                    try {
                        this.groupRepository.save(groupName, groupData);
                        callback.success(null);
                    } catch (Exception e) {
                        callback.error(e);
                    }
                });
    }

    public boolean groupExists(String groupName) {
        return groupRepository.existsByName(groupName);
    }

    public void invalidatePlayerData(UUID uniqueId) {
        this.playerDataCache.remove(uniqueId);
    }
}
