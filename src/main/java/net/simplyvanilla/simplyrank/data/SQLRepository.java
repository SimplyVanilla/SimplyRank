package net.simplyvanilla.simplyrank.data;

import com.google.gson.Gson;
import net.simplyvanilla.simplyrank.SimplyRankPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
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

        if (!playerExists(uuid))
            createPlayer(uuid);

        try {
            String qry = String.format("SELECT data FROM %s WHERE uuid='%s'", SQLHandler.TABLE_PLAYERS_NAME, strUUID);
            var result = sql.query(qry);

            if (!result.next()) {
                return null;
            }

            String jsonString = result.getString("data");
            if (jsonString == null) return null;

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
        try {
            String qry = String.format("SELECT data FROM %s WHERE name='%s'", SQLHandler.TABLE_GROUPS_NAME, groupName);
            var result = sql.query(qry);

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
        if (!playerExists(uuidString))
            createPlayer(uuidString);


        try {
            String qry = String.format("UPDATE %s SET data='%s' WHERE uuid='%s'",
                SQLHandler.TABLE_PLAYERS_NAME, gson.toJson(playerData), uuidString);

            sql.update(qry);

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
        String qry = String.format("UPDATE %s SET data='%s' WHERE name='%s'",
            SQLHandler.TABLE_GROUPS_NAME, gson.toJson(groupData), groupName);


        try {
            if (groupExists(groupName)) {
                sql.update(qry);
            }
            else {
                createGroup(groupName, groupData);
            }

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
        try {
            String qry = String.format("SELECT * FROM %s WHERE name='%s'", SQLHandler.TABLE_GROUPS_NAME ,name);
            ResultSet result = sql.query(qry);

            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void createGroup(String name, GroupData data) throws SQLException {
        String json = gson.toJson(data);

        String qry = String.format(
            """
            INSERT INTO %s
            (name, data)
            VALUES
            ('%s','%s')
            """,
            SQLHandler.TABLE_GROUPS_NAME,
            name,
            json
        );

        sql.update(qry);
    }

    public boolean playerExists(String uuidString) {
        try {
            String qry = String.format("SELECT * FROM %s WHERE uuid='%s'", SQLHandler.TABLE_PLAYERS_NAME, uuidString);
            ResultSet result = sql.query(qry);

            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean playerExists(UUID uuid) {
        return playerExists(uuid.toString());
    }

    public void createPlayer(String uuidString) {
        var data = PlayerData.getDefault();
        String json = gson.toJson(data);

        String qry = String.format(
            """
            INSERT INTO %s
            (uuid, data)
            VALUES
            ('%s','%s')
            """,
            SQLHandler.TABLE_PLAYERS_NAME,
            uuidString,
            json
        );

        try {
            sql.update(qry);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPlayer(UUID uuid) {
        createPlayer(uuid.toString());
    }

}
