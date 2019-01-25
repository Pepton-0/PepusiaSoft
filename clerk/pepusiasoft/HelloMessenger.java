package clerk.pepusiasoft;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HelloMessenger {

    public static Random random = new Random();

    public static List<String> read() {

        List<String> strings = new ArrayList<>();

        try {
            InputStreamReader reader = new InputStreamReader(ResourceLoader.get("database/hellomessages.txt"));

            StringBuffer buffer = new StringBuffer();

                /* reading */
            int i;
            while ((i = reader.read()) != -1) {
                char ch = (char) i;

                if (ch == '\n') {
                    strings.add(buffer.toString());
                    buffer.delete(0, buffer.length() - 1);
                } else {
                    buffer.append(ch);
                }
            }

            strings.add(buffer.toString()); // これをしないと、最後のメッセージが追加されない(改行が検出されないから)
        } catch (IOException e) {
            e.printStackTrace();
        }

        return strings;
    }

    public static String getRandom() {

        List<String> list = read();

        return list.get(random.nextInt(list.size() - 1));
    }
}