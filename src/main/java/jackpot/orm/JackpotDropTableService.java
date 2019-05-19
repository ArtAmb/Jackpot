package jackpot.orm;

import jackpot.orm.metadata.TableMetadata;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class JackpotDropTableService {

    public void dropTables(List<TableMetadata> tables) {

        ConnectionManager connectionManager = ConnectionManager.createNew();
        AtomicBoolean isDbDeleted = new AtomicBoolean(false);

        while (!isDbDeleted.get()) {

            AtomicBoolean isEveryTableDeleted = new AtomicBoolean(true);
            tables.forEach(table -> {
                boolean isDropped = dropTable(connectionManager, table);
                if (!isDropped)
                    isEveryTableDeleted.set(false);
            });

            if (isEveryTableDeleted.get())
                isDbDeleted.set(true);
        }
        connectionManager.close();
    }

    private boolean dropTable(ConnectionManager connectionManager, TableMetadata table) {
        String sql = String.format("DROP TABLE IF EXISTS %s;", table.getTableName());
        try {
            connectionManager.executeSql(sql);
        } catch (Exception ex) {
            System.out.println(String.format("DROP %s -> ERROR: %s", table.getTableName(), ex.getMessage()));
            return false;
        }

        return true;
    }
}
