package net.simplyvanilla.simplyrank;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.simplyvanilla.simplyrank.command.SimplyRankCommandExecutor;
import net.simplyvanilla.simplyrank.data.*;
import net.simplyvanilla.simplyrank.gson.ChatColorGsonDeserializer;
import net.simplyvanilla.simplyrank.listener.PlayerJoinEventListener;
import net.simplyvanilla.simplyrank.listener.PlayerQuitEventListener;
import net.simplyvanilla.simplyrank.placeholder.ScoreboardTeamsPlaceholderExtension;
import net.simplyvanilla.simplyrank.placeholder.SimplyRankPlaceholderExpansion;
import net.simplyvanilla.simplyrank.utils.PermissionApplier;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class SimplyRankPlugin extends JavaPlugin {

    private static SimplyRankPlugin instance;
    private DataManager dataManager;
    private SQLHandler sqlHandler = null;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        instance = this;

        try {
            config = loadConfig("config.yml");
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().log(Level.SEVERE, "Could not load config file! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        boolean useSql = config.getBoolean("mysql.active");

        if (useSql) {
            try {
                sqlHandler = createSQLHandlerFromConfig(config);
            } catch (NullPointerException e) {
                getLogger().log(Level.SEVERE, "Could not establish connection to mysql database. Presumably, there are credentials missing!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }


        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(new TypeToken<ChatColor>(){}.getType(), new ChatColorGsonDeserializer())
                .create();

        File dataFolder = getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        Path dataFolderPath = dataFolder.toPath();

        if (useSql) {
            dataManager = new DataManager(gson, sqlHandler); //Using a data manager that uses an sql database.
        }
        else {
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

            dataManager = new DataManager(gson, groupFolder, playerFolder); //Using a data manger that reads data from disk
        }

        if (!dataManager.groupExists("default")) {
            GroupData defaultData = new GroupData(ChatColor.GRAY, "Member ");
            dataManager.saveGroupDataAsync("default", defaultData, new IOCallback<>() {
                @Override
                public void success(Void data) {
                    getLogger().info("Successfully created default group!");
                }

                @Override
                public void error(Exception error) {
                    getLogger().info("There was an error creating the default group");
                }
            });
        }

        try {
            FileConfiguration permsFile = loadConfig("perms.yml");
            PlayerPermissionManager playerPermissionManager = new PlayerPermissionManager(this);
            GroupPermissionManager groupPermissionManager = new GroupPermissionManager();
            PermissionApplier permissionApplier = new PermissionApplier(
                dataManager,
                playerPermissionManager,
                groupPermissionManager
            );

            Set<String> keys = permsFile.getKeys(false);

            for (String key : keys) {
                ConfigurationSection section = permsFile.getConfigurationSection(key);
                Map<String, Object> sectionKeys = section.getValues(true);

                sectionKeys.forEach((k, v) -> {
                    if (v instanceof Boolean value) {
                        groupPermissionManager.setPermission(key, k, value);
                    }
                });
            }

            getServer().getPluginManager()
                .registerEvents(new PlayerJoinEventListener(permissionApplier), this);
            getServer().getPluginManager()
                .registerEvents(new PlayerQuitEventListener(playerPermissionManager), this);

            getCommand("simplyrank").setExecutor(new SimplyRankCommandExecutor(dataManager, permissionApplier));
        } catch (IOException e) {
            getLogger().severe("Could not load perms.yml");
            e.printStackTrace();
        }

        new SimplyRankPlaceholderExpansion().register();
        new ScoreboardTeamsPlaceholderExtension(config.getString("team_name_decorated", "").replace("%simplyscoreboard_team_name_decorated%", "")).register();
    }

    @Override
    public void onDisable() {
        instance = null;
        if (sqlHandler != null)
            sqlHandler.close();
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public static SimplyRankPlugin getInstance() {
        return instance;
    }

    private FileConfiguration loadConfig(String name) throws IOException {
        File dataFolder = getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File configFile = new File(dataFolder, name);

        if (!configFile.exists()) {
            Files.copy(getClassLoader().getResourceAsStream(name), configFile.toPath());
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    private SQLHandler createSQLHandlerFromConfig(FileConfiguration config) {

        try {
            String url = config.getString("mysql.url");
            String password = config.getString("mysql.password");
            String user = config.getString("mysql.username");

            return new SQLHandler(url, user, password);

        } catch (NullPointerException e) {
            throw e; //If there are credentials missing.
        }
    }

}
