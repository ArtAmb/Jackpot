package jackpot.orm;

import jackpot.orm.metadata.ColumnMetadata;
import jackpot.orm.metadata.ColumnType;
import jackpot.orm.metadata.TableMetadata;
import jackpot.utils.JackpotUtils;

import java.util.StringJoiner;

public class JackpotTableSqlGenerator {

    String createSql(TableMetadata table) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(String.format("CREATE TABLE IF NOT EXISTS %s (\n", table.getTableName()));

        StringJoiner stringJoiner = new StringJoiner(",\n");
        table.getColumns().forEach(col -> {
            stringJoiner.add(String.format("%s %s %s", col.getColumnName(), toSqlType(col.getColumnType()), generateColumnConstraints(col)));
        });

        stringBuffer.append(stringJoiner);
        stringBuffer.append(");");

        return stringBuffer.toString();
    }

    private String toSqlType(ColumnType columnType) {
        return JackpotUtils.toSqlType(columnType);
    }

    private String generateColumnConstraints(ColumnMetadata col) {
        StringBuffer stringBuffer = new StringBuffer("");

        if (col.isPrimaryKey())
            return "PRIMARY KEY";

        if (col.isNotNull())
            stringBuffer.append(" NOT NULL ");

        return stringBuffer.toString();
    }
}
