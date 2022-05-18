package net.simplyvanilla.simplyrank;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.simplyvanilla.simplyrank.command.SimplyRankCommandExecutor;
import net.simplyvanilla.simplyrank.data.DataManager;
import net.simplyvanilla.simplyrank.data.GroupData;
import net.simplyvanilla.simplyrank.data.IOCallback;
import net.simplyvanilla.simplyrank.data.SQLHandler;
import net.simplyvanilla.simplyrank.gson.ChatColorGsonDeserializer;
import net.simplyvanilla.simplyrank.placeholder.SimplyRankPlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
            config = loadConfig();
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

        if (useSql) {
            dataManager = new DataManager(gson, sqlHandler); //Using a data manager that uses an sql database.
        }
        else {
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

        getCommand("simplyrank").setExecutor(new SimplyRankCommandExecutor(dataManager));

        new SimplyRankPlaceholderExpansion().register();
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

    private FileConfiguration loadConfig() throws IOException {
        File dataFolder = getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File configFile = new File(dataFolder, "config.yml");

        if (!configFile.exists()) {
            Files.copy(getClassLoader().getResourceAsStream("config.yml"), configFile.toPath());
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    private SQLHandler createSQLHandlerFromConfig(FileConfiguration config) {

        try {
            String port = config.getString("mysql.port");
            String database = config.getString("mysql.database");
            String password = config.getString("mysql.password");
            String user = config.getString("mysql.username");
            String host = config.getString("mysql.host");

            return new SQLHandler(host, database, user, password, port);

        } catch (NullPointerException e) {
            throw e; //If there are credentials missing.
        }
    }



}
