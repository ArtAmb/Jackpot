package jackpot.utils;

public class Utils {

    public static boolean isBlank(String str) {
        return str == null || str.trim().equals("");
    }

    public static void assertIfTrue(boolean condition, String msg) {
        if (!condition)
            throw new IllegalStateException(msg);
    }

    public static void assertNotNull(Object obj, String msg) {
        assertIfTrue(obj != null, msg);
    }
}
