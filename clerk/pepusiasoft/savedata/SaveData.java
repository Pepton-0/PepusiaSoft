package clerk.pepusiasoft.savedata;

import clerk.pepusiasoft.Debug;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class SaveData {

    private static class DataPacker implements Serializable {
        private static final long serialVersionUID = 34345L;
        private HashMap<String, Object> map = new HashMap<>();
    }

    private static DataPacker packer;
    private static ArrayList<Runnable> savingTasks = new ArrayList<>();

    public static boolean serialize(String fileName) {
        try{
            SaveData.runSavingTasks();
        }catch(Throwable t){
            t.printStackTrace();
        }

        try {
            Debug.log("Saved application data at " + fileName);
            FileOutputStream fStream = new FileOutputStream(getJarDirectory().toString() + "/" + fileName);
            ObjectOutputStream oStream = new ObjectOutputStream(fStream);

            oStream.writeObject(packer);
            oStream.flush();
            oStream.close();

            return true; // succeed
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            Debug.log("Failed to serialize at " + fileName);

            return false; // failed
        }
    }

    public static boolean deserialize(String fileName) {
        try {
            FileInputStream fStream = new FileInputStream(getJarDirectory().toString() + "/" + fileName);
            ObjectInputStream oStream = new ObjectInputStream(fStream);

            packer = ((DataPacker) oStream.readObject());
            oStream.close();

            return true; // succeed
        } catch (IOException | URISyntaxException | ClassNotFoundException e) {
            Debug.log("Failed to deserialize");
            packer = new DataPacker();

            return false; // failed
        }
    }

    public static <T> T get(String location, T defaultValue) {
        return (T) packer.map.getOrDefault(location, defaultValue);
    }

    public static <T> T get(String location) {
        return (T) packer.map.getOrDefault(location, null);
    }

    public static void set(String location, Object value) {
        packer.map.put(location, value);
    }

    public static void addSavingTask(Runnable runnable) {
        savingTasks.add(runnable);
    }

    public static void runSavingTasks() {
        for (Runnable run : savingTasks) {
            run.run();
        }
    }

    // jarファイルが存在するフォルダ(をファイルとして認識した物)を表示する
    public static Path getJarDirectory() throws URISyntaxException {
        return Paths.get(SaveData.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
    }
}