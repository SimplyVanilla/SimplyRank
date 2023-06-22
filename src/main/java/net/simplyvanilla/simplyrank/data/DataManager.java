package net.simplyvanilla.simplyrank.data;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.simplyvanilla.simplyrank.SimplyRankPlugin;
import org.bukkit.Bukkit;

public final class DataManager {

    private SQLRepository repository;

    private final Map<UUID, PlayerData> playerDataCache = new HashMap<>();
    private final Map<String, GroupData> groupDataCache = new HashMap<>();

    public DataManager(Gson gson, SQLHandler sqlHandler) {
        repository = new SQLRepository(sqlHandler, gson);
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
        PlayerData cached = playerDataCache.get(uuid);

        if (cached != null) {
            callback.success(cached);
            return;
        }

        // Go into async
        Bukkit.getScheduler()
            .runTaskAsynchronously(
                SimplyRankPlugin.getInstance(),
                () -> {
                    var playerData = repository.loadPlayerData(uuid, callback);

                    // Switch back to sync
                    Bukkit.getScheduler()
                        .runTask(
                            SimplyRankPlugin.getInstance(),
                            () -> {
                                playerDataCache.put(uuid, playerData);
                                callback.success(playerData);
                            });
                });
    }

    /**
     * Load the specified group's data from disk without blocking the calling thread.
     *
     * @param groupName The name of the group to load
     * @param callback  The callback which is called after the data has been loaded or failed to be
     *                  loaded
     */
    public void loadGroupDataAsync(String groupName, IOCallback<GroupData, Exception> callback) {
        GroupData cached = groupDataCache.get(groupName);

        if (cached != null) {
            callback.success(cached);
            return;
        }

        // Go into async
        Bukkit.getScheduler()
            .runTaskAsynchronously(
                SimplyRankPlugin.getInstance(),
                () -> {
                    var groupData = repository.loadGroupData(groupName, callback);
                    if (groupData == null) {
                        callback.error(new Exception("Group data was null"));
                        return;
                    }
                    // Switch back to sync
                    Bukkit.getScheduler()
                        .runTask(
                            SimplyRankPlugin.getInstance(),
                            () -> {
                                groupDataCache.put(groupName, groupData);
                                callback.success(groupData);
                            });
                });
    }

    public PlayerData loadPlayerDataSync(UUID uuid) {
        return playerDataCache.computeIfAbsent(uuid, k -> repository.loadPlayerData(k, null));
    }

    public GroupData loadGroupDataSync(String groupName) {
        return groupDataCache.computeIfAbsent(groupName, s -> repository.loadGroupData(s, null));
    }

    public void savePlayerDataAsync(
        String uuidString, PlayerData playerData, IOCallback<Void, Exception> callback) {
        playerDataCache.put(UUID.fromString(uuidString), playerData);
        // Go into async
        Bukkit.getScheduler()
            .runTaskAsynchronously(
                SimplyRankPlugin.getInstance(),
                () -> repository.savePlayerData(uuidString, playerData, callback));
    }

    public void saveGroupDataAsync(
        String groupName, GroupData groupData, IOCallback<Void, Exception> callback) {
        groupDataCache.put(groupName, groupData);
        // Go into async
        Bukkit.getScheduler()
            .runTaskAsynchronously(
                SimplyRankPlugin.getInstance(),
                () -> repository.saveGroupData(groupName, groupData, callback));
    }

    public boolean groupExists(String groupName) {
        return repository.groupExists(groupName);
    }

    public void invalidatePlayerData(UUID uniqueId) {
        this.playerDataCache.remove(uniqueId);
    }
}
