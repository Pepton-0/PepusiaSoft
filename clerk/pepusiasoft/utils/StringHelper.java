package clerk.pepusiasoft.utils;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.text.Text;

import java.io.*;

public class StringHelper {

    private static StringBuilder chromeQueryUrl = new StringBuilder(
            "{google:baseURL}" +
                    "search?q=%s&{google:RLZ}" +
                    "{google:originalQueryForSuggestion}" +
                    "{google:assistedQueryStats}" +
                    "{google:searchFieldtrialParameter}" +
                    "{google:iOSSearchLanguage}" +
                    "{google:searchClient}" +
                    "{google:sourceId}" +
                    "{google:contextualSearchVersion}" +
                    "ie={inputEncoding}");

    private static final FontMetrics defaultMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(new Text().getFont());

    public static String ChromeUrl() {
        return "http://www.google.com/";
        //return "https://www.google.co.jp/";
    }

    public static String ChromeUrl(String keywords) {

        try {

            StringBuilder builder = new StringBuilder(chromeQueryUrl);
            String systemEncoding = System.getProperty("file.encoding");
            byte[] bytes = keywords.getBytes(systemEncoding);
            String utf8keywords = new String(bytes, systemEncoding);
            String baseUrl = ChromeUrl();

            // convert to real keywords from keywords
            // ""でくくられたときのが無視される場合がある……
            if (!utf8keywords.startsWith("\"") && !utf8keywords.endsWith("\"")) {
                utf8keywords = utf8keywords.replace(' ', '+');
                utf8keywords = utf8keywords.replace('　', '+');
            }

            replaceOnce(builder, "{google:baseURL}", baseUrl);
            replaceOnce(builder, "%s", utf8keywords);
            replaceOnce(builder, "{google:RLZ}", "oq=" + utf8keywords);
            replaceOnce(builder, "{google:originalQueryForSuggestion}", "&aqs=chrome..69i57j0l2j69i60j0l2.1630j0j7");
            replaceOnce(builder, "{google:sourceId}", "&sourceid=chrome");
            replaceOnce(builder, "{inputEncoding}", "&" + systemEncoding);

            replaceOnce(builder, "{google:assistedQueryStats}", "");
            replaceOnce(builder, "{google:searchFieldtrialParameter}", "");
            replaceOnce(builder, "{google:iOSSearchLanguage}", "");
            replaceOnce(builder, "{google:searchClient}", "");
            replaceOnce(builder, "{google:contextualSearchVersion}", "");

            return builder.toString();

        } catch (UnsupportedEncodingException e) { // UTF-8対応していなかったらエラー出るけど、別に対応していないパソコンなんてないっしょ！
            e.printStackTrace();
        }

        return "";
    }

    // 指定の長さ(Font的に)に制限され、「...」を付けられたStringを返す
    // firstStrは、初めに、修正後のStringに代入される値. 無文字のStringを返してしまうのを防ぐ為
    public static String getLimitedString(String original, int maxLength, String firstStr) {
        if(defaultMetrics.computeStringWidth(original) > maxLength) {
            String lastStr = firstStr;
            for (int i = 0; i < original.length(); i++) {
                String str = original.substring(0, i);
                if (defaultMetrics.computeStringWidth(str) <= maxLength)
                    lastStr = str + "...";
                else
                    break; // サイズオーバーしたら止める
            }
            return lastStr;
        }

        return original;
    }

    // oldに該当するものの最初の1つをnew_に書き換える
    private static void replaceOnce(StringBuilder str, String old, String new_) {
        int index = str.indexOf(old);
        str = str.replace(index, index + old.length(), new_);
    }
}
