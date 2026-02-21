package balikbayan.box.servicesample1;

import android.os.SystemClock;

public class MyRunnable implements Runnable {

    private Thread thread;
    private MyService service;
    private OnCountListener listener;
    private long count;
    private boolean running;

    public MyRunnable(MyService service, OnCountListener listener) {
        count = 0L;
        running = false;
        this.service = service;
        this.listener = listener;
    }

    @Override
    public void run() {

        thread =  Thread.currentThread();

        running = true;

        while (true) {

            if(thread.isInterrupted()) break;

            SystemClock.sleep(500);

            ++count;

            listener.onCount(count);
        }

        running = false;

        service.stopSelf();
    }

    public void stop() {
        thread.interrupt();
    }

    public boolean isRunning() {
        return running;
    }

    public interface OnCountListener {
        void onCount(long i);
    }
}
