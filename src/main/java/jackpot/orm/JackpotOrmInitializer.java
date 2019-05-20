package jackpot.orm;

import jackpot.orm.metadata.RelationMetadata;
import jackpot.orm.metadata.TableMetadata;
import jackpot.orm.properties.DatabaseInitAction;
import jackpot.orm.properties.JackpotOrmProperties;
import jackpot.orm.repository.JackpotQueryExecutor;
import jackpot.orm.repository.JackpotRepository;
import jackpot.orm.repository.JackpotRepositoryMetadata;
import jackpot.orm.repository.JackpotSaveExecutor;
import org.reflections.Reflections;

import javax.persistence.Entity;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

public class JackpotOrmInitializer {

    volatile private List<String> PACKAGES_TO_SCAN;
    private final EntityProcessor entityProcessor = new EntityProcessor();
    private final JackpotTableSqlGenerator jackpotTableSqlGenerator = new JackpotTableSqlGenerator();
    private final JackpotDropTableService jackpotDropTableService = new JackpotDropTableService();
    private final JackpotQueryExecutor jackpotQueryExecutor = new JackpotQueryExecutor();
    private final JackpotSaveExecutor jackpotSaveExecutor = new JackpotSaveExecutor();

    volatile private boolean running = false;

    private List<TableMetadata> allTables = new LinkedList<>();
    private List<RelationMetadata> allRelations = new LinkedList<>();

    static private JackpotOrmInitializer instance;

    synchronized public static JackpotOrmInitializer getInstance() {
        if (instance == null) {
            instance = new JackpotOrmInitializer();
        }

        return instance;
    }

    private JackpotOrmInitializer() {

    }


    public void init() throws ClassNotFoundException, SQLException {
        if (running) {
            return;
        }
        running = true;


        loadProperties();
        loadPackagesToScan();
        initDatabase();

        handleDatabaseInitAction();

        JackpotDatabase.init(allTables);

        createInterfaceImpl();
    }

    private void createInterfaceImpl() {
        Reflections ref = new Reflections(PACKAGES_TO_SCAN);
        Set<Class<? extends JackpotRepository>> allRepositories = ref.getSubTypesOf(JackpotRepository.class);
        Class<?>[] allReposArray = allRepositories.toArray(new Class<?>[allRepositories.size()]);
        JackpotRepositoryMetadataContainer.loadRepos(allReposArray);

        Object proxyImpl = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), allReposArray, (proxy, method, args) -> {

            System.out.println("PROXY CLASS == " + method.getDeclaringClass().getName());
            String methodName = method.getName();
            if (methodName.equals("toString")) {
                return method.getDeclaringClass().getName();
            }
            JackpotRepositoryMetadata repoMetadata = JackpotRepositoryMetadataContainer.getRepoMetadata(method.getDeclaringClass().getName());

            if (methodName.equals("save"))
                return jackpotSaveExecutor.execute(args[0].getClass().getName(), args[0]);

            return jackpotQueryExecutor.execute(methodName, repoMetadata.getTableClass().getName(), args);
        });

        JackpotRepositoryFactory.load(proxyImpl);
    }

    private void loadPackagesToScan() throws ClassNotFoundException {
        PACKAGES_TO_SCAN = Collections.singletonList(getPackagesToScan());
    }

    private String getPackagesToScan() throws ClassNotFoundException {
        String callingClass = getCallingClassName();
        Optional<Package> packageName = Optional.ofNullable(ClassLoader.getSystemClassLoader()
                .loadClass(callingClass)
                .getPackage());

        if (packageName.isPresent())
            return preparePackageName(packageName.get().getName());
        else
            return "";
    }

    private String preparePackageName(String packageName) {
        return packageName.replace(Pattern.quote("."), Pattern.quote("/"));
    }

    private void loadProperties() {
        Properties prop = new Properties();
        String filename = "/config.properties";
        try (InputStream input = this.getClass().getResourceAsStream(filename)) {
            if (input == null) {
                throw new IllegalStateException("Cannot load properties file " + filename);
            }

            prop.load(input);
            JackpotOrmProperties.load(prop);
        } catch (IOException e) {
            throw new IllegalStateException("Error during loading properties ", e);
        }

    }

    private String getCallingClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        String className = getClass().getName();

        for (int i = 0; i < stackTrace.length; ++i) {
            if (stackTrace[i].getClassName().equals(className)
                    && stackTrace[i].getMethodName().equals("init"))

                return stackTrace[i + 2].getClassName();
        }

        throw new IllegalStateException("THERE IS NO CALLING CLASS");
    }


    private void initDatabase() {
        Reflections ref = new Reflections(PACKAGES_TO_SCAN);
        for (Class<?> cl : ref.getTypesAnnotatedWith(Entity.class)) {
            Entity entity = cl.getAnnotation(Entity.class);
            TableMetadata tableMetadata = entityProcessor.process(cl, entity);
            allTables.add(tableMetadata);
        }

        allRelations.addAll(entityProcessor.getRelations());
    }

    private void handleDatabaseInitAction() {
        DatabaseInitAction databaseInitAction = JackpotOrmProperties.getDatabaseInitAction();

        if (databaseInitAction == null)
            return;

        switch (databaseInitAction) {

            case DROP_CREATE:
                dropDatabase();
            case CREATE:
                createDatabase();
                return;

            default:
                throw new IllegalStateException(String.format("DatabaseInitAction %s not handled !", databaseInitAction));
        }


    }

    private void dropDatabase() {
        jackpotDropTableService.dropTables(allTables);
    }

    private void createDatabase() {

        ConnectionManager connectionManager = ConnectionManager.createNew();

        allTables.forEach(tableMetadata -> {
            String sql = jackpotTableSqlGenerator.createSql(tableMetadata);
            connectionManager.executeSql(sql);
        });

        final JackpotRelationSqlGenerator jackpotRelationSqlGenerator = new JackpotRelationSqlGenerator(allTables);
        allRelations.forEach(relationMetadata -> {
            String sql = jackpotRelationSqlGenerator.createSql(relationMetadata);
            Arrays.asList(sql.split(";")).forEach(oneQuery -> {
                connectionManager.executeSql(oneQuery + ";");
            });

        });

        connectionManager.close();
    }

}
