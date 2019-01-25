package clerk.pepusiasoft.stages;

import borderless.BorderlessScene;
import clerk.pepusiasoft.*;
import clerk.pepusiasoft.nodes.Contents;
import clerk.pepusiasoft.nodes.Editor;
import clerk.pepusiasoft.nodes.Menubar;
import clerk.pepusiasoft.nodes.WebBrowser;
import clerk.pepusiasoft.savedata.SaveData;
import clerk.pepusiasoft.timersystem.RunScheduler;
import clerk.pepusiasoft.utils.ColorHelper;
import clerk.pepusiasoft.utils.NativeAccessor;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

public class Window extends Stage {

    public final App app;
    public final Menubar menubar;
    public final Contents contents;
    public final Editor editor;

    private ArrayList<ChangeFocusOwnerEventListener> changeFocusOwnerEventListeners = new ArrayList<>();

    public Window(App app) {

        Debug.log("Window started initializing");
        this.app = app;

        /* title and base setting */
        long l1 = System.currentTimeMillis();
        setTitle(App.APP_NAME + "\"" + HelloMessenger.getRandom() + "\"");
        getIcons().add(ResourceLoader.getFXImage("database/icon.png"));
        long l2 = System.currentTimeMillis();

        /* size and location setting */
        // Vec2d stageSize = SaveData.get("window.size");
        Double width = SaveData.get("window.size.width", 0d);
        Double height = SaveData.get("window.size.height", 0d);
        Rectangle2D desktopBounds = Screen.getPrimary().getVisualBounds();
        double initWidth = desktopBounds.getWidth() / 4.2;
        double initHeight = desktopBounds.getHeight() / 2.3;

        // 小さすぎたら、初期化
        if (width <= 10)
            width = initWidth;
        if (height <= 10)
            height = initHeight;

        setWidth(width);
        setHeight(height);
        setMinWidth(initWidth);
        setMinHeight(initHeight);
        long l3 = System.currentTimeMillis();

        /* node settings */
        BorderPane baseLayout = new BorderPane();
        BorderlessScene baseScene = new BorderlessScene(this, baseLayout, true);
        show();
        long l4 = System.currentTimeMillis();

        Debug.log("     finished frame settings");

        // どうやらBorderlessSceneのmove機能がpaddingに対応してなくて、マウスがmove時に少し移動してしまう
        baseLayout.setPadding(new Insets(1, 1, 1, 1));
        baseLayout.setStyle("-fx-background-color: " + ColorHelper.toHex(Preferences.FOCUSED_COLOR) + ";");

        menubar = new Menubar();
        baseLayout.setTop(menubar);

        contents = new Contents();
        baseLayout.setCenter(contents);

        editor = new Editor();
        baseLayout.setBottom(editor);

        /* set event listeners */
        showingProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                App.exit();
            }
        });

        baseScene.focusOwnerProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue) {
                ArrayList<ChangeFocusOwnerEventListener> removeTemp = new ArrayList<>();

                for (ChangeFocusOwnerEventListener p : changeFocusOwnerEventListeners) {
                    if (!removeTemp.contains(p)) {
                        boolean unused = p.changeFocusOwner(newValue == p);
                        if (unused) {
                            removeTemp.add(p);
                            Debug.log("Remove a listener");
                        }
                    }
                }
                changeFocusOwnerEventListeners.removeAll(removeTemp);
            }
        });

        // when unfocused, save data
        // when changed focus, change window color
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue)
                SaveData.serialize(App.defaultSerializeFileName);

            if (oldValue != newValue) {
                baseLayout.setStyle("-fx-background-color: " + (newValue ? ColorHelper.toHex(Preferences.FOCUSED_COLOR) : "black") + ";");
            }
        });

        // native key listener
        NativeAccessor.addKeyPressedListener((state, event) -> {
            Platform.runLater(() -> {
                // TODO 後々、キー入力設定もMenubarにある⚙から変えられるようにしよう
                // Alt + 1 = Hide/Show
                // Alt + 2 = Show and focus
                // Alt + 3 = Search the text or image on clipboard on the Internet and show the result on WebBrowser

                if(App.preferences.isFocused()) // 設定中に(特にキー設定中に)操作されたりしたらこまるので
                    return;

                // Hide/Show/Focus
                //if (state.isAltPressed() && event.getKeyCode() == NativeKeyEvent.VC_1) {
                if(NativeAccessor.areKeysDown(Preferences.SHOW_HIDE_KEYS)) {
                    if (isIconified()) {
                        // Show
                        setIconified(false);
                    } else {
                        // Hide
                        setIconified(true);
                    }
                }
                // Show and focus
                //else if (state.isAltPressed() && event.getKeyCode() == NativeKeyEvent.VC_2) {
                else if(NativeAccessor.areKeysDown(Preferences.SHOW_FOCUS_KEYS)){
                    showAndFocus();
                    Debug.log("Focus");
                }
                // Search the text on clipboard
                //else if (state.isAltPressed() && event.getKeyCode() == NativeKeyEvent.VC_3) {
                else if(NativeAccessor.areKeysDown(Preferences.SEARCH_COPIED_TEXT_KEYS)) {
                    showAndFocus();
                    String str = "";

                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                        try {
                            str = (String) clipboard.getData(DataFlavor.stringFlavor);
                        } catch (UnsupportedFlavorException | IOException e) {
                            Debug.log("Failed to get string flavor's data");
                        }
                    }

                    // Search information acquired from Clipboard
                    if (str != null && str.length() >= 1) {
                        Debug.log("str was " + str);
                        WebBrowser browser = contents.addWebBrowser(false);
                        WebBrowser.acceptString(str, browser);

                        Debug.log("Search the copied text");
                    }
                }
            });
        });

        /* end of initializing*/
        baseScene.setMoveControl(baseLayout);
        // sizeToScene();
        moveToSide(); // desktopBoundsをもう一度取得するという無駄が……
        setScene(baseScene);
        long l5 = System.currentTimeMillis();
        show();
        long l6 = System.currentTimeMillis();

        // SaveData.addSavingTask(() -> SaveData.set("window.size", new Vec2d(getWidth(), getHeight())));
        SaveData.addSavingTask(() -> {
            SaveData.set("window.size.width", getWidth());
            SaveData.set("window.size.height", getHeight());
        });

        Debug.log("Window finished initializing");
        Debug.log("Elapsed Times to Initialize Window:", "  Title and Icon " + (l2 - l1), "  Size and Location " + (l3 - l2),
                "  Instancing BorderlessPane " + (l4 - l3), "  Components " + (l5 - l4), "  Showing " + (l6 - l5));
    }

    public void moveToSide() {
        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        setX(screenSize.getWidth() - getWidth() - 5);
        setY(screenSize.getHeight() - getHeight() - 5);
    }

    public void addChangeFocusOwnerEventListener(ChangeFocusOwnerEventListener listener) {
        changeFocusOwnerEventListeners.add(listener);
    }

    public void showAndFocus() {
        if(!isFocused()) {
            if(!isShowing())
                show();
            setIconified(true);
            setIconified(false);
        }
    }
}