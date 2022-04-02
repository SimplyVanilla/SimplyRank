package net.simplyvanilla.simplyrank.data;

import com.google.gson.Gson;
import net.simplyvanilla.simplyrank.SimplyRankPlugin;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DataManager {

    private final Map<UUID, PlayerData> playerDataCache = new HashMap<>();
    private final Map<String, GroupData> groupDataCache = new HashMap<>();

    private final Gson gson;
    private final Path groupFolder;
    private final Path playerDataFolder;

    public DataManager(Gson gson, Path groupFolder, Path playerDataFolder) {
        this.gson = gson;
        this.groupFolder = groupFolder;
        this.playerDataFolder = playerDataFolder;
    }

    /**
     * <p>Loads the data from the specified user asynchronously and then runs the given callback on the main thread.</p>
     *
     * @param uuid     The player's uuid as a string
     * @param callback The callback to give the caller the ability to handle success and failure
     * @return
     */
    public void loadPlayerData(UUID uuid, IOCallback<PlayerData, IOException> callback) {
        String uuidString = uuid.toString();
        PlayerData cached = playerDataCache.get(uuid);

        if (cached != null) {
            callback.success(cached);
            return;
        }

        // Go into async
        Bukkit.getScheduler().runTaskAsynchronously(SimplyRankPlugin.getInstance(), () -> {
            try (Reader fileReader = Files.newBufferedReader(getPlayerDataFile(uuidString), StandardCharsets.UTF_8)) {
                PlayerData playerData = gson.fromJson(fileReader, PlayerData.class);

                // Switch back to sync
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> {
                    playerDataCache.put(uuid, playerData);
                    callback.success(playerData);
                });
            } catch (IOException e) {
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.error(e));
            }
        });
    }

    /**
     * <p>Load the specified group's data from disk without blocking the calling thread.</p>
     *
     * @param groupName The name of the group to load
     * @param callback  The callback which is called after the data has been loaded or failed to be loaded
     */
    public void loadGroupData(String groupName, IOCallback<GroupData, IOException> callback) {
        GroupData cached = groupDataCache.get(groupName);

        if (cached != null) {
            callback.success(cached);
            return;
        }

        // Go into async
        Bukkit.getScheduler().runTaskAsynchronously(SimplyRankPlugin.getInstance(), () -> {
            try (Reader fileReader = Files.newBufferedReader(getGroupFile(groupName), StandardCharsets.UTF_8)) {
                GroupData groupData = gson.fromJson(fileReader, GroupData.class);

                // Switch back to sync
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> {
                    groupDataCache.put(groupName, groupData);
                    callback.success(groupData);
                });
            } catch (IOException e) {
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.error(e));
            }
        });
    }

    public PlayerData loadPlayerDataSync(UUID uuid) {
        PlayerData playerData = playerDataCache.get(uuid);

        if (playerData == null) {
            try (Reader fileReader = Files.newBufferedReader(getPlayerDataFile(uuid.toString()), StandardCharsets.UTF_8)) {
                playerData = gson.fromJson(fileReader, PlayerData.class);
            } catch (IOException e) {
                playerData = PlayerData.getDefault();
            }
            playerDataCache.put(uuid, playerData);
        }

        return playerData;
    }

    public GroupData loadGroupDataSync(String groupName) throws IOException {
        GroupData groupData = groupDataCache.get(groupName);

        if (groupData == null) {
            Reader fileReader = Files.newBufferedReader(getGroupFile(groupName), StandardCharsets.UTF_8);
            groupData = gson.fromJson(fileReader, GroupData.class);
            fileReader.close();
            groupDataCache.put(groupName, groupData);
        }

        return groupData;
    }

    public void savePlayerData(String uuidString, PlayerData playerData, IOCallback<Void, IOException> callback) {
        playerDataCache.put(UUID.fromString(uuidString), playerData);
        // Go into async
        Bukkit.getScheduler().runTaskAsynchronously(SimplyRankPlugin.getInstance(), () -> {
            try {
                Files.writeString(getPlayerDataFile(uuidString), gson.toJson(playerData), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                // Switch back to sync
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.success(null));
            } catch (IOException e) {
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.error(e));
            }
        });
    }

    public void saveGroupData(String groupName, GroupData groupData, IOCallback<Void, IOException> callback) {
        groupDataCache.put(groupName, groupData);
        // Go into async
        Bukkit.getScheduler().runTaskAsynchronously(SimplyRankPlugin.getInstance(), () -> {
            try {
                Files.writeString(getGroupFile(groupName), gson.toJson(groupData), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                // Switch back to sync
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.success(null));
            } catch (IOException e) {
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.error(e));
            }
        });
    }

    public boolean groupExists(String group) {
        return Files.exists(getGroupFile(group));
    }

    private Path getPlayerDataFile(String uuidString) {
        return playerDataFolder.resolve(userDataFileName(uuidString));
    }

    private Path getGroupFile(String groupName) {
        return groupFolder.resolve(groupDataFileName(groupName));
    }

    private static String userDataFileName(String uuidString) {
        return uuidString + ".json";
    }

    private static String groupDataFileName(String groupName) {
        return groupName + ".json";
    }

}
