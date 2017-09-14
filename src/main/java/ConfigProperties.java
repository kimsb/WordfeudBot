import java.util.Properties;

public class ConfigProperties {

    private Properties properties = new Properties();

    public ConfigProperties() {
//        try {
//            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public String getProperty(final String propertyName) {
//        return properties.getProperty(propertyName);
        return System.getProperty(propertyName);
    }
}
