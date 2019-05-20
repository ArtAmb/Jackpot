package jackpot.orm.repository;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class A {
    int tmp = 1;
    B b = new B();
    C c = new C();
}

class B {
    int tmp = 5;
    int tmp1 = 8;
    C c1 = new C();
    C c2 = new C();
}

class C {
    int tmp = 4;
    int tmp1 = 8;
}

public class JackpotTmpMain {


    public static void main(String[] args) {
        JsonElement jsonEl = new Gson().toJsonTree(new A());

        Map<String, Object> mapObj = new Gson().fromJson(jsonEl
                , new TypeToken<HashMap<String, Object>>() {
                }.getType()
        );

        List<Map.Entry<String, ?>> result = asFlattendMap(mapObj);

        System.out.println("OK");
    }

    public static List<Map.Entry<String, ?>> asFlattendMap(Map<String, Object> map) {
        return map.entrySet().stream()
                .flatMap(child -> flatten(child))
                .collect(Collectors.toList());
    }

    private static Stream<Map.Entry<String, ?>> flatten(Map.Entry<String, ?> entry) {
        if (entry.getValue() instanceof Map) {
            return ((Map<String,?>) entry.getValue()).entrySet().stream().flatMap(child -> flatten(child));
        }
        return Stream.of(entry);
    }

}
