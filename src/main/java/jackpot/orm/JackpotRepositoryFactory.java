package jackpot.orm;

import jackpot.orm.repository.JackpotRepository;

public class JackpotRepositoryFactory {

    private static Object proxy = null;

    static void load(Object proxyImpl) {
        proxy = proxyImpl;
    }

    public static <T extends JackpotRepository> T createRepository(Class<T> repoClass) {
        return (T) proxy;
    }

}
