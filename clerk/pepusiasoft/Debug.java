package clerk.pepusiasoft;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/*
  ログ出力を行う
 */
public class Debug {

    static ZonedDateTime now = ZonedDateTime.now();

    public static void log(String... m) {
        StackTraceElement trace = new Throwable().getStackTrace()[1];
        String traceStr = trace.getMethodName() + "(" + trace.getFileName() + ":" + trace.getLineNumber() + ")";
        String time = now.format(DateTimeFormatter.ISO_LOCAL_TIME);
        for(String s : m) {
            System.out.println("[" + time + ", " + traceStr + "]" + s);
        }
    }

    //at clerk.pepusiasoft.App.<clinit>(App.java:19)

    public static void error(String... m) {
        for(String s : m) {
            System.err.println(s);
        }
    }

    public static void enphasis(String... m) {

        log("", "", "--------------------------------------------");

        for(String s : m) {
            log("     " + s);
        }

        log("--------------------------------------------", "", "");
    }
}
