package clerk.pepusiasoft.utils;

import javafx.scene.Node;
import javafx.scene.transform.Scale;

public class Zoomable{

    private final Node target;
    private Scale lastScale;

    public Zoomable(Node target) {
        this.target = target;
        lastScale = new Scale(1, 1, 0, 0);
    }

    public void zoom(double amount) {
        lastScale.setX(Math.max(lastScale.getX() + amount, 1));
        lastScale.setY(Math.max(lastScale.getY() + amount, 1));
        lastScale = lastScale.clone();

        target.getTransforms().add(lastScale);
    }
}
