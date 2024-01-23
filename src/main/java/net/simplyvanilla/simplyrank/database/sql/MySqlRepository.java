package net.simplyvanilla.simplyrank.database.sql;

import com.google.gson.Gson;
import net.simplyvanilla.simplyrank.database.group.GroupData;
import net.simplyvanilla.simplyrank.database.sql.exceptions.MySqlStatementFailedException;
import net.simplyvanilla.simplyrank.database.player.PlayerData;
import net.simplyvanilla.simplyrank.database.group.GroupRepository;
import net.simplyvanilla.simplyrank.database.player.PlayerDataRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class MySqlRepository implements PlayerDataRepository, GroupRepository {
    /*

        All methods are synchronous, unless specified

    */
    private final MySqlClient sql;
    private final Gson gson;

    public MySqlRepository(MySqlClient handler, Gson gson) {
        this.sql = handler;
        this.gson = gson;
    }

    public PlayerData findById(UUID uuid) {
        String query =
            String.format(
                "SELECT `data` FROM `%s` WHERE `id` = UUID_TO_BIN(?)", MySqlClient.TABLE_PLAYERS_NAME);

        try (var result = this.sql.query(this.sql.prepareStatement(query, uuid.toString()))) {

            if (!result.next()) {
                return PlayerData.getDefault();
            }

            String jsonString = result.getString("data");
            if (jsonString == null) return PlayerData.getDefault();

            return this.gson.fromJson(jsonString, PlayerData.class);

        } catch (SQLException e) {
            throw new MySqlStatementFailedException(e);
        }
    }

    public Optional<GroupData> findByName(String groupName) {

        String query =
            String.format("SELECT `data` FROM `%s` WHERE `name` = ?", MySqlClient.TABLE_GROUPS_NAME);

        try (var result = this.sql.query(this.sql.prepareStatement(query, groupName))) {
            if (!result.next()) {
                return Optional.empty();
            }

            String jsonString = result.getString("data");
            if (jsonString == null) return Optional.empty();

            return Optional.of(this.gson.fromJson(jsonString, GroupData.class));
        } catch (SQLException e) {
            throw new MySqlStatementFailedException(e);
        }
    }

    public void save(UUID playerId, PlayerData playerData) {
        String query =
            String.format(
                """
                    INSERT INTO `%s` (`id`, `data`) VALUES (UUID_TO_BIN(?), ?)
                    ON DUPLICATE KEY UPDATE `data` = VALUES(`data`), `updated_at` = CURRENT_TIMESTAMP
                    """,
                MySqlClient.TABLE_PLAYERS_NAME);

        try {
            var statement = this.sql.prepareStatement(query, playerId.toString(), this.gson.toJson(playerData));

            this.sql.update(statement);

        } catch (SQLException e) {
            throw new MySqlStatementFailedException(e);
        }
    }

    public void save(
        String groupName, GroupData groupData) {

        String query =
            String.format(
                """
                    INSERT INTO `%s` (`name`, `data`) VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE `data` = VALUES(`data`), `updated_at` = CURRENT_TIMESTAMP
                    """,
                MySqlClient.TABLE_GROUPS_NAME);

        try {
            var statement = this.sql.prepareStatement(query, groupName, this.gson.toJson(groupData));

            this.sql.update(statement);
        } catch (SQLException e) {
            throw new MySqlStatementFailedException(e);
        }
    }

    public boolean existsByName(String groupName) {

        String qry = String.format("SELECT * FROM `%s` WHERE `name` = ?", MySqlClient.TABLE_GROUPS_NAME);

        try (ResultSet result = this.sql.query(this.sql.prepareStatement(qry, groupName))) {
            return result.next();
        } catch (SQLException e) {
            throw new MySqlStatementFailedException(e);
        }
    }
}
