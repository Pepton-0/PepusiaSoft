package clerk.pepusiasoft.language;

import clerk.pepusiasoft.Debug;

import java.util.Locale;

public class Lang {

    private static Locale locale;
    private static final LanguageData defaultLang;
    private static LanguageData loadedLanguage;

    // load language
    static {
        // 米国のをデフォルトとする
        defaultLang = new LanguageData();
        defaultLang.initialize("US");

        // load language data instances
        locale = Locale.getDefault();
        String country = locale.getCountry();

        // 既にデフォルト言語としてロード済みなので、処理は不要
        if(country.equals("US")) {
            loadedLanguage = defaultLang;
        }
        else {
            loadedLanguage = new LanguageData();
            if(!loadedLanguage.initialize(country)) {
                Debug.log("The optimal language data for " + country +
                        " is not found, so replaced it with US language data");
                loadedLanguage = defaultLang; // もしロード失敗したら、米国ので代用
            }
            else {
                Debug.log("The optimal language is found.");
            }
        }
    }

    public static String of(String name) {

        String s = loadedLanguage.of(name);

        // 見つからなかったらデフォルトので取得
        if(s.equals(":" + name + ":")) {
            s = defaultLang.of(name);
        }

        return s;
    }

    public static Locale getLocale(){
        return locale;
    }
}
