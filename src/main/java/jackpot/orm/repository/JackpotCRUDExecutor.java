package jackpot.orm.repository;

import jackpot.orm.JackpotDatabase;
import jackpot.orm.metadata.TableMetadata;

import java.util.Arrays;

public class JackpotCRUDExecutor {
    private final JackpotQueryExecutor jackpotQueryExecutor = new JackpotQueryExecutor();

    public void delete(Object pk, String responseClassName) {
        TableMetadata tableMetadata = JackpotDatabase.getInstance().getTableByClassName(responseClassName);
        String deleteSql = "deleteBy" + tableMetadata.getPrimaryKeyColumn().getFieldName();

        jackpotQueryExecutor.execute(deleteSql, responseClassName, pk);
    }

    public Object findOne(Object pk, String responseClassName) {
        TableMetadata tableMetadata = JackpotDatabase.getInstance().getTableByClassName(responseClassName);
        String sql = "findBy" + tableMetadata.getPrimaryKeyColumn().getFieldName();

        Object resultList = jackpotQueryExecutor.execute(sql, responseClassName, pk);
        return Arrays.stream((Object[])resultList).findFirst().get();
    }
}
