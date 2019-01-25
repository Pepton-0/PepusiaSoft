package clerk.pepusiasoft.nodes;

import clerk.pepusiasoft.language.Lang;
import clerk.pepusiasoft.App;
import clerk.pepusiasoft.savedata.SaveData;
import clerk.pepusiasoft.stages.Preferences;
import clerk.pepusiasoft.timersystem.RunScheduler;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;

import java.awt.*;

public class Menubar extends BorderPane {

    private boolean isLocked = false;
    private boolean ableToTransparent = false;
    private boolean isUnfocused = false;
    private final Button lockWindow;
    private final Button opacityChanger;
    private boolean mouseIsOnWindow = false;

    public Menubar() {
        setStyle("-fx-background-color: gray;");
        HBox leftGroup = new HBox(3);
        HBox rightGroup = new HBox(3);
        leftGroup.setAlignment(Pos.CENTER_LEFT);
        rightGroup.setAlignment(Pos.CENTER_RIGHT);
        leftGroup.setPadding(new Insets(5, 3, 5, 3));
        rightGroup.setPadding(new Insets(5, 3, 5, 3));
        setPadding(new Insets(0, 0, 0, 20));

        lockWindow = new Button();
        opacityChanger = new Button();
        Button moveToSide = new Button(Lang.of("move window"));
        Button settingMenu = new Button(Lang.of("setting menu"));
        Button hide = new Button("_");
        Button quit = new Button("×");
        lockWindow.setTooltip(new Tooltip(Lang.of("lock window tooltip")));
        opacityChanger.setTooltip(new Tooltip(Lang.of("opacity changer tooltip")));
        moveToSide.setTooltip(new Tooltip(Lang.of("move window tooltip")));
        settingMenu.setTooltip(new Tooltip(Lang.of("not implemented")));
        hide.setTooltip(new Tooltip(Lang.of("hide tooltip")));
        quit.setTooltip(new Tooltip(Lang.of("quit tooltip")));

        settingMenu.setStyle("-fx-text-fill: green;");
        hide.setStyle("-fx-text-fill: blue;");
        quit.setStyle("-fx-text-fill: red;");

        moveToSide.setOnAction(t -> App.window.moveToSide());
        lockWindow.setOnAction(t -> setWindowLocked(!isLocked));
        opacityChanger.setOnAction(t -> setAbleToTransparent(!ableToTransparent));
        settingMenu.setOnAction(event -> {
            if(App.preferences.isIconified())
                App.preferences.setIconified(false);
            if (App.preferences.isShowing())
                App.preferences.hide();
            App.preferences.show();
        });
        settingMenu.setOnMouseEntered(t -> settingMenu.setStyle("-fx-text-fill: black; -fx-background-color: #00FF00;"));
        settingMenu.setOnMouseExited(t -> settingMenu.setStyle("-fx-text-fill: green;"));
        quit.setOnAction(t -> App.exit());
        quit.setOnMouseEntered(t -> quit.setStyle("-fx-text-fill: black; -fx-background-color: red;"));
        quit.setOnMouseExited(t -> quit.setStyle("-fx-text-fill: red;"));
        hide.setOnAction(t -> App.window.setIconified(true));
        hide.setOnMouseEntered(t -> hide.setStyle("-fx-text-fill: black; -fx-background-color: blue;"));
        hide.setOnMouseExited(t -> hide.setStyle("-fx-text-fill: blue;"));

        Platform.runLater(() -> {
            App.window.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!ableToTransparent)
                    return;

                if (oldValue && !newValue) { // unfocused
                    isUnfocused = true;
                    App.window.setOpacity(0.04d);
                } else if (!oldValue && newValue) { // focused
                    isUnfocused = false;
                    App.window.setOpacity(1d);
                }
            });
            RunScheduler.addRepeatable(0.07d, () -> {
                if (!isUnfocused && ableToTransparent) {
                    Point mousePos = MouseInfo.getPointerInfo().getLocation();
                    Point windowPos = new Point((int) (App.window.getX() + App.window.getWidth() / 2), (int) (App.window.getY() + App.window.getHeight() / 2));
                    Rectangle2D rect = Screen.getPrimary().getVisualBounds();

                    double distance = windowPos.distanceSq(mousePos);
                    double maxDistance = rect.getHeight() * rect.getHeight() + rect.getWidth() * rect.getWidth();

                    Platform.runLater(() -> App.window.setOpacity(Math.max(0.04d, 1d - Math.min(distance / maxDistance, 1d) * 2.3d)));
                } else if (isUnfocused && ableToTransparent) {
                    Point mousePos = MouseInfo.getPointerInfo().getLocation();
                    Rectangle2D rect = new Rectangle2D(App.window.getX(), App.window.getY(), App.window.getWidth(), App.window.getHeight());
                    boolean mouseIsOnWindow = rect.contains(mousePos.x, mousePos.y);
                    if (!this.mouseIsOnWindow && mouseIsOnWindow) {
                        Platform.runLater(() -> App.window.setOpacity(1d));
                    } else if (this.mouseIsOnWindow && !mouseIsOnWindow && isUnfocused) {
                        Platform.runLater(() -> App.window.setOpacity(0.04d));
                    }
                    this.mouseIsOnWindow = mouseIsOnWindow;
                }
            });
        });

        leftGroup.getChildren().addAll(lockWindow, opacityChanger, moveToSide);
        rightGroup.getChildren().addAll(settingMenu, hide, quit);
        setLeft(leftGroup);
        setRight(rightGroup);

        /* save data */
        Boolean locked = SaveData.get("window.menuBar.locked", false);
        Boolean autoTransparency = SaveData.get("window.menuBar.autoTransparency", false);
        Platform.runLater(() -> setWindowLocked(locked)); // 直ぐに適応することは出来ないので、遅れてする
        setAbleToTransparent(autoTransparency);

        SaveData.addSavingTask(() -> SaveData.set("window.menuBar.locked", isLocked));
    }

    private void setWindowLocked(boolean lock) {
        isLocked = lock;
        lockWindow.setText(Lang.of("lock window") + (lock ? '■' : '□'));
        if (App.window != null) {
            App.window.setAlwaysOnTop(lock);
            App.preferences.setAlwaysOnTop(lock);
        }
    }

    private void setAbleToTransparent(boolean ableToTransparent) {
        this.ableToTransparent = ableToTransparent;
        opacityChanger.setText(Lang.of("opacity changer") + (ableToTransparent ? '■' : '□'));

        if (ableToTransparent == false) {
            Platform.runLater(() -> {
                if (App.window != null) App.window.setOpacity(1.0d);
            });
        }
    }
}