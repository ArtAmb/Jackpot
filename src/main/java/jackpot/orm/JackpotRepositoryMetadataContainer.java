package jackpot.orm;

import jackpot.orm.repository.JackpotRepository;
import jackpot.orm.repository.JackpotRepositoryMetadata;
import lombok.val;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JackpotRepositoryMetadataContainer {
    private static final Map<String, JackpotRepositoryMetadata> repositoryMetadataByRepoClassNameMap = new HashMap<>();


    public static JackpotRepositoryMetadata getRepoMetadata(String repoClassName) {
        return repositoryMetadataByRepoClassNameMap.get(repoClassName);
    }


    public static void loadRepos(Class<?>[] reposClasses) {
        {
            for (val repClass : reposClasses) {
                loadRepo((Class<? extends JackpotRepository>) repClass);
            }
        }
    }

    private static void loadRepo(Class<? extends JackpotRepository> repoClass) {

        ParameterizedType genericSuperclass = getGenericSuperclass(repoClass);

        Class<?> idClass = (Class<?>) genericSuperclass.getActualTypeArguments()[0];
        Class<?> tableClass = (Class<?>) genericSuperclass.getActualTypeArguments()[1];

        JackpotRepositoryMetadata metadata = JackpotRepositoryMetadata.builder()
                .repositoryClassName(repoClass.getName())
                .repositoryClass(repoClass)
                .idClass(idClass)
                .tableClass(tableClass)
                .build();

        repositoryMetadataByRepoClassNameMap.put(metadata.getRepositoryClassName(), metadata);
    }

    private static ParameterizedType getGenericSuperclass(Class<? extends JackpotRepository> repoClass) {
        String jackpotRepositoryInterfaceName = JackpotRepository.class.getName();

        Type type = Arrays.stream(repoClass.getGenericInterfaces())
                .filter(genericInterface -> genericInterface.getTypeName().contains(jackpotRepositoryInterfaceName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(repoClass.getName() + " does not implement " + jackpotRepositoryInterfaceName));


        return (ParameterizedType) type;
    }


}
