package clerk.pepusiasoft.nodes;

import clerk.pepusiasoft.App;
import clerk.pepusiasoft.ChangeFocusOwnerEventListener;
import clerk.pepusiasoft.Debug;
import clerk.pepusiasoft.language.Lang;
import clerk.pepusiasoft.stages.Preferences;
import clerk.pepusiasoft.timersystem.RunScheduler;
import clerk.pepusiasoft.utils.StringHelper;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebBrowser extends BorderPane implements ChangeFocusOwnerEventListener {

    private static Pattern urlPattern = Pattern.compile("^https?://");

    private final Contents contents;
    private final BorderPane controller;
    private final HBox controlButtons;
    private final TextField ct_url;
    private final Button ct_back;
    private final Button ct_forward;
    private final Button ct_reload;
    private final Button ct_search;
    private final WebView view;
    private final WebEngine engine;

    private boolean mouseIsOnThis = false;

    public WebBrowser(Contents contents) {
        this.contents = contents;
        controller = new BorderPane();
        controlButtons = new HBox(2);
        view = new WebView();
        engine = view.getEngine();
        ct_url = new TextField();
        ct_search = new Button(Lang.of("search"));
        ct_search.setTooltip(new Tooltip(Lang.of("search tooltip")));
        ct_back = new Button(Lang.of("page back"));
        ct_back.setTooltip(new Tooltip(Lang.of("page back tooltip")));
        ct_forward = new Button(Lang.of("page forward"));
        ct_forward.setTooltip(new Tooltip(Lang.of("page forward tooltip")));
        ct_reload = new Button(Lang.of("reload"));
        ct_reload.setTooltip(new Tooltip(Lang.of("reload tooltip")));

        ct_search.setOnAction(t -> acceptString(ct_url.getText(), this));
        ct_back.setOnAction(t -> engine.getHistory().go(-1));
        ct_forward.setOnAction(t -> engine.getHistory().go(1));
        ct_reload.setOnAction(t -> engine.reload());
        ct_url.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                acceptString(ct_url.getText(), this);
            }
        });
        engine.getLoadWorker().stateProperty().addListener(((observable, oldValue, newValue) -> {
            switch (newValue) {
                case SCHEDULED:
                    ct_url.setText(engine.getLocation());
                    for (Tab tab : contents.getTabs()) {
                        if (tab.getContent() == this) {
                            tab.setText(Lang.of("loading web"));
                            RunScheduler.add(2, () ->
                            {
                                String s = fixTitleNameForTabName(engine.getTitle());
                                if (!s.equals("null"))
                                    Platform.runLater(() -> tab.setText(s));
                            });
                            break;
                        }
                    }
                    break;
                case SUCCEEDED:
                    for (Tab tab : contents.getTabs()) {
                        if (tab.getContent() == this) {
                            tab.setText(fixTitleNameForTabName(engine.getTitle()));
                            break;
                        }
                    }
                    break;
                case FAILED:
                    for (Tab tab : contents.getTabs()) {
                        if (tab.getContent() == this) {
                            tab.setText(fixTitleNameForTabName(Lang.of("failed to search")));
                            break;
                        }
                    }
                    break;
            }

            // Change state of back and forward
            WebHistory history = engine.getHistory();
            ct_back.setDisable(history.getCurrentIndex() <= 0);
            ct_forward.setDisable(history.getCurrentIndex() >= history.getEntries().size() - 1);
        }));

        view.setOnMouseEntered(event -> mouseIsOnThis = true);
        view.setOnMouseMoved(event -> mouseIsOnThis = true);
        view.setOnMouseExited(event -> mouseIsOnThis = false);
        view.setOnScroll(event -> { // default zoom : 1.0
            if (event.isControlDown() && mouseIsOnThis) {
                double movement = 0;
                switch (event.getTextDeltaYUnits()) {
                    case LINES:
                        view.setZoom(fixedZoomingScale(view.getZoom() +(movement= event.getTextDeltaY() * 0.05)));
                        // scroll about event.getTextDeltaY() lines
                        break;
                    case PAGES:
                        // scroll about event.getTextDeltaY() pages
                        view.setZoom(fixedZoomingScale(view.getZoom() + (movement=event.getTextDeltaY() * 0.05)));
                        break;
                    case NONE:
                        view.setZoom(fixedZoomingScale(view.getZoom() + (movement=event.getDeltaY() * 0.05)));
                        // scroll about event.getDeltaY() pixels
                        break;
                }

                String script = "window.scrollTo(window.scrollX, window.scrollY+"+(movement/0.05)+")";
                Platform.runLater(()->engine.executeScript(script));
            }
        });

        controlButtons.getChildren().addAll(ct_search, ct_back, ct_forward, ct_reload);
        controller.setCenter(ct_url);
        controller.setRight(controlButtons);
        setTop(controller);
        setCenter(view);
    }

    public void load(String url) {
        engine.load(url);
    }

    public String getURL() {
        return engine.getLocation();
    }

    private String fixTitleNameForTabName(String title) {
        int maxLength = Preferences.MAX_TAB_NAME_LENGTH;

        title = StringHelper.getLimitedString(title, maxLength, Lang.of("no web title"));

        if (title.length() > maxLength) {
            title = title.substring(0, maxLength - 1) + "...";
        }

        return title;
    }

    private double fixedZoomingScale(double original) {
        double value;
        if (original > 3.0)
            value = 3.0;
        else if (original < -3.0)
            value = -2.0;
        else
            value = original;

        value = (double) ((int) (value * 10d)) / 10d;

        return value;
    }

    // 新たに対象キーワードを検索するWebタブを生成
    public static void acceptDragboardByNewWeb(Dragboard board, DraggedDataType type) {
        WebBrowser browser = App.window.contents.addWebBrowser(false);

        acceptDragboard(board, type, browser);
    }

    // 対象Webタブにドラッグされたキーワードを対応
    public static void acceptDragboard(Dragboard board, DraggedDataType type, WebBrowser browser) {
        if (type == DraggedDataType.String) {
            acceptString(board.getString(), browser);
        } else if (type == DraggedDataType.Html)
            browser.engine.loadContent(board.getHtml());
        else if (type == DraggedDataType.Url)
            browser.engine.load(board.getUrl());
    }

    public static void acceptString(String str, WebBrowser browser) {
        Matcher m = urlPattern.matcher(str);

        if (m.find()) { // is url
            browser.engine.load(str);
        } else { // isn't url, is keywords
            String url = StringHelper.ChromeUrl(str);
            browser.engine.load(url);
        }

        Debug.log("URL of text was " + browser.engine.getLocation());
    }

    @Override
    public boolean changeFocusOwner(boolean focusedMe) {
        return getParent() == null;
    }
}