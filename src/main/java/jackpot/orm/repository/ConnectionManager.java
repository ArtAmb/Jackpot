package jackpot.orm.repository;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mysql.cj.result.Field;
import jackpot.orm.JackpotDatabase;
import jackpot.orm.metadata.ColumnMetadata;
import jackpot.orm.metadata.RelationType;
import jackpot.orm.metadata.TableMetadata;
import jackpot.orm.properties.JackpotOrmProperties;
import lombok.Builder;
import lombok.Data;
import lombok.val;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.io.Closeable;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Builder
@Data
class TmpTable {
    String tabName;

    String realTableName;
    String pkValue;

    JsonObject jsonObject;
}

@Builder
@Data
class TableToFetch {
    TableMetadata tableMetadata;
    JsonObject jsonObject;
}

public class ConnectionManager implements Closeable {
    private final Connection connection;
    private Savepoint savepoint;
    private final QueryRunner queryRunner = new QueryRunner();
    private final JackpotSqlGenerator jackpotSqlGenerator = new JackpotSqlGenerator();
    private final Gson gson = new Gson();


    static ConnectionManager createNew() {
        return new ConnectionManager(true);
    }

    static ConnectionManager createNew(boolean autoCommit) {
        return new ConnectionManager(autoCommit);
    }

    private ConnectionManager(boolean autoCommit) {
        try {
            this.connection = DriverManager
                    .getConnection(JackpotOrmProperties.getUrl(), JackpotOrmProperties.getLogin(), JackpotOrmProperties.getPassword());

            this.connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean isAutoCommit() throws SQLException {
        return connection.getAutoCommit();
    }

    public void executeSql(String sql) {
        try {
            this.connection.createStatement().execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public Object executeInsertSql(String sql) {
        try {
            PreparedStatement statement = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

//    public Object executeQuery(String sql, Class<?> responseClass) {
//        try {
//            val resultMap = queryRunner.query(this.connection, sql, new MapListHandler());
//            String resultJson = gson.toJson(resultMap);
//            Class<?> arrayClass = Class.forName("[L" + responseClass.getName() + ";");
//
//            return gson.fromJson(resultJson, arrayClass);
//
//        } catch (SQLException e) {
//            throw new IllegalStateException(e);
//        } catch (ClassNotFoundException e) {
//            throw new IllegalStateException(e);
//        }
//    }

    public Object executeQuery(String sql, Class<?> responseClass) {
        try {
            TableMetadata responseTableMetadata = JackpotDatabase.getInstance().getTableByClassName(responseClass.getName());

            val resultMap = queryRunner.query(this.connection, sql, new MapListHandler());
            String resultJson = gson.toJson(resultMap);
            Class<?> arrayClass = Class.forName("[L" + responseClass.getName() + ";");


            ResultSet rs = connection.createStatement().executeQuery(sql);
            ResultSetMetaData metadataRS = rs.getMetaData();
            Field[] fields = ((com.mysql.cj.jdbc.result.ResultSetMetaData) metadataRS).getFields();

            ArrayList<JsonObject> resultList = new ArrayList<>();
            while (rs.next()) {
                resultList.add(toJSONObj(rs, metadataRS, fields, responseTableMetadata));
            }

            JsonElement jsonResultList = gson.toJsonTree(resultList);
            return gson.fromJson(jsonResultList, arrayClass);

//            return resultList.stream()
//                    .map(jsonObj -> gson.fromJson(jsonObj, responseClass))
//                    .toArray();

//            return gson.fromJson(resultJson, arrayClass);

        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

//    public Object executeQuery(String sql, TableMetadata tableMetadata) {
//        try {
//            ArrayList<?> list = new ArrayList<>();
//            ResultSet rs = connection.createStatement().executeQuery(sql);
//            ResultSetMetaData metadataRS = rs.getMetaData();
//            Field[] fields = ((com.mysql.cj.jdbc.result.ResultSetMetaData) metadataRS).getFields();
//
//            while (rs.next()) {
//
//                toJSONObj(rs, metadataRS, fields);
//
//
//            }
//
//
//            return gson.fromJson(resultJson, tableMetadata.getTableClass());
//
//        } catch (SQLException e) {
//            throw new IllegalStateException(e);
//        } catch (ClassNotFoundException e) {
//            throw new IllegalStateException(e);
//        }
//    }


    private JsonObject toJSONObj(ResultSet rs, ResultSetMetaData metadataRS, Field[] fields, TableMetadata mainTable)
            throws SQLException {
        HashMap<String, TmpTable> mapByTabLabel = toTmpMap(rs, metadataRS, fields);

        Map<String, List<TmpTable>> mapByTabName = mapByTabLabel
                .values().stream()
                .collect(Collectors.groupingBy(TmpTable::getRealTableName));

        JsonObject resultObj = mapByTabName.get(mainTable.getTableName().toLowerCase()).get(0).getJsonObject();

        List<TableToFetch> tabsToFetch = new LinkedList<>();
        tabsToFetch.add(TableToFetch.builder()
                .tableMetadata(mainTable)
                .jsonObject(resultObj)
                .build());

        while (!tabsToFetch.isEmpty()) {
            TableToFetch tabToFetch = tabsToFetch.remove(0);
            TableMetadata tableMetadata = tabToFetch.getTableMetadata();
            JsonObject jsonObject = tabToFetch.getJsonObject();

            tableMetadata.getColumns().stream()
                    .filter(col -> col.getForeignKeyRelation() != null)
                    .filter(col -> RelationType.MANY_TO_ONE.equals(col.getForeignKeyRelation().getType()))
                    .forEach(col -> {

                        if (!jsonObject.get(col.getColumnName()).isJsonNull()) {
                            TableMetadata relatedTabMetadata = JackpotDatabase.getInstance().getTableByTableNameCaseInsensitive(col.getForeignKeyRelation().getTableName());
                            String fkValue = jsonObject.get(col.getColumnName()).getAsString();

//                            fetchLackingTables
                            List<TmpTable> tmpTables = mapByTabName.get(col.getForeignKeyRelation().getTableName().toLowerCase());
                            if(tmpTables == null) {
                                mapByTabLabel.putAll(fetchLackingTables(relatedTabMetadata, col, fkValue));
                                mapByTabName.clear();
                                mapByTabName.putAll(mapByTabLabel.values().stream()
                                        .collect(Collectors.groupingBy(TmpTable::getRealTableName)));
                                tmpTables = mapByTabName.get(col.getForeignKeyRelation().getTableName().toLowerCase());
                            }

                            TmpTable relatedTab = tmpTables.stream()
                                    .filter(tmpTab -> tmpTab.getPkValue().equalsIgnoreCase(fkValue))
                                    .findFirst()
                                    .orElseThrow(() ->
                                            new IllegalStateException(String.format("There is no value table %s with id %s",
                                                    col.getForeignKeyRelation().getTableName(), fkValue)));

                            jsonObject.remove(col.getColumnName());
                            jsonObject.add(col.getColumnName(), relatedTab.getJsonObject());


                            tabsToFetch.add(TableToFetch.builder()
                                    .tableMetadata(relatedTabMetadata)
                                    .jsonObject(relatedTab.getJsonObject())
                                    .build());
                        }
                    });
        }

        return resultObj;
    }

    private HashMap<String, TmpTable> toTmpMap(ResultSet rs, ResultSetMetaData metadataRS, Field[] fields) throws SQLException {
        HashMap<String, TmpTable> mapByTabLabel = new HashMap<>();

        int colIdx = 1;
        for (Field field : fields) {
            String tabName = field.getTableName();
            TableMetadata tab = JackpotDatabase.getInstance().getTableByTableNameCaseInsensitive(field.getOriginalTableName());
            TmpTable tmpTable = mapByTabLabel.getOrDefault(tabName, TmpTable.builder()
                    .tabName(tabName)
                    .pkValue(null)
                    .realTableName(field.getOriginalTableName())
                    .jsonObject(new JsonObject())
                    .build());

            String colName = metadataRS.getColumnName(colIdx);
            String colValue = rs.getString(colIdx);
            if (tmpTable.getPkValue() == null && isPK(tab, colName)) {
                tmpTable.setPkValue(colValue);
            }

            tmpTable.getJsonObject().addProperty(tab.getColumn(colName).getFieldName(), colValue);
            mapByTabLabel.put(tmpTable.getTabName(), tmpTable);

            ++colIdx;
        }

        return mapByTabLabel;
    }

    private HashMap<String, TmpTable> fetchLackingTables(TableMetadata relatedTabMetadata, ColumnMetadata col, String fkValue) {
        String sql = jackpotSqlGenerator.generateSql(relatedTabMetadata, "findBy" + col.getForeignKeyRelation().getColumnName(), fkValue);
        try {
            ResultSet rs = connection.createStatement().executeQuery(sql);
            ResultSetMetaData metadataRS = rs.getMetaData();
            Field[] fields = ((com.mysql.cj.jdbc.result.ResultSetMetaData) metadataRS).getFields();

            if (rs.next()) {
                return toTmpMap(rs, metadataRS, fields);
            } else {
                throw new IllegalStateException("Cannot fetch table " + relatedTabMetadata.getTableName() + " for FK " + fkValue);
            }


        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }


    }

    private boolean isPK(TableMetadata tab, String colName) {
        return tab.getPrimaryKeyColumn().getColumnName().toLowerCase().equals(colName.toLowerCase());
    }

    public void commit() {
        try {
            this.connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void savepoint() throws SQLException {
        this.savepoint = this.connection.setSavepoint();
    }

    public void rollbackSavepoint() throws SQLException {
        this.connection.releaseSavepoint(savepoint);
    }

    public void rollback() throws SQLException {
        this.connection.rollback();
    }

    @Override
    public void close() {
        try {
            if(isAutoCommit()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException();
        }
    }

    void forceClose() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException();
        }
    }


}
