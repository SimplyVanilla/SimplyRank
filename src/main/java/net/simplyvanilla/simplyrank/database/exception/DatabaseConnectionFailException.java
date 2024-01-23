package net.simplyvanilla.simplyrank.database.exception;

public class DatabaseConnectionFailException extends RuntimeException {
    public DatabaseConnectionFailException() {
        super("Could not connect to database! Please check your config.yml and try again.");
    }
}
