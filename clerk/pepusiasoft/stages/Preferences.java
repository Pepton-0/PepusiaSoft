package clerk.pepusiasoft.stages;

import borderless.BorderlessScene;
import clerk.pepusiasoft.App;
import clerk.pepusiasoft.Debug;
import clerk.pepusiasoft.ResourceLoader;
import clerk.pepusiasoft.language.Lang;
import clerk.pepusiasoft.nodes.ConfigItem;
import clerk.pepusiasoft.nodes.Configurator;
import clerk.pepusiasoft.nodes.KeyAcceptor;
import clerk.pepusiasoft.savedata.SaveData;
import clerk.pepusiasoft.timersystem.RunScheduler;
import clerk.pepusiasoft.utils.ColorHelper;
import clerk.pepusiasoft.utils.NativeAccessor;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.NumberStringConverter;
import org.jnativehook.keyboard.NativeKeyEvent;

import java.util.ArrayList;
import java.util.Arrays;

// 色んなコンフィグレーションが出来る
public class Preferences extends Stage {

    // コンフィグ一覧
    public static Color FOCUSED_COLOR; // ウインドウが選択された時の縁の色
    public static ArrayList<NativeAccessor.KeyCode> SHOW_HIDE_KEYS; // ウインドウの表示切り替えのショートカットキー
    public static ArrayList<NativeAccessor.KeyCode> SHOW_FOCUS_KEYS; // ウインドウがフォーカスされる
    public static ArrayList<NativeAccessor.KeyCode> SEARCH_COPIED_TEXT_KEYS; // クリップボード上のテキストをウェブで検索する
    public static int MAX_TAB_NAME_LENGTH;
    public static int SAVE_INTERVAL; // cannnot be under 5s


    private TextField searchField; // when search, reference its text, found a searched thing, show the result in split pane's right area.
    private Button onlyApply; // become clickable when something changed
    private Button applyAndExit; // become clickable when something changed
    private Configurator configurator;

    public Preferences() {

        configurator = new Configurator(this);
        initConfigs();

        // title and basic settings
        setTitle(Lang.of("preferences"));
        initStyle(StageStyle.UNDECORATED);
        getIcons().add(ResourceLoader.getFXImage("database/icon.png"));
        Platform.runLater(() -> setAlwaysOnTop(App.window.isAlwaysOnTop()));
        setWidth(600d);
        setHeight(350d);
        setMinWidth(400d);
        setMinHeight(150d);
        setMaxWidth(750d);
        setMaxHeight(350d);
        setResizable(false);

        // node settings
        //    scene, base layout
        BorderPane baseLayout = new BorderPane();
        BorderlessScene scene = new BorderlessScene(this, baseLayout, false);
        // TODO ちゃんとSplitPane(Configurator)が縮小してくれるようにする為のコードだけど、何故か不要になった？
        //baseLayout.setPrefSize(0d, 0d);
        //baseLayout.setMinSize(0d, 0d);
        baseLayout.setPadding(new Insets(1, 1, 1, 1));
        baseLayout.setStyle("-fx-background-color: " + ColorHelper.toHex(FOCUSED_COLOR) + ";");

        //    menu bar
        BorderPane menuBar = new BorderPane();
        menuBar.setPadding(new Insets(0, 0, 0, 20));
        menuBar.setStyle("-fx-background-color: gray;");
        {
            HBox searchingTools = new HBox();
            searchingTools.setPadding(new Insets(5, 3, 5, 3));
            searchingTools.setAlignment(Pos.CENTER_LEFT);
            {
                searchField = new TextField();
                Button doSearch = new Button(Lang.of("preferences.search"));

                searchField.setPromptText(Lang.of("preferences.search prompt"));
                doSearch.setTooltip(new Tooltip(Lang.of("preferences.search tooltip")));

                searchField.setOnKeyReleased(event -> {
                    if (event.getCode() == KeyCode.ENTER)
                        configurator.showCorrespondedConfigs(searchField.getText());
                });
                doSearch.setOnAction(event -> configurator.showCorrespondedConfigs(searchField.getText()));

                searchingTools.getChildren().addAll(searchField, doSearch);
            }

            HBox windowTools = new HBox();
            windowTools.setPadding(new Insets(5, 3, 5, 3));
            windowTools.setSpacing(3d);
            windowTools.setAlignment(Pos.CENTER_RIGHT);
            {
                Button exit = new Button("×");

                exit.setTooltip(new Tooltip(Lang.of("preferences.exit tooltip")));
                exit.setStyle("-fx-text-fill: red;");

                exit.setOnAction(event -> App.preferences.hide());
                exit.setOnMouseEntered(t -> exit.setStyle("-fx-text-fill: black; -fx-background-color: red;"));
                exit.setOnMouseExited(t -> exit.setStyle("-fx-text-fill: red;"));

                windowTools.getChildren().add(exit);
            }

            menuBar.setLeft(searchingTools);
            menuBar.setRight(windowTools);
        }

        //    configuration area
        // configurator = new Configurator();

        //    applier
        HBox applyingTools = new HBox();
        applyingTools.setPadding(new Insets(5, 3, 5, 3));
        applyingTools.setAlignment(Pos.CENTER_RIGHT);
        applyingTools.setSpacing(3d);
        applyingTools.setStyle("-fx-background-color: gray;");
        {
            onlyApply = new Button(Lang.of("preferences.onlyApply"));
            applyAndExit = new Button(Lang.of("preferences.applyAndExit"));
            Button cancel = new Button(Lang.of("preferences.cancel"));

            onlyApply.setTooltip(new Tooltip(Lang.of("preferences.onlyApply tooltip")));
            applyAndExit.setTooltip(new Tooltip(Lang.of("preferences.applyAndExit tooltip")));
            cancel.setTooltip(new Tooltip(Lang.of("preferences.cancel tooltip")));

            onlyApply.setOnAction(event -> configurator.applyChanges());
            applyAndExit.setOnAction(event -> {
                configurator.applyChanges();
                hide();
            });
            cancel.setOnAction(event -> {
                hide();
            });

            applyingTools.getChildren().addAll(onlyApply, applyAndExit, cancel);
            changeApplyingButtonStates(false);
        }

        baseLayout.setTop(menuBar);
        baseLayout.setCenter(configurator);
        baseLayout.setBottom(applyingTools);

        // set event listeners of scene and baseLayout
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue) {
                baseLayout.setStyle("-fx-background-color: " + (newValue ? ColorHelper.toHex(FOCUSED_COLOR) : "black") + ";");
            }
        });

        scene.setMoveControl(menuBar);
        setScene(scene);
    }

    public void changeApplyingButtonStates(boolean changed) {
        if (changed) {
            if (onlyApply.isDisable())
                onlyApply.setDisable(false);
            if (applyAndExit.isDisable())
                applyAndExit.setDisable(false);
        } else {
            if (!onlyApply.isDisable())
                onlyApply.setDisable(true);
            if (!applyAndExit.isDisable())
                applyAndExit.setDisable(true);
        }
    }

    private void initConfigs() {
        // Stages' frame color
        configurator.addConfig(new ConfigItem<>("window.design", "focused color", new ColorPicker(), picker -> {
            picker.setValue(Color.valueOf(SaveData.get("preferences.window.design.focused color", Color.BLUE.toString())));
            FOCUSED_COLOR = picker.getValue();
        }, picker -> {
            SaveData.set("preferences.window.design.focused color", picker.getValue().toString());
            FOCUSED_COLOR = picker.getValue();
        }));

        // max tab name length of web browser
        configurator.addConfig(new ConfigItem<>("window.design", "max tab name length", new TextField(), field -> {
            TextFormatter<Number> numberFormatter = new TextFormatter<>
                    (new NumberStringConverter(Lang.getLocale()),
                            SaveData.get("preferences.window.design.max tab name length", 80d), change -> change);
            field.setTextFormatter(numberFormatter);
            MAX_TAB_NAME_LENGTH = numberFormatter.getValue().intValue();
        }, field -> {
            MAX_TAB_NAME_LENGTH = ((Number) field.getTextFormatter().getValue()).intValue();
            SaveData.set("preferences.window.design.max tab name length", MAX_TAB_NAME_LENGTH);

            Debug.log("Value is " + field.getText());
        }));

        // Key short cut
        configurator.addConfig(new ConfigItem<>("window.short cut", "show/hide", new KeyAcceptor(), field -> {
            field.reset(SaveData.get("preferences.window.short cut.show/hide",
                    new ArrayList<>(Arrays.asList(
                            new NativeAccessor.KeyCode(NativeKeyEvent.VC_ALT, NativeKeyEvent.KEY_LOCATION_LEFT),
                            new NativeAccessor.KeyCode(NativeKeyEvent.VC_1, NativeKeyEvent.KEY_LOCATION_STANDARD)))));
            SHOW_HIDE_KEYS = field.list;
        }, field -> {
            SaveData.set("preferences.window.short cut.show/hide", field.list);
            SHOW_HIDE_KEYS = field.list;
        }));

        configurator.addConfig(new ConfigItem<>("window.short cut", "show and focus", new KeyAcceptor(), field -> {
            field.reset(SaveData.get("preferences.window.short cut.show and focus",
                    new ArrayList<>(Arrays.asList(
                            new NativeAccessor.KeyCode(NativeKeyEvent.VC_ALT, NativeKeyEvent.KEY_LOCATION_LEFT),
                            new NativeAccessor.KeyCode(NativeKeyEvent.VC_2, NativeKeyEvent.KEY_LOCATION_STANDARD)))));
            SHOW_FOCUS_KEYS = field.list;
        }, field -> {
            SaveData.set("preferences.window.short cut.show and focus", field.list);
            SHOW_FOCUS_KEYS = field.list;
        }));

        configurator.addConfig(new ConfigItem<>("window.short cut", "search copied text", new KeyAcceptor(), field -> {
            field.reset(SaveData.get("preferences.window.short cut.search copied text",
                    new ArrayList<>(Arrays.asList(
                            new NativeAccessor.KeyCode(NativeKeyEvent.VC_ALT, NativeKeyEvent.KEY_LOCATION_LEFT),
                            new NativeAccessor.KeyCode(NativeKeyEvent.VC_3, NativeKeyEvent.KEY_LOCATION_STANDARD)))));
            SEARCH_COPIED_TEXT_KEYS = field.list;
        }, field -> {
            SaveData.set("preferences.window.short cut.search copied text", field.list);
            SEARCH_COPIED_TEXT_KEYS = field.list;
        }));

        configurator.addConfig(new ConfigItem<>("auto saver", "auto save interval", new TextField(), field ->{
            TextFormatter<Number> numberFormatter = new TextFormatter<>
                    (new javafx.util.converter.NumberStringConverter(Lang.getLocale()),
                            SaveData.get("preferences,auto saver.auto save interval", 60), change -> change);
            field.setTextFormatter(numberFormatter);
            SAVE_INTERVAL = numberFormatter.getValue().intValue();
        }, field -> {
            SAVE_INTERVAL = ((Number) field.getTextFormatter().getValue()).intValue();
            SaveData.set("preferences,auto saver.auto save interval", SAVE_INTERVAL);
        }));

        // periodic saving.
        class Saver {
            private void save() {
                SaveData.serialize(App.defaultSerializeFileName);
                RunScheduler.add(Math.max(SAVE_INTERVAL, 5d), this::save); // 短すぎる間隔にならないよう、5以上に制限する
            }
        }
        RunScheduler.add(Math.max(SAVE_INTERVAL, 5d), () -> new Saver().save()); // 短すぎる間隔にならないよう、5以上に制限する
    }
}
