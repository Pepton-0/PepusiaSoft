package borderless;

import clerk.pepusiasoft.Debug;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Controller implements window controls: maximise, minimise, move, and Windows Aero Snap.
 *
 * @author Nicolas Senet-Larson
 * @version 1.0
 */
public class BorderlessController {
    private Stage primaryStage;
    Delta prevSize;
    Delta prevPos;
    AnchorPane root;
    boolean maximised;
    private boolean snapped;
    @FXML
    private Pane leftPane;
    @FXML
    private Pane rightPane;
    @FXML
    private Pane topPane;
    @FXML
    private Pane bottomPane;
    @FXML
    private Pane topLeftPane;
    @FXML
    private Pane topRightPane;
    @FXML
    private Pane bottomLeftPane;
    @FXML
    private Pane bottomRightPane;

    /**
     * The constructor.
     */
    public BorderlessController() {
        prevSize = new Delta();
        prevPos = new Delta();
        maximised = false;
        snapped = false;
    }

    public BorderlessController(Stage primaryStage) {
        this();

        leftPane = new Pane();
        rightPane = new Pane();
        topPane = new Pane();
        bottomPane = new Pane();
        topLeftPane = new Pane();
        topRightPane = new Pane();
        bottomLeftPane = new Pane();
        bottomRightPane = new Pane();

        Region region = new Region();

        topLeftPane.setOpacity(0.0);
        topLeftPane.prefHeight(5.0);
        topLeftPane.prefWidth(10.0);
        AnchorPane.setLeftAnchor(topLeftPane, 0.0);
        AnchorPane.setTopAnchor(topLeftPane, 0.0);
        topLeftPane.setCursor(Cursor.NW_RESIZE);

        topRightPane.setOpacity(0.0);
        topRightPane.prefHeight(5.0);
        topRightPane.prefWidth(10.0);
        AnchorPane.setRightAnchor(topRightPane, 0.0);
        AnchorPane.setTopAnchor(topRightPane, 0.0);
        topRightPane.setCursor(Cursor.NE_RESIZE);

        bottomRightPane.setOpacity(0.0);
        bottomRightPane.setOpacity(0.0);
        bottomRightPane.prefHeight(5.0);
        bottomRightPane.prefWidth(10.0);
        AnchorPane.setBottomAnchor(bottomRightPane, 0.0);
        AnchorPane.setRightAnchor(bottomRightPane, 0.0);
        bottomRightPane.setCursor(Cursor.SE_RESIZE);

        bottomLeftPane.setOpacity(0.0);
        bottomLeftPane.prefHeight(5.0);
        bottomLeftPane.prefWidth(10.0);
        AnchorPane.setBottomAnchor(bottomLeftPane, 0.0);
        AnchorPane.setLeftAnchor(bottomLeftPane,0.0);
        bottomLeftPane.setCursor(Cursor.SW_RESIZE);

        leftPane.setOpacity(0.0);
        leftPane.prefWidth(5.0);
        AnchorPane.setBottomAnchor(leftPane, 5.0);
        AnchorPane.setLeftAnchor(leftPane,0.0);
        AnchorPane.setTopAnchor(leftPane,5.0);
        leftPane.setCursor(Cursor.W_RESIZE);

        rightPane.setOpacity(0.0);
        rightPane.prefWidth(5.0);
        AnchorPane.setBottomAnchor(rightPane,5.0);
        AnchorPane.setRightAnchor(rightPane,0.0);
        AnchorPane.setTopAnchor(rightPane,5.0);
        rightPane.setCursor(Cursor.E_RESIZE);

        topPane.setOpacity(0.0);
        topPane.prefHeight(5.0);
        AnchorPane.setLeftAnchor(topPane, 10.0);
        AnchorPane.setRightAnchor(topPane,10.0);
        AnchorPane.setTopAnchor(topPane,0.0);
        topPane.setCursor(Cursor.N_RESIZE);

        bottomPane.setOpacity(0.0);
        bottomPane.prefHeight(5.0);
        AnchorPane.setBottomAnchor(bottomPane, 0.0);
        AnchorPane.setLeftAnchor(bottomPane, 10.0);
        AnchorPane.setRightAnchor(bottomPane,10.0);
        bottomPane.setCursor(Cursor.S_RESIZE);

        root = new AnchorPane(region, topLeftPane, topRightPane, bottomRightPane, bottomLeftPane, leftPane, rightPane, topPane, bottomPane);
        root.prefWidth(Region.USE_COMPUTED_SIZE);
        root.prefHeight(Region.USE_COMPUTED_SIZE);
        this.primaryStage=primaryStage;
        initialize();
    }

    /**
     * Called after the FXML layout is loaded.
     */
    @FXML
    private void initialize() {
        setResizeControl(leftPane, "left");
        setResizeControl(rightPane, "right");
        setResizeControl(topPane, "top");
        setResizeControl(bottomPane, "bottom");
        setResizeControl(topLeftPane, "top-left");
        setResizeControl(topRightPane, "top-right");
        setResizeControl(bottomLeftPane, "bottom-left");
        setResizeControl(bottomRightPane, "bottom-right");
    }

    /**
     * Reference to main application.
     *
     * @param primaryStage the main application stage.
     */
    protected void setMainApp(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Maximise on/off the application.
     */
    protected void maximise() {
        Rectangle2D screen;
        if (Screen.getScreensForRectangle(primaryStage.getX(), primaryStage.getY(), primaryStage.getWidth() / 2,
                primaryStage.getHeight() / 2).size() == 0) {
            screen = Screen.getScreensForRectangle(primaryStage.getX(), primaryStage.getY(),
                    primaryStage.getWidth(), primaryStage.getHeight()).get(0).getVisualBounds();
        } else {
            screen = Screen.getScreensForRectangle(primaryStage.getX(), primaryStage.getY(),
                    primaryStage.getWidth() / 2, primaryStage.getHeight() / 2).get(0).getVisualBounds();
        }

        if (maximised) {
            primaryStage.setWidth(prevSize.x);
            primaryStage.setHeight(prevSize.y);
            primaryStage.setX(prevPos.x);
            primaryStage.setY(prevPos.y);
            isMaximised(false);
        } else {
            // Record position and size, and maximise.
            if (!snapped) {
                prevSize.x = primaryStage.getWidth();
                prevSize.y = primaryStage.getHeight();
                prevPos.x = primaryStage.getX();
                prevPos.y = primaryStage.getY();
            } else if (!screen.contains(prevPos.x, prevPos.y)) {
                if (prevSize.x > screen.getWidth())
                    prevSize.x = screen.getWidth() - 20;

                if (prevSize.y > screen.getHeight())
                    prevSize.y = screen.getHeight() - 20;

                prevPos.x = screen.getMinX() + (screen.getWidth() - prevSize.x) / 2;
                prevPos.y = screen.getMinY() + (screen.getHeight() - prevSize.y) / 2;
            }

            primaryStage.setX(screen.getMinX());
            primaryStage.setY(screen.getMinY());
            primaryStage.setWidth(screen.getWidth());
            primaryStage.setHeight(screen.getHeight());

            isMaximised(true);
        }
    }

    /**
     * Minimise the application.
     */
    protected void minimise() {
        primaryStage.setIconified(true);
    }

    /**
     * Set a node that can be pressed and dragged to move the application around.
     *
     * @param node the node.
     */
    protected void setMoveControl(final Node node) {
        final Delta delta = new Delta();
        final Delta eventSource = new Delta();

        // Record drag deltas on press.
        node.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                delta.x = mouseEvent.getX();
                delta.y = mouseEvent.getY();

                if (maximised || snapped) {
                    delta.x = (prevSize.x * (mouseEvent.getX() / primaryStage.getWidth()));
                    delta.y = (prevSize.y * (mouseEvent.getY() / primaryStage.getHeight()));
                } else {
                    prevSize.x = primaryStage.getWidth();
                    prevSize.y = primaryStage.getHeight();
                    prevPos.x = primaryStage.getX();
                    prevPos.y = primaryStage.getY();
                }

                eventSource.x = mouseEvent.getScreenX();
                eventSource.y = node.prefHeight(primaryStage.getHeight());
            }
        });

        // Dragging moves the application around.
        node.setOnMouseDragged(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                // Move x axis.
                primaryStage.setX(mouseEvent.getScreenX() - delta.x);

                if (snapped) {
                    // Aero Snap off.
                    Rectangle2D screen = Screen.getScreensForRectangle(mouseEvent.getScreenX(),
                            mouseEvent.getScreenY(), 1, 1).get(0).getVisualBounds();

                    primaryStage.setHeight(screen.getHeight());

                    if (mouseEvent.getScreenY() > eventSource.y) {
                        primaryStage.setWidth(prevSize.x);
                        primaryStage.setHeight(prevSize.y);
                        snapped = false;
                    }
                } else {
                    // Move y axis.
                    primaryStage.setY(mouseEvent.getScreenY() - delta.y);
                }

                // Aero Snap off.
                if (maximised) {
                    primaryStage.setWidth(prevSize.x);
                    primaryStage.setHeight(prevSize.y);
                    isMaximised(false);
                }
            }
        });

        // Maximise on double click.
        node.setOnMouseClicked(mouseEvent -> {
            if ((mouseEvent.getButton().equals(MouseButton.PRIMARY)) && (mouseEvent.getClickCount() == 2)) {
                maximise();
            }
        });

        // Aero Snap on release.
        node.setOnMouseReleased(mouseEvent -> {
            if ((mouseEvent.getButton().equals(MouseButton.PRIMARY)) && (mouseEvent.getScreenX() != eventSource.x)) {
                Rectangle2D screen = Screen.getScreensForRectangle(mouseEvent.getScreenX(),
                        mouseEvent.getScreenY(), 1, 1).get(0).getVisualBounds();

                // Aero Snap Left.
                if (mouseEvent.getScreenX() == screen.getMinX()) {
                    primaryStage.setY(screen.getMinY());
                    primaryStage.setHeight(screen.getHeight());

                    primaryStage.setX(screen.getMinX());
                    if (screen.getWidth() / 2 < primaryStage.getMinWidth()) {
                        primaryStage.setWidth(primaryStage.getMinWidth());
                    } else {
                        primaryStage.setWidth(screen.getWidth() / 2);
                    }

                    snapped = true;
                }

                // Aero Snap Right.
                else if (mouseEvent.getScreenX() == screen.getMaxX() - 1) {
                    primaryStage.setY(screen.getMinY());
                    primaryStage.setHeight(screen.getHeight());

                    if (screen.getWidth() / 2 < primaryStage.getMinWidth()) {
                        primaryStage.setWidth(primaryStage.getMinWidth());
                    } else {
                        primaryStage.setWidth(screen.getWidth() / 2);
                    }
                    primaryStage.setX(screen.getMaxX() - primaryStage.getWidth());

                    snapped = true;
                }

                // Aero Snap Top.
                else if (mouseEvent.getScreenY() == screen.getMinY()) {
                    if (!screen.contains(prevPos.x, prevPos.y)) {
                        if (prevSize.x > screen.getWidth())
                            prevSize.x = screen.getWidth() - 20;

                        if (prevSize.y > screen.getHeight())
                            prevSize.y = screen.getHeight() - 20;

                        prevPos.x = screen.getMinX() + (screen.getWidth() - prevSize.x) / 2;
                        prevPos.y = screen.getMinY() + (screen.getHeight() - prevSize.y) / 2;
                    }

                    primaryStage.setX(screen.getMinX());
                    primaryStage.setY(screen.getMinY());
                    primaryStage.setWidth(screen.getWidth());
                    primaryStage.setHeight(screen.getHeight());
                    isMaximised(true);
                }
            }
        });
    }

    /**
     * Set pane to resize application when pressed and dragged.
     *
     * @param pane      the pane the action is set to.
     * @param direction the resize direction. Diagonal: 'top' or 'bottom' + 'right' or 'left'.
     */
    private void setResizeControl(Pane pane, final String direction) {
        pane.setOnMouseDragged(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                double width = primaryStage.getWidth();
                double height = primaryStage.getHeight();

                // Horizontal resize.
                if (direction.endsWith("left")) {
                    if ((width > primaryStage.getMinWidth()) || (mouseEvent.getX() < 0)) {
                        primaryStage.setWidth(width - mouseEvent.getScreenX() + primaryStage.getX());
                        primaryStage.setX(mouseEvent.getScreenX());
                    }
                } else if ((direction.endsWith("right"))
                        && ((width > primaryStage.getMinWidth()) || (mouseEvent.getX() > 0))) {
                    primaryStage.setWidth(width + mouseEvent.getX());
                }

                // Vertical resize.
                if (direction.startsWith("top")) {
                    if (snapped) {
                        primaryStage.setHeight(prevSize.y);
                        snapped = false;
                    } else if ((height > primaryStage.getMinHeight()) || (mouseEvent.getY() < 0)) {
                        primaryStage.setHeight(height - mouseEvent.getScreenY() + primaryStage.getY());
                        primaryStage.setY(mouseEvent.getScreenY());
                    }
                } else if (direction.startsWith("bottom")) {
                    if (snapped) {
                        primaryStage.setY(prevPos.y);
                        snapped = false;
                    } else if ((height > primaryStage.getMinHeight()) || (mouseEvent.getY() > 0)) {
                        primaryStage.setHeight(height + mouseEvent.getY());
                    }
                }
            }
        });

        // Record application height and y position.
        pane.setOnMousePressed(mouseEvent -> {
            if ((mouseEvent.isPrimaryButtonDown()) && (!snapped)) {
                prevSize.y = primaryStage.getHeight();
                prevPos.y = primaryStage.getY();
            }
        });

        // Aero Snap Resize.
        pane.setOnMouseReleased(mouseEvent -> {
            if ((mouseEvent.getButton().equals(MouseButton.PRIMARY)) && (!snapped)) {
                Rectangle2D screen = Screen.getScreensForRectangle(mouseEvent.getScreenX(),
                        mouseEvent.getScreenY(), 1, 1).get(0).getVisualBounds();

                if ((primaryStage.getY() <= screen.getMinY()) && (direction.startsWith("top"))) {
                    primaryStage.setHeight(screen.getHeight());
                    primaryStage.setY(screen.getMinY());
                    snapped = true;
                }

                if ((mouseEvent.getScreenY() >= screen.getMaxY()) && (direction.startsWith("bottom"))) {
                    primaryStage.setHeight(screen.getHeight());
                    primaryStage.setY(screen.getMinY());
                    snapped = true;
                }
            }
        });

        // Aero Snap resize on double click.
        pane.setOnMouseClicked(mouseEvent -> {
            if ((mouseEvent.getButton().equals(MouseButton.PRIMARY)) && (mouseEvent.getClickCount() == 2)
                    && ((direction.equals("top")) || (direction.equals("bottom")))) {
                Rectangle2D screen = Screen.getScreensForRectangle(primaryStage.getX(), primaryStage.getY(),
                        primaryStage.getWidth() / 2, primaryStage.getHeight() / 2).get(0).getVisualBounds();

                if (snapped) {
                    primaryStage.setHeight(prevSize.y);
                    primaryStage.setY(prevPos.y);
                    snapped = false;
                } else {
                    prevSize.y = primaryStage.getHeight();
                    prevPos.y = primaryStage.getY();
                    primaryStage.setHeight(screen.getHeight());
                    primaryStage.setY(screen.getMinY());
                    snapped = true;
                }
            }
        });
    }

    protected void isMaximised(Boolean maximised) {
        this.maximised = maximised;
        setResizable(!maximised);
    }

    protected void setResizable(Boolean bool) {
        leftPane.setDisable(!bool);
        rightPane.setDisable(!bool);
        topPane.setDisable(!bool);
        bottomPane.setDisable(!bool);
        topLeftPane.setDisable(!bool);
        topRightPane.setDisable(!bool);
        bottomLeftPane.setDisable(!bool);
        bottomRightPane.setDisable(!bool);
    }
}
