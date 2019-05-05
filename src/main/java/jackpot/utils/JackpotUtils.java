package jackpot.utils;

import jackpot.orm.metadata.ColumnType;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.util.Optional;

public class JackpotUtils {

    public static String getTableName(Class<?> cl, Entity entityAnnotation) {
        if (!Utils.isBlank(entityAnnotation.name()))
            return entityAnnotation.name();

        return cl.getSimpleName();
    }

    public static String getColumnName(Field field, Optional<Column> annotationColumn) {
        if (annotationColumn.isPresent() && !Utils.isBlank(annotationColumn.get().name()))
            return annotationColumn.get().name();

        return field.getName();
    }

    public static  String toSqlType(ColumnType columnType) {
        switch (columnType) {

            case STRING:
                return "text";
            case INTEGER:
                return "integer";
        }

        throw new IllegalStateException("There is no sql type for " + columnType.name());
    }
}
