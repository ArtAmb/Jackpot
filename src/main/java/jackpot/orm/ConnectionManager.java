package jackpot.orm;

import com.google.gson.Gson;
import jackpot.orm.properties.JackpotOrmProperties;
import lombok.val;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager implements Closeable {
    private final Connection connection;
    private final QueryRunner queryRunner = new QueryRunner();
    private final Gson gson = new Gson();


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

    public Object executeQuery(String sql, Class<?> responseClass) {
        try {
            val resultMap = queryRunner.query(this.connection, sql, new MapListHandler());
            String resultJson = gson.toJson(resultMap);
            Class<?> arrayClass = Class.forName("[L" + responseClass.getName() + ";");

            return gson.fromJson(resultJson, arrayClass);

        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } catch (ClassNotFoundException e) {
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
