package jackpot.orm;

import jackpot.orm.metadata.RelationMetadata;
import jackpot.orm.metadata.RelationType;
import jackpot.utils.AnnotationUtils;
import jackpot.utils.JackpotUtils;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RelationProcessor {
    public List<RelationMetadata> process(Class<?> ownerClass, Entity ownerEntityAnnotation, List<Field> fieldsWithRelations) {

        try {
            return fieldsWithRelations.stream().map(field -> {
                Optional<Column> annotationColumn = Optional.ofNullable(field.getAnnotation(Column.class));

                return RelationMetadata.builder()
                        .targetFieldName(field.getName())
                        .targetColumnName(JackpotUtils.getColumnName(field, annotationColumn))
                        .targetTableName(JackpotUtils.getTableName(ownerClass, ownerEntityAnnotation))
                        .sourceColumnName(getSourceColumnName(field))
                        .sourceTableName(getSourceTableName(field))
                        .type(getRelationType(field))
                        .targetColumnNotNull(annotationColumn.isPresent() ? !annotationColumn.get().nullable() : false)
                        .build();

            }).collect(Collectors.toList());
        } catch (Exception ex) {
            throw new IllegalStateException(String.format("ERROR in class %s : %s", ownerClass.getName(), ex.getMessage()), ex);
        }
    }

    private String getSourceTableName(Field field) {
        Class<?> fieldClass = field.getType();
        Entity fieldEntityAnnotation = fieldClass.getAnnotation(Entity.class);
        if (fieldEntityAnnotation == null)
            throw new IllegalStateException(String.format("%s is not an ENTITY ", fieldClass.getName()));

        return JackpotUtils.getTableName(fieldClass, fieldEntityAnnotation);
    }

    private String getSourceColumnName(Field field) {
//        if(isAnnotateBy sth)
//            return customFk;

        Class<?> fieldClass = field.getType();

        Field pkField = Arrays.stream(fieldClass.getDeclaredFields())
                .filter(fi -> AnnotationUtils.isAnnotatedBy(fi, Id.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("There is no PK for " + fieldClass.getName()));

        return JackpotUtils.getColumnName(pkField);
    }

    private RelationType getRelationType(Field field) {
        if (AnnotationUtils.isAnnotatedBy(field, OneToMany.class))
            return RelationType.ONE_TO_MANY;

        if (AnnotationUtils.isAnnotatedBy(field, ManyToMany.class))
            return RelationType.MANY_TO_MANY;

        if (AnnotationUtils.isAnnotatedBy(field, ManyToOne.class))
            return RelationType.MANY_TO_ONE;

        throw new IllegalStateException(String.format("Unrecognized reltation %s", field.getName()));
    }
}
