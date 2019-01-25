package clerk.pepusiasoft.language;

import clerk.pepusiasoft.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class LanguageData {

    private final HashMap<String, String> map = new HashMap<>();

    public String of(String name) {
        StringBuilder builder = new StringBuilder(map.getOrDefault(name, ":" + name + ":"));
        return builder.toString();
    }

    public boolean initialize(String country) {
        map.clear();
        /*
        URL data = ResourceLoader.at("database/language/Lang_" + country);
        if (data == null || data.getFile().isEmpty())
            return false; // Failed to load

        File file = new File(data.getFile());
        */
        InputStream stream = ResourceLoader.get("database/language/Lang_" + country);

        try {
            //FileReader reader = new FileReader(file);
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            StringBuilder content = new StringBuilder();

            String name = "", matter = "";
            int i;
            while ((i = reader.read()) != -1) {
                char c = (char) i;

                if (c == '=') {
                    name = content.toString();
                    content.setLength(0);
                    continue;
                }
                if (c == '\n') {
                    matter = content.toString();
                    content.setLength(0);

                    if(matter.lastIndexOf(13) != -1) { // 13という、改行コードでもない特別なコードがWindows用にあるらしい
                        matter = matter.substring(0, matter.length() - 1);
                    }

                    if (!name.isEmpty() && !matter.isEmpty())
                        map.put(name, matter);

                    name = "";
                    matter = "";
                    continue;
                }

                content.append(c);
            }
            matter = content.toString();
            if (!name.isEmpty() && !matter.isEmpty())
                map.put(name, matter);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}