package ab.trainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import static java.lang.String.format;

public class PropertiesProvider {

    Properties properties;
    private String propertiesFilePath;

    public PropertiesProvider(String propertiesFilePath){
        this.propertiesFilePath = propertiesFilePath;
        try {
            properties = new Properties();
            File file = new File(propertiesFilePath);
            if(!file.exists())  {
                File dir = new File(file.getParent());
                if(!dir.exists()) dir.mkdirs();
                file.createNewFile();
            }
            FileInputStream fis = new FileInputStream(propertiesFilePath);
            properties.load(fis);
        }catch (Exception exc){
            throw new RuntimeException(format("Failed to load properties from file %s", propertiesFilePath), exc );
        }
    }

    public String get(ApplicationProperty applicationProperty) {
        return properties.getProperty(applicationProperty.name());
    }

    public void set(ApplicationProperty applicationProperty, String value) {
        try {
            properties.setProperty(applicationProperty.name(), value);
            properties.store(new FileWriter(propertiesFilePath), "ab");
        } catch(IOException e) {
            throw new RuntimeException(format("Failed to save properties to file %s", propertiesFilePath), e);
        }
    }
}
