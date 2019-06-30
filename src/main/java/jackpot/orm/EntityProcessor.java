package jackpot.orm;

import jackpot.orm.metadata.*;
import jackpot.utils.AnnotationUtils;
import jackpot.utils.JackpotUtils;
import lombok.Getter;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
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
    @Getter
    private List<TableMetadata> additionalTables = new LinkedList();
    private List<Field> manyToManyFields = new LinkedList<>();

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

    private boolean isManyToMany(Field field) {
        return AnnotationUtils.isAnnotatedBy(field, ManyToMany.class);
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

            this.manyToManyFields.addAll(objectFields.stream()
                    .filter(field -> isManyToMany(field))
                    .collect(Collectors.toList()));

            relations.addAll(relationProcessor.process(cl, entityAnnotation, withRelations));
        }
    }

    public List<TableMetadata> prepareAndGetAdditionalTables(List<TableMetadata> allTables) {
        Map<String, TableMetadata> allTablesMapByClassName = allTables.stream()
                .collect(Collectors.toMap(table -> table.getClassName(), table -> table));

        manyToManyFields.forEach(field -> {
            Optional<TableMetadata> generatedTable = buildTableMetadataFromManyToManyField(field, allTablesMapByClassName);
            if (generatedTable.isPresent())
                additionalTables.add(generatedTable.get());
        });


        return additionalTables;
    }

    private Optional<TableMetadata> buildTableMetadataFromManyToManyField(Field field, Map<String, TableMetadata> allTablesMapByClassName) {
        Class<?> source1Class = field.getDeclaringClass();
        Class<?> source2Class = null;
        if (JackpotUtils.isFieldInstanceOfClass(field, List.class)) {
            source2Class = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        } else {
            throw new IllegalStateException(String.format("%s[%s] is no LIST !!!!",
                    field.getName(),
                    source1Class.getName()));
        }

        TableMetadata source1Table = allTablesMapByClassName.get(source1Class.getName());
        TableMetadata source2Table = allTablesMapByClassName.get(source2Class.getName());

        ColumnMetadata pkColumn = ColumnMetadata.builder()
                .fieldName("id")
                .columnName("id")
                .columnType(ColumnType.INTEGER)
                .primaryKey(true)
                .notNull(true)
                .foreignKeyRelation(null)
                .build();
        String tabNameFormat = "tab_indirect_%s_%s";
        String tabName = String.format(tabNameFormat, source1Table.getTableName(), source2Table.getTableName());
        String alternativeTabName = String.format(tabNameFormat, source2Table.getTableName(), source1Table.getTableName());
        List<String> possibleTableNames = Arrays.asList(tabName, alternativeTabName);
        boolean tableAlreadyExist = additionalTables.stream()
                .filter(TableMetadata::isAutogenerated)
                .filter(tableMetadata -> possibleTableNames.contains(tableMetadata.getTableName()))
                .findFirst().isPresent();

        if (tableAlreadyExist)
            return Optional.empty();

        RelationMetadata fk1 = RelationMetadata.builder()
                .targetFieldName("fk1")
                .targetColumnName("fk1")
                .targetTableName(tabName)
                .sourceColumnName(source1Table.getPrimaryKeyColumn().getColumnName())
                .sourceTableName(source1Table.getTableName())
                .type(RelationType.MANY_TO_ONE)
                .targetColumnNotNull(false)
                .redundant(false)
                .build();

        RelationMetadata fk2 = RelationMetadata.builder()
                .targetFieldName("fk2")
                .targetColumnName("fk2")
                .targetTableName(tabName)
                .sourceColumnName(source2Table.getPrimaryKeyColumn().getColumnName())
                .sourceTableName(source2Table.getTableName())
                .type(RelationType.MANY_TO_ONE)
                .targetColumnNotNull(false)
                .redundant(false)
                .build();

        relations.addAll(Arrays.asList(fk1, fk2));

        return Optional.of(TableMetadata.builder()
                .className(tabName)
                .tableName(tabName)
                .tableClass(null)
                .columns(new LinkedList<>(Arrays.asList(pkColumn)))
                .autogenerated(true)
                .build());
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
