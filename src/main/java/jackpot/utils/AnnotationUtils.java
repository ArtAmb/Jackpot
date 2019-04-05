package jackpot.utils;

import java.lang.reflect.Field;

public class AnnotationUtils {

    public static boolean isAnnotatedBy(Field field, Class annotation) {
        return field.getAnnotation(annotation) != null;
    }
}
