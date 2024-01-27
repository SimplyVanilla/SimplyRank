package net.simplyvanilla.simplyrank.database.sql;

import net.simplyvanilla.simplyrank.SimplyRankPlugin;

import java.sql.*;

public class MySqlClient {

    public static final String TABLE_PLAYERS_NAME = "player";
    public static final String TABLE_GROUPS_NAME = "group";

    private final String url;
    private final String username;
    private final String password;

    private Connection connection;

    public MySqlClient(String url, String user, String password) {
        this.url = url;
        this.username = user;
        this.password = password;

        connect();
        initTables();
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            SimplyRankPlugin.getInstance().getSLF4JLogger().error("Could not connect to MySQL database", e);
        }
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            SimplyRankPlugin.getInstance().getSLF4JLogger().error("Could not close MySQL connection", e);
        }
    }

    public void update(PreparedStatement statement) throws SQLException {
        statement.executeUpdate();
        statement.close();
    }

    public ResultSet query(PreparedStatement statement) throws SQLException {
        ResultSet rs;
        rs = statement.executeQuery();

        statement.closeOnCompletion();

        return rs;
    }

    private void executeRawStatement(String cmd) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute(cmd);
        }
    }

    private void initTables() {

        String cmdPlayers =
            String.format(
                """
                      CREATE TABLE if not exists `%s` (
                         `id` BINARY(16) NOT NULL,
                         `data` text NOT NULL,
                         `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`)
                       )
                    """,
                TABLE_PLAYERS_NAME);

        String cmdGroups =
            String.format(
                """
                       CREATE TABLE if not exists `%s` (
                         `id` int unsigned NOT NULL AUTO_INCREMENT,
                         `name` varchar(255) NOT NULL,
                         `data` TEXT NOT NULL,
                         `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `name` (`name`)
                       )
                    """,
                TABLE_GROUPS_NAME);

        // table has id, address, type, proxy, fetched_at
        String proxyCacheTable = """
                CREATE TABLE IF NOT EXISTS `proxy_cache` (
                    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
                    `address` VARBINARY(16) NOT NULL,
                    `type` VARCHAR(255) NOT NULL,
                    `proxy` TINYINT(1) NOT NULL,
                    `fetched_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (`id`),
                    UNIQUE KEY `address` (`address`),
                    INDEX `fetched_at` (`fetched_at`)
                )
            """;

        // table has id, address (unique), invoker_id, created_at
        String addressWhitelistTable = """
                CREATE TABLE IF NOT EXISTS `address_whitelist` (
                    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
                    `address` VARBINARY(16) NOT NULL,
                    `invoker_id` BINARY(16) NOT NULL,
                    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (`id`),
                    UNIQUE KEY `address` (`address`)
                )
            """;

        try {
            executeRawStatement(cmdPlayers);
            executeRawStatement(cmdGroups);
            executeRawStatement(proxyCacheTable);
            executeRawStatement(addressWhitelistTable);
        } catch (SQLException e) {
            SimplyRankPlugin.getInstance().getSLF4JLogger().error("Could not create tables", e);
        }
    }

    public PreparedStatement prepareStatement(String qry) throws SQLException {
        return connection.prepareStatement(qry);
    }

    public PreparedStatement prepareStatement(String qry, String... parameters) throws SQLException {
        var statement = prepareStatement(qry);

        for (int i = 0; i < parameters.length; i++) {
            String s = parameters[i];
            statement.setString(i + 1, s);
        }

        return statement;
    }
}
