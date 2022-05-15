package net.simplyvanilla.simplyrank.data;

import com.google.gson.Gson;
import net.simplyvanilla.simplyrank.SimplyRankPlugin;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DataManager {

    private DataRepository repository;

    final Map<UUID, PlayerData> playerDataCache = new HashMap<>();
    final Map<String, GroupData> groupDataCache = new HashMap<>();

    public DataManager(Gson gson, Path groupFolder, Path playerDataFolder) {
        repository = new DiskDataRepository(gson, groupFolder, playerDataFolder);
    }

    /**
     * <p>Loads the data from the specified user asynchronously and then runs the given callback on the main thread.</p>
     *
     * @param uuid     The player's uuid as a string
     * @param callback The callback to give the caller the ability to handle success and failure
     * @return
     */
    public void loadPlayerDataAsync(UUID uuid, IOCallback<PlayerData, IOException> callback) {
        PlayerData cached = playerDataCache.get(uuid);

        if (cached != null) {
            callback.success(cached);
            return;
        }

        // Go into async
        Bukkit.getScheduler().runTaskAsynchronously(SimplyRankPlugin.getInstance(), () -> {

            var playerData = repository.loadPlayerData(uuid, callback);

            // Switch back to sync
            Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> {
                playerDataCache.put(uuid, playerData);
                callback.success(playerData);
            });


        });
    }

    /**
     * <p>Load the specified group's data from disk without blocking the calling thread.</p>
     *
     * @param groupName The name of the group to load
     * @param callback  The callback which is called after the data has been loaded or failed to be loaded
     */
    public void loadGroupDataAsync(String groupName, IOCallback<GroupData, IOException> callback) {
        GroupData cached = groupDataCache.get(groupName);

        if (cached != null) {
            callback.success(cached);
            return;
        }

        // Go into async
        Bukkit.getScheduler().runTaskAsynchronously(SimplyRankPlugin.getInstance(), () -> {

            var groupData = repository.loadGroupData(groupName, callback);
            // Switch back to sync
            Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> {
                groupDataCache.put(groupName, groupData);
                callback.success(groupData);
            });

        });
    }

    public PlayerData loadPlayerDataSync(UUID uuid) {
        PlayerData playerData = playerDataCache.get(uuid);

        if (playerData == null) {
            playerData = repository.loadPlayerData(uuid, null);
            playerDataCache.put(uuid, playerData);
        }

        return playerData;
    }

    public GroupData loadGroupDataSync(String groupName) throws IOException {
        GroupData groupData = groupDataCache.get(groupName);

        if (groupData == null) {
            groupData = repository.loadGroupData(groupName, null);
            groupDataCache.put(groupName, groupData);
        }

        return groupData;
    }

    public void savePlayerDataAsync(String uuidString, PlayerData playerData, IOCallback<Void, IOException> callback) {
        playerDataCache.put(UUID.fromString(uuidString), playerData);
        // Go into async
        Bukkit.getScheduler().runTaskAsynchronously(SimplyRankPlugin.getInstance(), () -> {
            repository.savePlayerData(uuidString, playerData, callback);
        });
    }

    public void saveGroupDataAsync(String groupName, GroupData groupData, IOCallback<Void, IOException> callback) {
        groupDataCache.put(groupName, groupData);
        // Go into async
        Bukkit.getScheduler().runTaskAsynchronously(SimplyRankPlugin.getInstance(), () -> {
            repository.saveGroupData(groupName, groupData, callback);
        });
    }

    public boolean groupExists(String groupName) {
        return repository.groupExists(groupName);
    }

}
