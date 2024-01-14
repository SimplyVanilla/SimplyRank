package net.simplyvanilla.simplyrank;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.simplyvanilla.simplyrank.command.SimplyRankCommandExecutor;
import net.simplyvanilla.simplyrank.data.GroupPermissionService;
import net.simplyvanilla.simplyrank.data.PlayerDataService;
import net.simplyvanilla.simplyrank.data.PlayerPermissionService;
import net.simplyvanilla.simplyrank.data.callback.IOCallback;
import net.simplyvanilla.simplyrank.data.database.group.GroupData;
import net.simplyvanilla.simplyrank.data.database.sql.MySqlClient;
import net.simplyvanilla.simplyrank.data.database.sql.MySqlRepository;
import net.simplyvanilla.simplyrank.exception.DatabaseConnectionFailException;
import net.simplyvanilla.simplyrank.gson.TextColorGsonDeserializer;
import net.simplyvanilla.simplyrank.listener.PlayerLoginEventListener;
import net.simplyvanilla.simplyrank.listener.PlayerQuitEventListener;
import net.simplyvanilla.simplyrank.placeholder.MiniPlaceholderRegister;
import net.simplyvanilla.simplyrank.placeholder.ScoreboardTeamsPlaceholderExtension;
import net.simplyvanilla.simplyrank.placeholder.SimplyRankPlaceholderExpansion;
import net.simplyvanilla.simplyrank.data.PermissionApplyService;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class SimplyRankPlugin extends JavaPlugin {

    private static SimplyRankPlugin instance;
    private PlayerDataService playerDataService;
    private MySqlClient mySqlClient = null;


    @Override
    public void onEnable() {
        MySqlRepository mySqlRepository = null;
        FileConfiguration config;
        instance = this;

        try {
            config = loadConfig("config.yml");
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().log(Level.SEVERE, "Could not load config file! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            mySqlClient = createSQLHandlerFromConfig(config);
        } catch (NullPointerException e) {
            getLogger()
                .log(
                    Level.SEVERE,
                    "Could not establish connection to database. Presumably, there are credentials missing!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Gson gson =
            new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(
                    new TypeToken<TextColor>() {
                    }.getType(), new TextColorGsonDeserializer())
                .create();

        mySqlRepository = new MySqlRepository(mySqlClient, gson);

        File dataFolder = getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        playerDataService =
            new PlayerDataService(this, mySqlRepository, mySqlRepository);

        if (!playerDataService.groupExists("default")) {
            GroupData defaultData = new GroupData(NamedTextColor.GRAY, "Member ");
            playerDataService.saveGroupDataAsync(
                "default",
                defaultData,
                new IOCallback<>() {
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
            PlayerPermissionService playerPermissionService =
                new PlayerPermissionService(this, this.playerDataService);
            GroupPermissionService groupPermissionService = new GroupPermissionService();
            PermissionApplyService permissionApplyService =
                new PermissionApplyService(playerDataService, playerPermissionService, groupPermissionService);

            Set<String> keys = permsFile.getKeys(false);

            for (String key : keys) {
                ConfigurationSection section = permsFile.getConfigurationSection(key);
                Map<String, Object> sectionKeys = section.getValues(true);

                sectionKeys.forEach(
                    (k, v) -> {
                        if (v instanceof Boolean value) {
                            groupPermissionService.setPermission(key, k, value);
                        }
                    });
            }

            getServer()
                .getPluginManager()
                .registerEvents(new PlayerQuitEventListener(playerPermissionService), this);
            getServer()
                .getPluginManager()
                .registerEvents(new PlayerLoginEventListener(permissionApplyService), this);

            getCommand("simplyrank")
                .setExecutor(new SimplyRankCommandExecutor(playerDataService, permissionApplyService));
        } catch (IOException e) {
            getLogger().severe("Could not load perms.yml");
            e.printStackTrace();
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SimplyRankPlaceholderExpansion().register();
            new ScoreboardTeamsPlaceholderExtension().register();
            new MiniPlaceholderRegister(this).register();
        }
    }

    @Override
    public void onDisable() {
        instance = null;
        if (mySqlClient != null) {
            mySqlClient.close();
        }
    }

    public PlayerDataService getDataManager() {
        return playerDataService;
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

    private MySqlClient createSQLHandlerFromConfig(FileConfiguration config) {

        try {
            String url = config.getString("database.url");
            String password = config.getString("database.password");
            String user = config.getString("database.username");

            return new MySqlClient(url, user, password);

        } catch (NullPointerException e) {
            throw new DatabaseConnectionFailException();
        }
    }
}
