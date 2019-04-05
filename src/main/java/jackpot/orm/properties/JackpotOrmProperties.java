package jackpot.orm.properties;

import jackpot.utils.Utils;

import java.util.Properties;


public class JackpotOrmProperties {
    static private JackpotOrmProperties instance;


    private final String url;
    private final String login;
    private final String password;
    private final DatabaseInitAction databaseInitAction;

    private JackpotOrmProperties(Properties prop) {
        this.url = prop.getProperty("jackpot.connection_url");
        this.login = prop.getProperty("jackpot.user_login");
        this.password = prop.getProperty("jackpot.password");
        String databaseAction = prop.getProperty("jackpot.database.init.action");
        this.databaseInitAction = !Utils.isBlank(databaseAction) ? DatabaseInitAction.valueOf(databaseAction) : null;
    }


    synchronized public static void load(Properties prop) {
        if (instance == null) {
            instance = new JackpotOrmProperties(prop);
        }
    }


    public static String getUrl() {
        return instance.url;
    }

    public static String getLogin() {
        return instance.login;
    }

    public static String getPassword() {
        return instance.password;
    }

    public static DatabaseInitAction getDatabaseInitAction() {
        return instance.databaseInitAction;
    }
}
