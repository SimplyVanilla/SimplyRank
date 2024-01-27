package net.simplyvanilla.simplyrank.database.sql.exceptions;

public class MySqlStatementFailedException extends RuntimeException{
    public MySqlStatementFailedException(Throwable cause) {
        super(cause);
    }
}
