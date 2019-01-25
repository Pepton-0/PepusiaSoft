package clerk.pepusiasoft.nodes;

import clerk.pepusiasoft.App;
import clerk.pepusiasoft.Debug;
import clerk.pepusiasoft.language.Lang;
import clerk.pepusiasoft.savedata.ContentData;
import clerk.pepusiasoft.savedata.SaveData;
import clerk.pepusiasoft.utils.StringHelper;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class Contents extends TabPane {

    public Contents() {
        //design
        setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        setStyle("-fx-background-color: #e6e6e6;");

        // restore data
        Collection<ContentData> collection = SaveData.get("contents.list", new ArrayList<>());
        int selected = SaveData.get("contents.selected", -1);
        boolean failedToRestore = false;

        addTabAdder();
        if (collection.size() <= 0) {
            addNoteEditor().requestFocus();
        } else {
            for (ContentData data : collection) {
                try {
                    String name = data.getString("contentName");

                    if (NoteEditor.class.getSimpleName().equals(name)) {
                        NoteEditor note = addNoteEditor();
                        note.setHtmlText(data.getString("text"));
                        note.resetTitile();
                    } else if (WebBrowser.class.getSimpleName().equals(name))
                        addWebBrowser(true).load(data.getString("url"));
                    else {
                        getTabs().add(new Tab("***********" + (getTabs().size() - 1), new TextArea("Not found")));
                    }
                } catch(Exception e) {
                    failedToRestore = true;
                    Debug.log("Failed to load a tab.");
                }
            }
        }

        // 一度復元に失敗した場合、selectedに対応するタブが亡くなっている可能性がある為、selectedのセレクトをやらない
        if (!failedToRestore &&selected != -1) {
            if (selected < getTabs().size() - 1) { // タブ追加タブではない時
                getSelectionModel().select(selected);
                getTabs().get(selected).getContent().requestFocus(); // 上手くいかない。メニューバーの方がセレクトされてしまう
            } else if (getTabs().size() >= 2) {
                getSelectionModel().select(getTabs().size() - 2);
                getTabs().get(getTabs().size() - 2).getContent().requestFocus(); // 上記同様、上手くいかないかも
            }
        } else {
            getTabs().get(getSelectionModel().getSelectedIndex()).getContent().requestFocus();
        }

        SaveData.addSavingTask(() -> {
            ArrayList<ContentData> list = new ArrayList<>();

            for (Tab tab : getTabs()) {
                ContentData data = new ContentData();
                Node content = tab.getContent();
                if (content == null)
                    continue; // タブ追加ボタンだった時、キャンセル
                String contentName = content.getClass().getSimpleName();

                if (contentName.equals(NoteEditor.class.getSimpleName())) {
                    data.set("text", ((NoteEditor) content).getHtmlText());
                } else if (contentName.equals(WebBrowser.class.getSimpleName())) {
                    data.set("url", ((WebBrowser) content).getURL());
                }

                data.set("contentName", contentName);
                list.add(data);
            }

            SaveData.set("contents.list", list);
            SaveData.set("contents.selected", getSelectionModel().getSelectedIndex());
        });
    }

    public NoteEditor addNoteEditor() {
        NoteEditor editor = createNoteEditor();
        Tab tab = new Tab(Lang.of("new note") + " " + (getTabs().size()), editor);
        tab.setStyle("-fx-background-color: white;");
        WebView webView = (WebView) editor.lookup("WebView");
        GridPane.setHgrow(webView, Priority.ALWAYS);
        GridPane.setVgrow(webView, Priority.ALWAYS);

        getTabs().add(getTabs().size() - 1, tab);
        SelectionModel<Tab> selection = getSelectionModel();
        selection.select(tab);

        return editor;
    }

    public WebBrowser addWebBrowser(boolean loadGoogle) {
        WebBrowser browser = createWebBrowser(loadGoogle);
        Tab tab = new Tab(Lang.of("new web") + " " + (getTabs().size()), browser);

        getTabs().add(getTabs().size() - 1, tab);
        SelectionModel<Tab> selection = getSelectionModel();
        selection.select(tab);

        return browser;
    }

    private void addTabAdder() {
        Tab tab = new Tab("+");
        tab.setTooltip(new Tooltip(Lang.of("adder tooltip")));
        tab.setClosable(false);

        // Set a context menu
        ContextMenu ctm = new ContextMenu();
        MenuItem note = new MenuItem(Lang.of("add note"));
        MenuItem web = new MenuItem(Lang.of("add web"));

        note.setOnAction(event1 -> addNoteEditor());
        web.setOnAction(event1 -> addWebBrowser(true));

        ctm.getItems().addAll(note, web);
        tab.setContextMenu(ctm);

        tab.setOnSelectionChanged(event -> { // selectedIndexPropertyが同時に起きる事はない
            if(getTabs().size() <= 1) { // 他にタブが無くなった場合
                App.exit();
            }
        });

        getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue.intValue() != newValue.intValue() && newValue.intValue() == getTabs().indexOf(tab)) {
                getSelectionModel().select(oldValue.intValue());

                if(App.window != null && App.window.isFocused()) {
                    Point point = MouseInfo.getPointerInfo().getLocation();
                    tab.getContextMenu().show(App.window, point.x, point.y); // TODO キーショートカット時に、誤作動で表示されてしまうことがある
                }
            }
        });
        getTabs().add(tab);
    }

    private NoteEditor createNoteEditor() {
        return new NoteEditor(this);
    }

    private WebBrowser createWebBrowser(boolean loadGoogle) {
        WebBrowser browser = new WebBrowser(this);
        if (loadGoogle)
            browser.load(StringHelper.ChromeUrl());

        return browser;

    }
}