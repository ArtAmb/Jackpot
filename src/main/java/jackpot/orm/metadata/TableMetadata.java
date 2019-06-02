package jackpot.orm.metadata;


import jackpot.utils.Utils;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class TableMetadata {

    String className;
    String tableName;
    Class<?> tableClass;

    List<ColumnMetadata> columns;

    public ColumnMetadata getPrimaryKeyColumn() {
        return columns.stream().filter(ColumnMetadata::isPrimaryKey).findFirst()
                .orElseThrow(()
                        -> new IllegalStateException(String.format("Table %s does NOT have primary key", tableName)));
    }

    public ColumnMetadata getColumn(String name) {
        Utils.assertIfTrue(!Utils.isBlank(name), "Column name cannot be blank");

        return columns.stream().filter(col -> col.getColumnName().equals(name)).findFirst()
                .orElseThrow(()
                        -> new IllegalStateException(String.format("Table %s does NOT have column %s", tableName, name)));
    }

    public boolean hasColumn(String name) {
        Utils.assertIfTrue(!Utils.isBlank(name), "Column name cannot be blank");

        return columns.stream().filter(col -> col.getColumnName().equals(name)).findFirst().isPresent();
    }
}
