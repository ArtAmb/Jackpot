package jackpot.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AnnotationUtils {

    public static boolean isAnnotatedBy(Field field, Class annotation) {
        return field.getAnnotation(annotation) != null;
    }

    public static boolean isRelation(Field field, Class relationClass) {
        if (JackpotUtils.isFieldInstanceOfClass(field, List.class)) {
            Class<?> fieldClass = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            Optional<Field> fkField = Arrays.stream(fieldClass.getDeclaredFields())
                    .filter(fi -> AnnotationUtils.isAnnotatedBy(fi, relationClass))
                    .findFirst();

            return fkField.isPresent();
        }

        return false;
    }
}
