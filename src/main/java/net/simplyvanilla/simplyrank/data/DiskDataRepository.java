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
import java.util.UUID;

public class DiskDataRepository implements DataRepository {

    private final Gson gson;
    private final Path groupFolder;
    private final Path playerDataFolder;

    public DiskDataRepository(Gson gson, Path groupFolder, Path playerDataFolder) {
        this.gson = gson;
        this.groupFolder = groupFolder;
        this.playerDataFolder = playerDataFolder;
    }

    @Override
    public PlayerData loadPlayerData(UUID uuid, IOCallback<PlayerData, Exception> callback) {

        String uuidString = uuid.toString();

        try (Reader fileReader = Files.newBufferedReader(getPlayerDataFile(uuidString), StandardCharsets.UTF_8)) {
            PlayerData playerData = gson.fromJson(fileReader, PlayerData.class);
            return playerData;
        } catch (IOException e) {
            if (callback != null)
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.error(e));
            return PlayerData.getDefault();
        }
    }

    @Override
    public GroupData loadGroupData(String groupName, IOCallback<GroupData, Exception> callback) {

        try (Reader fileReader = Files.newBufferedReader(getGroupFile(groupName), StandardCharsets.UTF_8)) {
            GroupData groupData = gson.fromJson(fileReader, GroupData.class);
            return groupData;
        } catch (IOException e) {
            if (callback != null)
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.error(e));
        }

        return null;
    }

    @Override
    public void savePlayerData(String uuidString, PlayerData playerData, IOCallback<Void, Exception> callback) {
        try {
            Files.writeString(getPlayerDataFile(uuidString), gson.toJson(playerData), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            if (callback != null)
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.success(null));

        } catch (IOException e) {
            if (callback != null)
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.error(e));
            else
                e.printStackTrace(); //Not optimal but it should not go unnoticed.
        }
    }

    @Override
    public void saveGroupData(String groupName, GroupData groupData, IOCallback<Void, Exception> callback) {
        try {
            Files.writeString(getGroupFile(groupName), gson.toJson(groupData), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            // Switch back to sync
            if (callback != null)
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.success(null));
        } catch (IOException e) {
            if (callback != null)
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.error(e));
            else
                e.printStackTrace(); //Not optimal but it should not go unnoticed.
        }
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
