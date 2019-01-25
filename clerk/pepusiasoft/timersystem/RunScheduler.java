package clerk.pepusiasoft.timersystem;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class RunScheduler {

    private static final CopyOnWriteArrayList<Task> queue = new CopyOnWriteArrayList<>();

    private static final ArrayList<Task> removeTemp = new ArrayList<>();

    public static void add(double seconds, Runnable runnable) {
        queue.add(new Task(seconds, runnable, false));
    }
    public static void addRepeatable(double interval, Runnable runnable) { queue.add(new Task(interval, runnable, true)); }

    static void onFrame() {
        removeTemp.clear();
        for (Task task : queue) {
            if (task.seconds <= 0) {
                task.runnable.run();
                task.seconds = -1;
                if(task.repeatable)
                    addRepeatable(task.interval, task.runnable::run);

                removeTemp.add(task);
            } else {
                task.seconds -= (double) RTimer.getDelay() / 1000d;
            }
        }
        queue.removeAll(removeTemp);
    }


    private static class Task {
        private double seconds;
        private final Runnable runnable;
        private final boolean repeatable;

        private final double interval;

        Task(double seconds, Runnable runnable, boolean repeatable) {
            this.seconds = seconds;
            this.runnable = runnable;
            this.repeatable = repeatable;

            interval = repeatable ? seconds : 0d;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Task) {
                if (((Task) obj).runnable == this.runnable) {
                    return true;
                }
            }
            return false;
        }
    }
}

