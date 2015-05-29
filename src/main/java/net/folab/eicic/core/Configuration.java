package net.folab.eicic.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;
import java.util.Properties;

public class Configuration {

    private static final String CHARSET = "UTF-8";

    private Properties properties;

    private String name;

    public Configuration(Class<?> type) {
        properties = new Properties();
        name = "conf/" + type.getName() + ".properties";
        try {
            Reader reader = new InputStreamReader(new FileInputStream(name),
                    CHARSET);
            properties.load(reader);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    public boolean getBoolean(String key) {
        String alternative = Boolean.FALSE.toString();
        return Boolean.parseBoolean(getConfiguration(key, alternative));
    }

    public void setBoolean(String key, boolean value) {
        properties.setProperty(key, String.valueOf(value));
        save();
    }

    public int getInteger(String key, int alternative) {
        Optional<String> value = getConfiguration(key);
        if (value.isPresent())
            return Integer.parseInt(value.get());
        else
            return alternative;
    }

    public void setInteger(String key, int value) {
        properties.setProperty(key, String.valueOf(value));
        save();
    }

    public void save() {
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(name),
                    CHARSET);
            properties.store(writer, null);
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
