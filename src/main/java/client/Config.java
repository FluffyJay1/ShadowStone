package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Class to store, serialize and deserialize config settings
 * 
 * @author micha
 *
 */
public class Config {
    public static int WINDOW_WIDTH = 1920;
    public static int WINDOW_HEIGHT = 1080;

    /**
     * Serializes and saves the config to file
     */
    public void saveToFile() {
        File f = new File("config.dat");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream file = new FileOutputStream(f);
            ObjectOutputStream obj = new ObjectOutputStream(file);
            obj.writeObject(this);
            obj.close();
            file.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Deserializes the config from file and loads them
     */
    public static Config loadFromFile() {
        File f = new File("config.dat");
        if (f.exists()) {
            try {
                FileInputStream file = new FileInputStream(f);
                ObjectInputStream obj = new ObjectInputStream(file);
                Config config = (Config) obj.readObject();
                obj.close();
                file.close();
                return config;
            } catch (IOException | ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            try {
                f.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return new Config();
    }
}
