package jackpot.orm.repository;

import jackpot.orm.JackpotDatabase;
import jackpot.orm.JackpotLogger;
import jackpot.orm.metadata.TableMetadata;

public class JackpotQueryExecutor {

    private JackpotSqlGenerator jackpotSqlGenerator = new JackpotSqlGenerator();

    public Object execute(String queryString, String responseClassName, Object... arguments) {
        TableMetadata tableMetadata = getTableMetadata(responseClassName);
        ConnectionManager connectionManager = ConnectionManager.createNew();

        String sql = generateSql(tableMetadata, queryString, arguments);
        JackpotLogger.log(sql);

        Object result = null;
        if (sql.substring(0, "DELETE".length()).equals("DELETE")) {
            connectionManager.executeSql(sql);
        } else {
            result = connectionManager.executeQuery(sql, tableMetadata.getTableClass());
        }

        connectionManager.close();

        return result;
    }

    private TableMetadata getTableMetadata(String responseClassName) {
        return JackpotDatabase.getInstance().getTableByClassName(responseClassName);
    }

    private String generateSql(TableMetadata tableMetadata, String queryString, Object... arguments) {
        return jackpotSqlGenerator.generateSql(tableMetadata, queryString, arguments);
    }

}
