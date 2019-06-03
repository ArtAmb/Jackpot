package jackpot.orm;

import jackpot.orm.metadata.TableMetadata;
import jackpot.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JackpotDatabase {
    private static JackpotDatabase instance;

    private final Map<String, TableMetadata> allTablesMapByClassName;
    private final Map<String, TableMetadata> allTablesMapByTableName;


    synchronized public static void init(List<TableMetadata> allTables) {
        if (instance == null)
            instance = new JackpotDatabase(allTables);
    }

    public static JackpotDatabase getInstance() {
        if (instance == null)
            throw new IllegalStateException("JackpotDatabase has been not initiated");

        return instance;
    }

    private JackpotDatabase(List<TableMetadata> allTables) {
        allTablesMapByClassName = allTables.stream()
                .collect(Collectors.toMap(table -> table.getClassName(), table -> table));
        allTablesMapByTableName =  allTables.stream()
                .collect(Collectors.toMap(table -> table.getTableName(), table -> table));
    }

    public TableMetadata getTableByClassName(String className) {
        TableMetadata table = allTablesMapByClassName.get(className);
        Utils.assertNotNull(table, String.format("Table for class %s does not exist", className));

        return table;
    }

    public TableMetadata getTableByTableName(String tableName) {
        TableMetadata table = allTablesMapByTableName.get(tableName);
        Utils.assertNotNull(table, String.format("Table for table name %s does not exist", tableName));

        return table;
    }

}
