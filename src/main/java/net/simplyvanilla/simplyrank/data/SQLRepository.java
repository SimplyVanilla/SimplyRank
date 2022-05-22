package net.simplyvanilla.simplyrank.data;

import com.google.gson.Gson;
import net.simplyvanilla.simplyrank.SimplyRankPlugin;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLRepository implements DataRepository {

    private final SQLHandler sql;
    private final Gson gson;

    public SQLRepository(SQLHandler handler, Gson gson) {
        this.sql = handler;
        this.gson = gson;
    }

    @Override
    public PlayerData loadPlayerData(UUID uuid, IOCallback<PlayerData, Exception> callback) {

        var strUUID = uuid.toString();

        String qry = "SELECT `data` FROM `?` WHERE `uuid` = ?";

        try (var result = sql.query(
                sql.prepareStatement(qry, SQLHandler.TABLE_PLAYERS_NAME, strUUID))) {

            if (!result.next()) {
                return PlayerData.getDefault();
            }

            String jsonString = result.getString("data");
            if (jsonString == null) return PlayerData.getDefault();;

            var playerData = gson.fromJson(jsonString, PlayerData.class);
            return playerData;

        } catch (SQLException e) {
            if (callback != null)
                callback.error(e);
            else
                e.printStackTrace();
        }

        return null;
    }

    @Override
    public GroupData loadGroupData(String groupName, IOCallback<GroupData, Exception> callback) {

        String qry = "SELECT `data` FROM `?` WHERE `name` = ?";

        try (var result = sql.query(
                sql.prepareStatement(qry, SQLHandler.TABLE_GROUPS_NAME, groupName))) {

            if (!result.next()) {
                return null;
            }

            String jsonString = result.getString("data");
            if (jsonString == null) return null;

            var groupData = gson.fromJson(jsonString, GroupData.class);
            return groupData;
        } catch (SQLException e) {
            if (callback != null)
                callback.error(e);
            else
                e.printStackTrace();
        }


        return null;
    }

    @Override
    public void savePlayerData(String uuidString, PlayerData playerData, IOCallback<Void, Exception> callback) {

        String qry =
            """
            INSERT INTO ? (`uuid`, `data`) VALUES (?, ?) as `new`
            ON DUPLICATE KEY UPDATE `data` = `new`.`data`, `updated_at` = CURRENT_TIMESTAMP
            """;

        try {
            var statement = sql.prepareStatement(
                qry,
                SQLHandler.TABLE_PLAYERS_NAME,
                uuidString,
                gson.toJson(playerData)
            );


            sql.update(statement);

            //Sync success
            if (callback != null)
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.success(null));

        } catch (SQLException e) {
            if (callback != null)
                callback.error(e);
            else
                e.printStackTrace();
        }

    }

    @Override
    public void saveGroupData(String groupName, GroupData groupData, IOCallback<Void, Exception> callback) {

        String qry =
            """
            INSERT INTO ? (`name`, `data`) VALUES (?, ?) AS `new`
            ON DUPLICATE KEY UPDATE `data` = `new`.`data`, `updated_at` = CURRENT_TIMESTAMP
            """;

        try {
            var statement = sql.prepareStatement(
                qry,
                SQLHandler.TABLE_GROUPS_NAME,
                groupName,
                gson.toJson(groupData)
            );

            sql.update(statement);

            //Sync success
            if (callback != null)
                Bukkit.getScheduler().runTask(SimplyRankPlugin.getInstance(), () -> callback.success(null));

        } catch (SQLException e) {
            if (callback != null)
                callback.error(e);
            else
                e.printStackTrace();
        }
    }
    @Override
    public boolean groupExists(String name) {

        String qry = "SELECT * FROM `?` WHERE name=?";


        try (ResultSet result = sql.query(
                sql.prepareStatement(qry, SQLHandler.TABLE_GROUPS_NAME, name))) {

            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

}
