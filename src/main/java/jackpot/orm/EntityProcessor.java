package jackpot.orm;

import jackpot.orm.metadata.ColumnMetadata;
import jackpot.orm.metadata.ColumnType;
import jackpot.orm.metadata.RelationMetadata;
import jackpot.orm.metadata.TableMetadata;
import jackpot.utils.AnnotationUtils;
import jackpot.utils.JackpotUtils;
import lombok.Getter;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityProcessor {

    private final static List<String> standardTypes = Arrays.asList(String.class.getName(),
            Integer.class.getName(),
            Enum.class.getName(),
            LocalDate.class.getName(),
            LocalDateTime.class.getName());

    private final static List<String> specialAspectTypes = Arrays.asList("org.aspectj.lang.JoinPoint$StaticPart",
            "java.lang.annotation.Annotation");

    private final static List<Class<? extends Annotation>> relationAnnotations =
            Arrays.asList(OneToMany.class,
                    ManyToOne.class,
                    ManyToMany.class);

    @Getter
    private List<RelationMetadata> relations = new LinkedList();
    private final RelationProcessor relationProcessor = new RelationProcessor();

    TableMetadata process(Class<?> cl, Entity entityAnnotation) {
        String tableName = getTableName(cl, entityAnnotation);

        return TableMetadata.builder()
                .className(cl.getName())
                .tableClass(cl)
                .tableName(tableName)
                .columns(createColumnsMetadata(cl, entityAnnotation))
                .build();
    }

    private String getTableName(Class<?> cl, Entity entityAnnotation) {
        return JackpotUtils.getTableName(cl, entityAnnotation);
    }

    private List<ColumnMetadata> createColumnsMetadata(Class<?> cl, Entity entityAnnotation) {

        processRelations(cl, entityAnnotation);

        return Stream.of(cl.getDeclaredFields())
                .filter(field -> !isObjectField(field))
                .map(field -> {
                            Optional<Column> annotationColumn = Optional.ofNullable(field.getAnnotation(Column.class));

                            return ColumnMetadata.builder()
                                    .fieldName(field.getName())
                                    .columnName(getColumnName(field, annotationColumn))
                                    .columnType(toColumnType(field.getType()))
                                    .primaryKey(AnnotationUtils.isAnnotatedBy(field, Id.class))
                                    .notNull(annotationColumn.isPresent() ? !annotationColumn.get().nullable() : false)
                                    .build();
                        }
                ).collect(Collectors.toList());
    }

    private void processRelations(Class<?> cl, Entity entityAnnotation) {
        List<Field> objectFields = Stream.of(cl.getDeclaredFields())
                .filter(field -> isNOTSpecialAspectType(field))
                .filter(field -> isObjectField(field))
                .collect(Collectors.toList());

        if (!objectFields.isEmpty()) {
            List<Field> withoutRelations = objectFields.stream()
                    .filter(field -> !hasRelationAnnotations(field))
                    .collect(Collectors.toList());

            if (!withoutRelations.isEmpty()) {
                StringJoiner msgBuf = new StringJoiner("\n");
                msgBuf.add("Following fields are objects without relations: ");
                withoutRelations.forEach(field -> msgBuf.add(String.format("%s %s", cl.getName(), field.getName())));

                throw new IllegalStateException(msgBuf.toString());
            }

            List<Field> withRelations = objectFields.stream()
                    .filter(field -> hasRelationAnnotations(field))
                    .collect(Collectors.toList());

            relations.addAll(relationProcessor.process(cl, entityAnnotation, withRelations));
        }
    }

    private boolean isObjectField(Field field) {
        return !standardTypes.contains(field.getType().getName());
    }

    private boolean isNOTSpecialAspectType(Field field) {
        return !specialAspectTypes.contains(field.getType().getName());
    }

    private boolean hasRelationAnnotations(Field field) {

        return Stream.of(field.getDeclaredAnnotations())
                .map(ann -> ann.annotationType())
                .anyMatch(annClass -> relationAnnotations.contains(annClass));
    }

    private ColumnType toColumnType(Class<?> type) {
        try {
            return convertToColumnType(type);
        } catch (Exception ex) {
            throw new IllegalStateException("Convertion error: ", ex);
        }
    }

    private ColumnType convertToColumnType(Class<?> type) {
        // TODO sprawdzenie mozliwych konwerterow i ew zwrot wyniku

        if (Integer.class.getName().equals(type.getName()))
            return ColumnType.INTEGER;

        if (String.class.getName().equals(type.getName()))
            return ColumnType.STRING;

        throw new IllegalStateException("ColumnConvertionError There is no converter for type " + type.getName());
    }

    private String getColumnName(Field field, Optional<Column> annotationColumn) {
        return JackpotUtils.getColumnName(field, annotationColumn);
    }

}
