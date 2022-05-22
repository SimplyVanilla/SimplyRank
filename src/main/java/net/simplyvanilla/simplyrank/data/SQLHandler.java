package net.simplyvanilla.simplyrank.data;

import java.sql.*;

public class SQLHandler {

    public static final String TABLE_PLAYERS_NAME = "players";
    public static final String TABLE_GROUPS_NAME = "groups";

    private final String URL;
    private final String USERNAME;
    private final String PASSWORD;

    private Connection connection;

    public SQLHandler(String url, String user, String password) {
        this.URL = url;
        this.USERNAME = user;
        this.PASSWORD = password;

        connect();
        initTables();
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection(
                URL,
                USERNAME,
                PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
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
        Statement st = connection.createStatement();
        st.execute(cmd);
        st.close();
    }

    private void initTables() {

       String cmdPlayers = String.format(
           """
             CREATE TABLE if not exists `%s` (
                `id` int unsigned NOT NULL AUTO_INCREMENT,
                `uuid` char(36) NOT NULL,
                `data` text NOT NULL,
                `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (`id`),
                UNIQUE KEY `uuid` (`uuid`)
              )
           """,
           TABLE_PLAYERS_NAME
       );

        String cmdGroups = String.format(
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
            TABLE_GROUPS_NAME
        );

       try {
           executeRawStatement(cmdPlayers);
           executeRawStatement(cmdGroups);
       } catch (SQLException e) {
           e.printStackTrace();
       }
    }

    public PreparedStatement prepareStatement(String qry) throws SQLException {
        return connection.prepareStatement(qry);
    }

    public PreparedStatement prepareStatement(String qry, String... parameters) throws SQLException {
        var statement = prepareStatement(qry);

        for (int i = 1; i<=parameters.length; i++) {
            String s = parameters[i];
            statement.setString(i, s);
        }

        return statement;
    }

}
