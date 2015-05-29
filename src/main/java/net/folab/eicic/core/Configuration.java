package net.folab.eicic.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;
import java.util.Properties;

public class Configuration {

    private Properties properties;

    public Configuration(Class<?> type) {
        properties = new Properties();
        try {
            Reader reader = new InputStreamReader(new FileInputStream("conf/"
                    + type.getName() + ".properties"), "UTF-8");
            properties.load(reader);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean getBoolean(String key) {
        String alternative = Boolean.FALSE.toString();
        return Boolean.parseBoolean(getConfiguration(key, alternative));
    }

    public Optional<String> getConfiguration(String key) {
        if (properties.containsKey(key))
            return Optional.of(properties.getProperty(key));
        else
            return Optional.empty();
    }

    public String getConfiguration(String key, String alternative) {
        Optional<String> optional = getConfiguration(key);
        if (optional.isPresent())
            return optional.get();
        else
            return alternative;
    }

}
