package clerk.pepusiasoft;

import clerk.pepusiasoft.language.Lang;
import clerk.pepusiasoft.savedata.SaveData;
import clerk.pepusiasoft.stages.Preferences;
import clerk.pepusiasoft.timersystem.RTimer;
import clerk.pepusiasoft.stages.Window;
import clerk.pepusiasoft.utils.NativeAccessor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;

public class App extends Application {

    public static String APP_NAME = Lang.of("title");
    public static Preferences preferences;
    public static Window window;
    public static final String defaultSerializeFileName = "pepusiaSoftData";

    private static boolean exited = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // primaryStage.close(); // 必要ない！
        Debug.log(APP_NAME + " is activated.");

        long l1 = System.currentTimeMillis();
        RTimer.start(50L);
        long l2 = System.currentTimeMillis();
        SaveData.deserialize(defaultSerializeFileName);
        long l3 = System.currentTimeMillis();
        NativeAccessor.start();
        long l4 = System.currentTimeMillis();
        preferences = new Preferences();
        long l5 = System.currentTimeMillis();
        window = new Window(this);
        long l6 = System.currentTimeMillis();

        Debug.log("Elapsed Times", "  RTimer starting: " + (l2 - l1), "  Deserialization " + (l3 - l2), "  Activating JNativeHook: " + (l4 - l3), "  Preferences initializing " + (l5 - l4),
                "  Window initializing: " + (l6 - l5));
    }

    public static void exit() {
        if (window != null && !exited) { // exit()がhide()により、2回呼ばれてしまう為、対策する
            exited = true;
            Debug.log(APP_NAME + " is exited");

            window.hide(); // 直ぐSystem.exit(0)しても、処理に時間が掛かる為、素早く処理されているように見せかける
            SaveData.serialize(defaultSerializeFileName);
            System.exit(0);
        }
    }
}