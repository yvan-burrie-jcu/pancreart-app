package jcu.cp3407.pancreart;

import jcu.cp3407.pancreart.model.PodHandler;

import java.util.*;

public abstract class Task extends TimerTask {

    final ArrayList<Task> tasks = new ArrayList<>();

    final Timer timer;

    /** Specifies running period in number of seconds. */
    final long interval;

    double speed;

    PodHandler handler;

    Task(Timer timer, long interval, double speed) {
        this.timer = timer;
        this.interval = interval;
        this.speed = speed;
    }

    void initiate() {
        long oneSecond = (long) (speed * 1000);
        timer.schedule(this, 0, interval * oneSecond);
        for (Task task : tasks) {
            task.initiate();
        }
    }

    void registerTask(Task task) {
        tasks.add(task);
    }

    void assignHandler(PodHandler handler) {
        this.handler = handler;
        for (Task task : tasks) {
            task.assignHandler(handler);
        }
    }
}
