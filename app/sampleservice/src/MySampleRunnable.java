package balikbayan.box.sampleservice;

import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.os.Handler;

public class MySampleRunnable implements Runnable {

    public static final int COUNT = 1001;

    private Thread thread;
    private Handler handler;
    private MySampleService service;
    private long count;
    private boolean running;

    public MySampleRunnable(MySampleService service) {
        count = 0L;
        running = false;
        this.service = service;
    }

    @Override
    public void run() {
        int i;

        thread =  Thread.currentThread();

        running = true;

        while (true) {

            if(thread.isInterrupted()) break;

            SystemClock.sleep(500);

            ++count;
            sendMessage(handler, COUNT, count);
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

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    private void sendMessage(Handler handler, int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

}
