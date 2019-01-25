package clerk.pepusiasoft;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ResourceLoader {

    @Deprecated
    public static URL at(String location){
        return ClassLoader.getSystemClassLoader().getResource(location);
    }

    public static InputStream get(String location) {
        return ClassLoader.getSystemResourceAsStream(location);
    }

    public static Image getImage(String location) {
        try {
            return ImageIO.read(new BufferedInputStream(get(location)));
        } catch (IOException e) {
            Debug.log("Failed to load an image from " + location + ".");
            e.printStackTrace();
        }

        return null;
    }

    public static javafx.scene.image.Image getFXImage(String location) {
        return new javafx.scene.image.Image(get(location));
    }
}
