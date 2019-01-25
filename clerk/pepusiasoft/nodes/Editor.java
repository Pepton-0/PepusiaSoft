package clerk.pepusiasoft.nodes;

import clerk.pepusiasoft.language.Lang;
import clerk.pepusiasoft.App;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class Editor extends BorderPane {

    public Editor() {
        setStyle("-fx-background-color: gray;");

        HBox leftGroup = new HBox(3); // creating new tabs
        HBox rightGroup = new HBox(3); // about showing tabs
        leftGroup.setAlignment(Pos.CENTER_LEFT);
        rightGroup.setAlignment(Pos.CENTER_RIGHT);;
        leftGroup.setPadding(new Insets(5, 3, 5, 3));
        rightGroup.setPadding(new Insets(5, 3, 5, 3));

        Button appendNote = new Button(Lang.of("add note"));
        Button appendWeb = new Button(Lang.of("add web"));
        //Button edit = new Button(Lang.of("edit this"));
        appendNote.setTooltip(new Tooltip(Lang.of("add note tooltip")));
        appendWeb.setTooltip(new Tooltip(Lang.of("add web tooltip")));

        appendNote.setOnAction(t -> App.window.contents.addNoteEditor());
        appendWeb.setOnAction(t-> App.window.contents.addWebBrowser(true));
        appendWeb.setOnDragOver(event -> {
            Dragboard board = event.getDragboard();
            if(board.hasString() || board.hasHtml() || board.hasUrl())
                event.acceptTransferModes(TransferMode.COPY);
        });
        appendWeb.setOnDragDropped(event -> {
            Dragboard board = event.getDragboard();
            if (board.hasString()) {
                WebBrowser.acceptDragboardByNewWeb(board, DraggedDataType.String);
                event.setDropCompleted(true);
            } else if (board.hasHtml()) {
                WebBrowser.acceptDragboardByNewWeb(board, DraggedDataType.Html);
                event.setDropCompleted(true);
            } else if (board.hasUrl()) {
                WebBrowser.acceptDragboardByNewWeb(board, DraggedDataType.Url);
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
        });

        leftGroup.getChildren().addAll(appendNote, appendWeb);

        setLeft(leftGroup);
        setRight(rightGroup);
    }
}