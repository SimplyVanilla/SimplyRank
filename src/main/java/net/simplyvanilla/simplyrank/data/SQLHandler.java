package net.simplyvanilla.simplyrank.data;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.logging.Level;

public class SQLHandler {

    public static final String TABLE_PLAYERS_NAME = "Players";
    public static final String TABLE_GROUPS_NAME = "GroupData";

    private final String HOST;
    private final String DATABASE;
    private final String USERNAME;
    private final String PASSWORD;
    private final String PORT;

    private Connection connection;

    public SQLHandler(String host, String database, String user, String password, String port) {
        this.HOST = host;
        this.DATABASE = database;
        this.USERNAME = user;
        this.PASSWORD = password;
        this.PORT = port;

        connect();
        initTables();
    }

    public void connect() {
        try {
            Bukkit.getLogger().log(Level.INFO, String.format(
                """
                Trying to establish mysql connection with:
                HOST: '%s:%s',
                DATABASE: '%s',
                USER: '%s',
                """,
                HOST, PORT, DATABASE, USERNAME
                ));

            connection = DriverManager.getConnection(
                "jdbc:mysql://" + HOST + ":" + PORT + " /" + DATABASE + "?autoReconnect=true",
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

    public void update(String qry) throws SQLException {
        Statement st = connection.createStatement();
        st.executeUpdate(qry);
        st.close();
    }

    public void execute(String cmd) throws SQLException {
        Statement st = connection.createStatement();
        st.execute(cmd);
        st.close();
    }

    public ResultSet query(String qry) throws SQLException {
        ResultSet rs;

        Statement st = connection.createStatement();
        rs = st.executeQuery(qry);

        st.closeOnCompletion();

        return rs;
    }

    private void initTables() {

       String cmdPlayers = String.format(
           """
              CREATE TABLE if not exists %s
              (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY ,
                uuid TEXT NOT NULL,
                data TEXT NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)
           """,
           TABLE_PLAYERS_NAME
       );

        String cmdGroups = String.format(
            """
              CREATE TABLE if not exists %s
              (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY ,
                name TEXT NOT NULL,
                data TEXT NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)
           """,
            TABLE_GROUPS_NAME
        );

       try {
           execute(cmdPlayers);
           execute(cmdGroups);
       } catch (SQLException e) {
           e.printStackTrace();
       }
    }

}
