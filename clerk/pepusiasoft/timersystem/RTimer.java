package clerk.pepusiasoft.timersystem;

import java.util.TimerTask;

public class RTimer {
    public static final java.util.Timer timer = new java.util.Timer();
    private static long delay = 0;

    /*
    * 一定時間ごとのupdate()呼び出しを可にする
    * */
    public static void start(long delay) {

        RTimer.delay = delay;

        //onFrame();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                RunScheduler.onFrame();
            }
        }, delay, delay);
    }

    public static long getDelay() {
        return delay;
    }
}
