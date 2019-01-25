package clerk.pepusiasoft.nodes;

import clerk.pepusiasoft.App;
import clerk.pepusiasoft.ChangeFocusOwnerEventListener;
import clerk.pepusiasoft.Debug;
import clerk.pepusiasoft.language.Lang;
import clerk.pepusiasoft.stages.Preferences;
import clerk.pepusiasoft.timersystem.RunScheduler;
import clerk.pepusiasoft.utils.StringHelper;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoteEditor extends HTMLEditor implements ChangeFocusOwnerEventListener {

    private static final Pattern tagPattern = Pattern.compile("<[^>]*>");
    private static final Pattern spacePattern = Pattern.compile("　| |[&bnbsp;]|\n");

    private final Contents contents;

    public NoteEditor(Contents contents) {
        this.contents = contents;

        // TODO exp4jを使って、自動計算機能を実装したい
        setOnKeyTyped(event -> {
            Debug.log("AAA");
            Node webViewNode = lookup(".web-view");
            if(webViewNode instanceof WebView){

                // 1. Get the line that contains the typed char
                WebEngine engine = ((WebView) webViewNode).getEngine();
                Integer index = (Integer) engine.executeScript(
                        "function executing() " +
                                "{" +
                                "if(this.selectionStart || this.selectionStart == 0){return this.selectionStart;} "+
                                "else {return 0;}" +
                                "}" +
                                "executing();");
                Debug.log("Input at: " + String.valueOf(index));
            }
        });


        Platform.runLater(() -> App.window.addChangeFocusOwnerEventListener(this));
    }

    public void resetTitile() {
        int maxLength = Preferences.MAX_TAB_NAME_LENGTH;
        boolean noContent = false;
        String title = getOnlyText();
        title = spacePattern.matcher(title).replaceAll("");
        Tab tab = null;
        for (Tab t : contents.getTabs()) if (t.getContent() == this) tab = t;

        if (title.length() <= 0) {
            noContent = true;
            title = Lang.of("no title");
        }

        // no title を既に設定している場合は、修正の必要はない
        if(!noContent){
            title = StringHelper.getLimitedString(title, maxLength, Lang.of("no title"));
        }

        if (tab != null)
            tab.setText(title);
    }

    private String getOnlyText() {
        final String htmlText = getHtmlText();

        final Matcher matcher = tagPattern.matcher(htmlText);
        final StringBuffer text = new StringBuffer(htmlText.length());

        while (matcher.find())
            matcher.appendReplacement(text, " ");

        matcher.appendTail(text);

        return text.toString().trim();
    }

    @Override
    public boolean changeFocusOwner(boolean focusedMe) {

        for (Tab t : contents.getTabs()) {
            if (t.getContent() == this) {
                RunScheduler.add(0.1d, () -> Platform.runLater(this::resetTitile));
                return false;
            }
        }

        return true;
    }
}