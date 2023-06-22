package net.simplyvanilla.simplyrank.data;

import com.google.gson.Gson;
import net.simplyvanilla.simplyrank.SimplyRankPlugin;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLRepository {
    /*

        All methods are synchronous, unless specified

    */
    private final SQLHandler sql;
    private final Gson gson;

    public SQLRepository(SQLHandler handler, Gson gson) {
        this.sql = handler;
        this.gson = gson;
    }

    public PlayerData loadPlayerData(UUID uuid, IOCallback<PlayerData, Exception> callback) {

        var strUUID = uuid.toString();

        String qry =
            String.format(
                "SELECT `data` FROM `%s` WHERE `id` = UUID_TO_BIN(?)", SQLHandler.TABLE_PLAYERS_NAME);

        try (var result = sql.query(sql.prepareStatement(qry, strUUID))) {

            if (!result.next()) {
                return PlayerData.getDefault();
            }

            String jsonString = result.getString("data");
            if (jsonString == null) return PlayerData.getDefault();

            return gson.fromJson(jsonString, PlayerData.class);

        } catch (SQLException e) {
            if (callback != null) callback.error(e);
            else SimplyRankPlugin.getInstance().getLogger().severe(e.getMessage());
        }

        return null;
    }

    public GroupData loadGroupData(String groupName, IOCallback<GroupData, Exception> callback) {

        String qry =
            String.format("SELECT `data` FROM `%s` WHERE `name` = ?", SQLHandler.TABLE_GROUPS_NAME);

        try (var result = sql.query(sql.prepareStatement(qry, groupName))) {
            if (!result.next()) {
                return null;
            }

            String jsonString = result.getString("data");
            if (jsonString == null) return null;

            return gson.fromJson(jsonString, GroupData.class);
        } catch (SQLException e) {
            if (callback != null) callback.error(e);
            else SimplyRankPlugin.getInstance().getLogger().severe(e.getMessage());
        }

        return null;
    }

    public void savePlayerData(
        String uuidString, PlayerData playerData, IOCallback<Void, Exception> callback) {

        String query =
            String.format(
                """
                    INSERT INTO `%s` (`id`, `data`) VALUES (UUID_TO_BIN(?), ?)
                    ON DUPLICATE KEY UPDATE `data` = VALUES(`data`), `updated_at` = CURRENT_TIMESTAMP
                    """,
                SQLHandler.TABLE_PLAYERS_NAME);

        updateData(query, callback, uuidString, gson.toJson(playerData));
    }

    public void saveGroupData(
        String groupName, GroupData groupData, IOCallback<Void, Exception> callback) {

        String query =
            String.format(
                """
                    INSERT INTO `%s` (`name`, `data`) VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE `data` = VALUES(`data`), `updated_at` = CURRENT_TIMESTAMP
                    """,
                SQLHandler.TABLE_GROUPS_NAME);

        updateData(query, callback, groupName, gson.toJson(groupData));
    }

    public boolean groupExists(String name) {

        String qry = String.format("SELECT * FROM `%s` WHERE `name` = ?", SQLHandler.TABLE_GROUPS_NAME);

        try (ResultSet result = sql.query(sql.prepareStatement(qry, name))) {
            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void updateData(
        String query, IOCallback<Void, Exception> callback, String key, String data) {
        try {
            var statement = sql.prepareStatement(query, key, data);

            sql.update(statement);

            // Sync success
            if (callback != null)
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.success(null));

        } catch (SQLException e) {
            if (callback != null) callback.error(e);
            else SimplyRankPlugin.getInstance().getLogger().severe(e.getMessage());
        }
    }
}
