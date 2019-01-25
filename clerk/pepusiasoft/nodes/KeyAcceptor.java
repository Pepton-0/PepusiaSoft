package clerk.pepusiasoft.nodes;

import clerk.pepusiasoft.language.Lang;
import clerk.pepusiasoft.utils.NativeAccessor;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import org.jnativehook.keyboard.NativeKeyEvent;

import java.util.ArrayList;

public class KeyAcceptor extends TextField {

    public ArrayList<NativeAccessor.KeyCode> list = new ArrayList<>();

    public KeyAcceptor() {
        super();

        setPromptText(Lang.of("preferences.window.show/hide.prompt"));
        setFocusTraversable(false); // Tabキーの入力を可能にする為
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            event.consume(); // to cancel input
            reset();
        });
        addEventFilter(KeyEvent.KEY_TYPED, event ->{ // 何故か、KEY_PRESSEDだけだと文字入力がされてしまうので
            event.consume();
            reset();
        });
    }


    private void reset() {

        // make text field
        StringBuilder builder = new StringBuilder();
        ArrayList<NativeAccessor.KeyCode> list = new ArrayList<>();
        for (NativeAccessor.KeyCode model : NativeAccessor.getPressedKeyCodes()) {
            list.add(model);
            builder.append("|").append(NativeKeyEvent.getKeyText(model.keyCode));
        }

        String text;
        if (builder.indexOf("|") != -1)
            text = builder.substring(1);
        else
            text = builder.toString();

        setText(text);
        this.list = list;
    }

    public void reset(ArrayList<NativeAccessor.KeyCode> models){
        this.list = models;

        StringBuilder builder = new StringBuilder();

        for (NativeAccessor.KeyCode model : models) {
            builder.append("|").append(NativeKeyEvent.getKeyText(model.keyCode));
        }

        String text;
        if (builder.indexOf("|") != -1)
            text = builder.substring(1);
        else
            text = builder.toString();

        setText(text);
    }
}
