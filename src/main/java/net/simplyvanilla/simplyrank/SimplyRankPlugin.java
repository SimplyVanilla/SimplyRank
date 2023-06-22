package net.simplyvanilla.simplyrank;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.simplyvanilla.simplyrank.command.SimplyRankCommandExecutor;
import net.simplyvanilla.simplyrank.data.*;
import net.simplyvanilla.simplyrank.exception.DatabaseConnectionFailException;
import net.simplyvanilla.simplyrank.gson.TextColorGsonDeserializer;
import net.simplyvanilla.simplyrank.listener.PlayerJoinEventListener;
import net.simplyvanilla.simplyrank.listener.PlayerQuitEventListener;
import net.simplyvanilla.simplyrank.placeholder.MiniPlaceholderRegister;
import net.simplyvanilla.simplyrank.placeholder.ScoreboardTeamsPlaceholderExtension;
import net.simplyvanilla.simplyrank.placeholder.SimplyRankPlaceholderExpansion;
import net.simplyvanilla.simplyrank.utils.PermissionApplier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class SimplyRankPlugin extends JavaPlugin {

  private static SimplyRankPlugin instance;
  private DataManager dataManager;
  private SQLHandler sqlHandler = null;

  @Override
  public void onEnable() {
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
      sqlHandler = createSQLHandlerFromConfig(config);
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
                new TypeToken<TextColor>() {}.getType(), new TextColorGsonDeserializer())
            .create();

    File dataFolder = getDataFolder();

    if (!dataFolder.exists()) {
      dataFolder.mkdirs();
    }

    dataManager =
        new DataManager(gson, sqlHandler); // Using a data manager that uses an sql database.

    if (!dataManager.groupExists("default")) {
      GroupData defaultData = new GroupData(NamedTextColor.GRAY, "Member ");
      dataManager.saveGroupDataAsync(
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
      PlayerPermissionManager playerPermissionManager =
          new PlayerPermissionManager(this, this.dataManager);
      GroupPermissionManager groupPermissionManager = new GroupPermissionManager();
      PermissionApplier permissionApplier =
          new PermissionApplier(dataManager, playerPermissionManager, groupPermissionManager);

      Set<String> keys = permsFile.getKeys(false);

      for (String key : keys) {
        ConfigurationSection section = permsFile.getConfigurationSection(key);
        Map<String, Object> sectionKeys = section.getValues(true);

        sectionKeys.forEach(
            (k, v) -> {
              if (v instanceof Boolean value) {
                groupPermissionManager.setPermission(key, k, value);
              }
            });
      }

      getServer()
          .getPluginManager()
          .registerEvents(new PlayerJoinEventListener(permissionApplier), this);
      getServer()
          .getPluginManager()
          .registerEvents(new PlayerQuitEventListener(playerPermissionManager), this);

      getCommand("simplyrank")
          .setExecutor(new SimplyRankCommandExecutor(dataManager, permissionApplier));
    } catch (IOException e) {
      getLogger().severe("Could not load perms.yml");
      e.printStackTrace();
    }

    new SimplyRankPlaceholderExpansion().register();
    new ScoreboardTeamsPlaceholderExtension().register();
    new MiniPlaceholderRegister(this).register();
  }

  @Override
  public void onDisable() {
    instance = null;
    if (sqlHandler != null) {
      sqlHandler.close();
    }
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
      String url = config.getString("database.url");
      String password = config.getString("database.password");
      String user = config.getString("database.username");

      return new SQLHandler(url, user, password);

    } catch (NullPointerException e) {
      throw new DatabaseConnectionFailException();
    }
  }
}
