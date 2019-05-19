package jackpot.orm.repository;

import jackpot.orm.ConnectionManager;
import jackpot.orm.JackpotDatabase;
import jackpot.orm.metadata.ColumnMetadata;
import jackpot.orm.metadata.RelationType;
import jackpot.orm.metadata.TableMetadata;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class JackpotQueryExecutor {

    public Object execute(String queryString, String responseClassName, Object... arguments) {
        TableMetadata tableMetadata = getTableMetadata(responseClassName);
        ConnectionManager connectionManager = ConnectionManager.createNew();

        String sql = generateSql(tableMetadata, queryString, arguments);
        Object result = connectionManager.executeQuery(sql, tableMetadata.getTableClass());
        connectionManager.close();

        return result;
    }

    private TableMetadata getTableMetadata(String responseClassName) {
        return JackpotDatabase.getInstance().getTable(responseClassName);
    }

    private String generateSql(TableMetadata tableMetadata, String queryString, Object... arguments) {
        String tablePrefix = tableMetadata.getTableName();

        String[] query = queryString.split("By");
        String selectPart = parseSelectPart(tablePrefix, tableMetadata, query[0]);

        String[] conditions = query[1].split("And");
        List<String> parsedConditions = parseConditions(tablePrefix, tableMetadata, conditions, arguments);
        String conditionPart = String.join(" AND ", parsedConditions);

        return String.format("%s WHERE 1=1 AND %s", selectPart, conditionPart);
    }

    private String parseSelectPart(String tablePrefix, TableMetadata tableMetadata, String s) {
        switch (s) {
            case "find":
                break;

            default:
                throw new IllegalStateException("Not recognized criteria api command: " + s);
        }


        String startSentence = String.format("SELECT * FROM %s %s ", tableMetadata.getTableName(), tablePrefix);

        String joinPart = generateJoinSql(tablePrefix, tableMetadata);


        return startSentence + joinPart;
    }

    private String generateJoinSql(String tablePrefix, TableMetadata tableMetadata) {
        List<ColumnMetadata> fks = tableMetadata.getColumns()
                .stream()
                .filter(col -> col.getForeignKeyRelation() != null)
                .filter(col -> RelationType.MANY_TO_ONE.equals(col.getForeignKeyRelation().getType()))
                .collect(Collectors.toList());


        StringBuffer strBuf = new StringBuffer();
        fks.forEach(fk -> {
            strBuf.append(String.format(" JOIN %s %s ON %s.%s = %s.%s ",
                    fk.getForeignKeyRelation().getTableName(),
                    fk.getForeignKeyRelation().getTableName(),
                    tablePrefix,
                    fk.getColumnName(),
                    fk.getForeignKeyRelation().getTableName(),
                    fk.getForeignKeyRelation().getColumnName()));
        });


        return strBuf.toString();
    }

    private List<String> parseConditions(String tabPrefix, TableMetadata tableMetadata, String[] conditions, Object... arguments) {
        List<String> results = new LinkedList<>();

        int argumentsIndex = 0;
        for (String condition : conditions) {
            if (condition.contains("Is")) {
                String[] parts = condition.split("Is");
                results.add(parseCondition(tabPrefix, parts[0], parts[1]));
            }

            results.add(parseCondition(tabPrefix, condition, arguments[argumentsIndex++]));
        }

        return results;
    }

    private String parseCondition(String tabPrefix, String condition, Object value) {
        return String.format("%s.%s = %s", tabPrefix, condition, value);
    }

}