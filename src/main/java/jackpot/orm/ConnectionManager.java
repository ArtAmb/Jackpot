package jackpot.orm;

import jackpot.orm.properties.JackpotOrmProperties;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionManager implements Closeable {
    private final Connection connection;


    public static ConnectionManager createNew() {
        return new ConnectionManager(true);
    }

    public static ConnectionManager createNew(boolean autoCommit) {
        return new ConnectionManager(autoCommit);
    }

    private ConnectionManager(boolean autoCommit) {
        try {
            this.connection = DriverManager
                    .getConnection(JackpotOrmProperties.getUrl(), JackpotOrmProperties.getLogin(), JackpotOrmProperties.getPassword());

            this.connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void executeSql(String sql) {
        try {
            this.connection.createStatement().execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public ResultSet executeQuery(String sql) {
        try {
            return this.connection.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void commit() throws SQLException {
        this.connection.commit();
    }

    public void rollback() throws SQLException {
        this.connection.rollback();
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException();
        }
    }


}
