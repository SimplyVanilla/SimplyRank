package net.simplyvanilla.simplyrank;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.simplyvanilla.simplyrank.command.SimplyRankCommandExecutor;
import net.simplyvanilla.simplyrank.data.DataManager;
import net.simplyvanilla.simplyrank.data.GroupData;
import net.simplyvanilla.simplyrank.data.IOCallback;
import net.simplyvanilla.simplyrank.gson.ChatColorGsonDeserializer;
import net.simplyvanilla.simplyrank.placeholder.SimplyRankPlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SimplyRankPlugin extends JavaPlugin {

    private static SimplyRankPlugin instance;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(new TypeToken<ChatColor>(){}.getType(), new ChatColorGsonDeserializer())
                .create();

        File dataFolder = getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        Path dataFolderPath = dataFolder.toPath();

        Path groupFolder = dataFolderPath.resolve("groups");

        if (!Files.exists(groupFolder)) {
            try {
                Files.createDirectories(groupFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Path playerFolder = dataFolderPath.resolve("players");
        if (!Files.exists(playerFolder)) {
            try {
                Files.createDirectories(playerFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dataManager = new DataManager(gson, groupFolder, playerFolder);

        if (!dataManager.groupExists("default")) {
            GroupData defaultData = new GroupData(ChatColor.GRAY, "Member ");
            dataManager.saveGroupDataAsync("default", defaultData, new IOCallback<>() {
                @Override
                public void success(Void data) {
                    getLogger().info("Successfully created default group!");
                }

                @Override
                public void error(IOException error) {
                    getLogger().info("There was an error creating the default group");
                }
            });
        }

        getCommand("simplyrank").setExecutor(new SimplyRankCommandExecutor(dataManager));

        new SimplyRankPlaceholderExpansion().register();
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public static SimplyRankPlugin getInstance() {
        return instance;
    }

}
