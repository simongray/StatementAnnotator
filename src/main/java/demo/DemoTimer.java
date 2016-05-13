package demo;

/**
 * Created by simongray on 16/04/2016.
 */
public class DemoTimer {
    static String task;
    static long start;

    public static void start(String task) {
        DemoTimer.task = task;
        DemoTimer.start = System.currentTimeMillis();
    }

    public static void stop() {
        System.out.println(task + " took " + (System.currentTimeMillis()-start)/1000.0 + " seconds");
    }
}
