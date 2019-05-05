package jackpot.orm;

import jackpot.orm.metadata.ColumnMetadata;
import jackpot.orm.metadata.RelationMetadata;
import jackpot.orm.metadata.TableMetadata;
import jackpot.utils.JackpotUtils;
import jackpot.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JackpotRelationSqlGenerator {

    private final Map<String, TableMetadata> allTablesMapByName;

    public JackpotRelationSqlGenerator(List<TableMetadata> allTables) {
        allTablesMapByName = allTables.stream()
                .collect(Collectors.toMap(table -> table.getTableName(), table -> table));
    }

    public String createSql(RelationMetadata relationMetadata) {

        switch (relationMetadata.getType()) {

            case MANY_TO_MANY:
                break;
            case ONE_TO_MANY:
                return generateOneToManyRelationSQL(relationMetadata);
            case MANY_TO_ONE:
                break;
        }


        throw new IllegalStateException("Unrecognized relation type " + relationMetadata.getType());
    }

    private String generateOneToManyRelationSQL(RelationMetadata relationMetadata) {
        StringBuffer sqlBuf = new StringBuffer();

        TableMetadata sourceTable = allTablesMapByName.get(relationMetadata.getSourceTableName());
        Utils.assertNotNull(sourceTable, relationMetadata.getSourceTableName() + " not found");

        ColumnMetadata sourceColumn = getSourceColumn(sourceTable, relationMetadata);

        sqlBuf.append(String.format("ALTER TABLE %s ADD %s %s%s;",
                relationMetadata.getTargetTableName(),
                relationMetadata.getTargetColumnName(),
                JackpotUtils.toSqlType(sourceColumn.getColumnType()),
                relationMetadata.isTargetColumnNotNull() ? " NOT NULL" : ""));

        sqlBuf.append(String.format("ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s(%s);",
                relationMetadata.getTargetTableName(),
                createFkConstraintName(relationMetadata),
                relationMetadata.getTargetColumnName(),
                sourceTable.getTableName(),
                sourceColumn.getColumnName()));

        return sqlBuf.toString();
    }

    private ColumnMetadata getSourceColumn(TableMetadata sourceTable, RelationMetadata relationMetadata) {
        if (relationMetadata.getSourceColumnName() == null)
            return sourceTable.getPrimaryKeyColumn();

        return sourceTable.getColumn(relationMetadata.getSourceColumnName());
    }

    private String createFkConstraintName(RelationMetadata relationMetadata) {
        return String.format("fk_%s_%s_%s", relationMetadata.getTargetTableName(), relationMetadata.getTargetColumnName(), relationMetadata.getSourceTableName());
    }
}
