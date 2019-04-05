package jackpot.orm;

import jackpot.orm.metadata.ColumnMetadata;
import jackpot.orm.metadata.ColumnType;
import jackpot.orm.metadata.RelationMetadata;
import jackpot.orm.metadata.TableMetadata;
import jackpot.utils.AnnotationUtils;
import jackpot.utils.Utils;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityProcessor {


    @Getter
    private List<RelationMetadata> relations = new LinkedList();

    TableMetadata process(Class<?> cl, Entity entityAnnotation) {
        String tableName = getTableName(cl, entityAnnotation);

        return TableMetadata.builder()
                .tableName(tableName)
                .columns(createColumnsMetadata(cl, entityAnnotation))
                .build();
    }

    private String getTableName(Class<?> cl, Entity entityAnnotation) {
        if (!Utils.isBlank(entityAnnotation.name()))
            return entityAnnotation.name();

        return cl.getSimpleName();
    }

    private List<ColumnMetadata> createColumnsMetadata(Class<?> cl, Entity entityAnnotation) {

        return Stream.of(cl.getDeclaredFields()).map(field -> {
                    Optional<Column> annotationColumn = Optional.ofNullable(field.getAnnotation(Column.class));

                    return ColumnMetadata.builder()
                            .columnName(getColumnName(field, annotationColumn))
                            .columnType(toColumnType(field.getType()))
                            .primaryKey(AnnotationUtils.isAnnotatedBy(field, Id.class))
                            .notNull(annotationColumn.isPresent() ? !annotationColumn.get().nullable() : false)
                            .build();
                }
        )
                .collect(Collectors.toList());
    }

    private ColumnType toColumnType(Class<?> type) {
        try {
            return convertToColumnType(type);
        } catch (Exception ex) {
            throw new IllegalStateException("Convertion error: ", ex);
        }
    }

    private ColumnType convertToColumnType(Class<?> type) throws IllegalAccessException, InstantiationException {
        // TODO sprawdzenie mozliwych konwerterow i ew zwrot wyniku

        if (Integer.class.getName().equals(type.getName()))
            return ColumnType.INTEGER;

        if (String.class.getName().equals(type.getName()))
            return ColumnType.STRING;

        throw new IllegalStateException("ColumnConvertionError There is no cnverter for type " + type.getName());
    }

    private String getColumnName(Field field, Optional<Column> annotationColumn) {
        if (annotationColumn.isPresent() && !Utils.isBlank(annotationColumn.get().name()))
            return annotationColumn.get().name();

        return field.getName();
    }

}
