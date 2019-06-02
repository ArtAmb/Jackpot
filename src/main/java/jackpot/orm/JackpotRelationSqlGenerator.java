package jackpot.orm;

import jackpot.orm.metadata.*;
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
                return generateManyToOneRelationSQL(relationMetadata);
        }


        throw new IllegalStateException("Unrecognized relation type " + relationMetadata.getType());
    }

    private String generateOneToManyRelationSQL(RelationMetadata relationMetadata) {

        TableMetadata sourceTable = allTablesMapByName.get(relationMetadata.getSourceTableName());
        Utils.assertNotNull(sourceTable, relationMetadata.getSourceTableName() + " not found");

        TableMetadata targetTable = allTablesMapByName.get(relationMetadata.getTargetTableName());

        String targetFieldName = null;
        if(sourceTable.hasColumn(relationMetadata.getSourceColumnName())) {
            targetFieldName = getSourceColumn(sourceTable, relationMetadata).getFieldName();
        } else {
            targetFieldName = relationMetadata.getSourceColumnName();
        }


        RelationMetadata manyToOneRelation = RelationMetadata.builder()
                .targetFieldName(targetFieldName)
                .targetColumnName(relationMetadata.getSourceColumnName())
                .targetTableName(relationMetadata.getSourceTableName())
                .sourceColumnName(targetTable.getPrimaryKeyColumn().getColumnName())
                .sourceTableName(relationMetadata.getTargetTableName())
                .type(RelationType.MANY_TO_ONE)
                .targetColumnNotNull(false)
                .build();

        return generateManyToOneRelationSQL(manyToOneRelation);
    }

    private String generateManyToOneRelationSQL(RelationMetadata relationMetadata) {
        StringBuffer sqlBuf = new StringBuffer();

        TableMetadata sourceTable = allTablesMapByName.get(relationMetadata.getSourceTableName());
        Utils.assertNotNull(sourceTable, relationMetadata.getSourceTableName() + " not found");
        TableMetadata targetTable = allTablesMapByName.get(relationMetadata.getTargetTableName());

        ColumnMetadata sourceColumn = getSourceColumn(sourceTable, relationMetadata);

        sqlBuf.append(String.format("ALTER TABLE %s ADD %s %s%s;",
                relationMetadata.getTargetTableName(),
                relationMetadata.getTargetColumnName(),
                JackpotUtils.toSqlType(sourceColumn.getColumnType()),
                relationMetadata.isTargetColumnNotNull() ? " NOT NULL" : ""));

        targetTable.getColumns().add(ColumnMetadata.builder()
                .fieldName(relationMetadata.getTargetFieldName())
                .columnName(relationMetadata.getTargetColumnName())
                .notNull(relationMetadata.isTargetColumnNotNull())
                .columnType(sourceColumn.getColumnType())
                .primaryKey(false)
                .foreignKeyRelation(ForeignKeyRelation.builder()
                        .tableName(relationMetadata.getSourceTableName())
                        .columnName(relationMetadata.getSourceColumnName())
                        .type(relationMetadata.getType())
                        .build())
                .build());


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
