package net.simplyvanilla.simplyrank;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.simplyvanilla.simplyrank.command.SimplyRankCommandExecutor;
import net.simplyvanilla.simplyrank.permission.GroupPermissionService;
import net.simplyvanilla.simplyrank.permission.PermissionApplyService;
import net.simplyvanilla.simplyrank.permission.PlayerDataService;
import net.simplyvanilla.simplyrank.permission.PlayerPermissionService;
import net.simplyvanilla.simplyrank.database.group.GroupData;
import net.simplyvanilla.simplyrank.database.sql.MySqlClient;
import net.simplyvanilla.simplyrank.database.sql.MySqlRepository;
import net.simplyvanilla.simplyrank.database.exception.DatabaseConnectionFailException;
import net.simplyvanilla.simplyrank.gson.TextColorGsonDeserializer;
import net.simplyvanilla.simplyrank.listener.PlayerLoginEventListener;
import net.simplyvanilla.simplyrank.listener.PlayerQuitEventListener;
import net.simplyvanilla.simplyrank.placeholder.MiniPlaceholderRegister;
import net.simplyvanilla.simplyrank.placeholder.ScoreboardTeamsPlaceholderExtension;
import net.simplyvanilla.simplyrank.placeholder.SimplyRankPlaceholderExpansion;
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
            config = this.loadConfig("config.yml");
        } catch (IOException e) {
            e.printStackTrace();
            this.getLogger().log(Level.SEVERE, "Could not load config file! Disabling plugin...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            this.mySqlClient = this.createSQLHandlerFromConfig(config);
        } catch (NullPointerException e) {
            this.getLogger()
                .log(
                    Level.SEVERE,
                    "Could not establish connection to database. Presumably, there are credentials missing!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Gson gson =
            new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(
                    new TypeToken<TextColor>() {
                    }.getType(), new TextColorGsonDeserializer())
                .create();

        mySqlRepository = new MySqlRepository(this.mySqlClient, gson);

        File dataFolder = this.getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.playerDataService =
            new PlayerDataService(this, mySqlRepository, mySqlRepository);

        if (!this.playerDataService.groupExists("default")) {
            GroupData defaultData = new GroupData(NamedTextColor.GRAY, "Member ");
            try {
                this.playerDataService.saveGroupData(
                    "default",
                    defaultData);
                this.getLogger().info("Successfully created default group!");
            } catch (Exception e) {
                this.getSLF4JLogger().info("There was an error creating the default group", e);
            }
        }

        try {
            FileConfiguration permsFile = this.loadConfig("perms.yml");
            PlayerPermissionService playerPermissionService =
                new PlayerPermissionService(this, this.playerDataService);
            GroupPermissionService groupPermissionService = new GroupPermissionService();
            PermissionApplyService permissionApplyService =
                new PermissionApplyService(this, this.playerDataService, playerPermissionService, groupPermissionService);

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

            this.getServer()
                .getPluginManager()
                .registerEvents(new PlayerQuitEventListener(playerPermissionService), this);
            this.getServer()
                .getPluginManager()
                .registerEvents(new PlayerLoginEventListener(this, permissionApplyService), this);

            this.getCommand("simplyrank")
                .setExecutor(new SimplyRankCommandExecutor(this.playerDataService, permissionApplyService));
        } catch (IOException e) {
            this.getLogger().severe("Could not load perms.yml");
            e.printStackTrace();
        }

        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SimplyRankPlaceholderExpansion().register();
            new ScoreboardTeamsPlaceholderExtension().register();
            new MiniPlaceholderRegister(this).register();
        }
    }

    @Override
    public void onDisable() {
        instance = null;
        if (this.mySqlClient != null) {
            this.mySqlClient.close();
        }
    }

    public PlayerDataService getDataManager() {
        return this.playerDataService;
    }

    public static SimplyRankPlugin getInstance() {
        return instance;
    }

    private FileConfiguration loadConfig(String name) throws IOException {
        File dataFolder = this.getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File configFile = new File(dataFolder, name);

        if (!configFile.exists()) {
            Files.copy(this.getClassLoader().getResourceAsStream(name), configFile.toPath());
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

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
