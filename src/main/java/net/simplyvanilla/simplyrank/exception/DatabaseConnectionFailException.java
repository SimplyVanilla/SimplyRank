package net.simplyvanilla.simplyrank.exception;

public class DatabaseConnectionFailException extends RuntimeException {
    public DatabaseConnectionFailException() {
        super("Could not connect to database! Please check your config.yml and try again.");
    }
}
