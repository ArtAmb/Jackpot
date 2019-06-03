package jackpot.orm.repository;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jackpot.orm.ConnectionManager;
import jackpot.orm.JackpotDatabase;
import jackpot.orm.JackpotLogger;
import jackpot.orm.metadata.ColumnMetadata;
import jackpot.orm.metadata.TableMetadata;
import jackpot.utils.Utils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class JackpotSaveExecutor {
    private final static String disableFkCheckSql = "SET FOREIGN_KEY_CHECKS=0;";
    private final static String enableFkCheckSql = "SET FOREIGN_KEY_CHECKS=1;";

    private final Gson gson = new Gson();

    public Object execute(String responseClassName, Object objToPersistence) throws SQLException, NoSuchFieldException, IllegalAccessException {
        TableMetadata tableMetadata = getTableMetadataByClassName(responseClassName);
        ConnectionManager connectionManager = ConnectionManager.createNew(false);

        JsonObject jsonObj = gson.toJsonTree(objToPersistence).getAsJsonObject();
        String sql = generateSql(tableMetadata, jsonObj);
        JackpotLogger.log(sql);

        connectionManager.executeSql(disableFkCheckSql);

        if (sql.substring(0, "INSERT".length()).equals("INSERT")) {
            Object pkValue = connectionManager.executeInsertSql(sql);
            Field declaredField = objToPersistence.getClass().getDeclaredField(tableMetadata.getPrimaryKeyColumn().getFieldName());
            declaredField.setAccessible(true);
            declaredField.set(objToPersistence, pkValue);
        } else {
            connectionManager.executeSql(sql);
        }

        connectionManager.executeSql(enableFkCheckSql);
        connectionManager.commit();
        connectionManager.close();

        return objToPersistence;
    }

    private String generateSql(TableMetadata tableMetadata, JsonObject jsonObj) {
        Map<String, String> tabColToValueMap = new HashMap<>();

        tableMetadata.getColumns().stream()
                .filter(col -> col.getForeignKeyRelation() == null)
                .forEach(col -> {
                    JsonElement value = jsonObj.get(col.getFieldName());
                    tabColToValueMap.put(col.getColumnName(), getValue(value, col));
                });

        tableMetadata.getColumns().stream()
                .filter(col -> col.getForeignKeyRelation() != null)
                .forEach(col -> {
                    JsonElement fkValue = jsonObj.get(col.getColumnName());
                    if (fkValue != null) {
                        TableMetadata fkTableMetadata = getTableMetadataByTableName(col.getForeignKeyRelation().getTableName());
                        JsonElement value = fkValue.getAsJsonObject().get(fkTableMetadata.getPrimaryKeyColumn().getColumnName());

                        tabColToValueMap.put(col.getColumnName(), getValue(value, col));

                    }
                });

        ColumnMetadata pkMetadata = tableMetadata.getPrimaryKeyColumn();
        String pkValue = tabColToValueMap.get(pkMetadata.getColumnName());

        if (!Utils.isBlank(pkValue) && !pkValue.equals("'0'") && !pkValue.equals("0") && !pkValue.equals("NULL")) {
            String setSequence = tabColToValueMap.entrySet().stream()
                    .map(entrySet -> String.format("%s=%s", entrySet.getKey(), entrySet.getValue()))
                    .collect(Collectors.joining(", "));

            return String.format("UPDATE %s SET %s WHERE %s = %s",
                    tableMetadata.getTableName(),
                    setSequence,
                    pkMetadata.getColumnName(),
                    pkValue);
        }

        return String.format("INSERT INTO %s (%s) VALUES(%s)",
                tableMetadata.getTableName(),
                tabColToValueMap.keySet().toString().replaceAll("(\\[|\\])", ""),
                tabColToValueMap.values().toString().replaceAll("(\\[|\\])", ""));
    }

    private String getValue(JsonElement value, ColumnMetadata columnMetadata) {
        return value != null ? String.format("'%s'", value.getAsString()) : columnMetadata.getDefaultValue();
    }

    private TableMetadata getTableMetadataByClassName(String responseClassName) {
        return JackpotDatabase.getInstance().getTableByClassName(responseClassName);
    }

    private TableMetadata getTableMetadataByTableName(String tableName) {
        return JackpotDatabase.getInstance().getTableByTableName(tableName);
    }
}
