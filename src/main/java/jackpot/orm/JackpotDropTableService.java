package jackpot.orm;

import jackpot.orm.metadata.TableMetadata;

import java.util.List;

public class JackpotDropTableService {

    public void dropTables(List<TableMetadata> tables) {

        ConnectionManager connectionManager = ConnectionManager.createNew();

        tables.forEach(table -> {
            String sql = String.format("DROP TABLE IF EXISTS %s;", table.getTableName());
            connectionManager.executeSql(sql);
        });

        connectionManager.close();
    }
}
