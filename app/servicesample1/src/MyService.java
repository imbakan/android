package balikbayan.box.servicesample1;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

public class MyService extends Service {

    public static final int COUNT = 5001;

    private IBinder binder;
    private Handler handler;
    private MyRunnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new MyBinder();
        runnable = new MyRunnable(MyService.this, new MyRunnable.OnCountListener() {
            @Override
            public void onCount(long i) {
                sendMessage(handler, COUNT, i);
            }
        });
        Log.d("KLGYN", "service create");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("KLGYN", "service destroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("KLGYN", "service start");

        new Thread(runnable).start();

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("KLGYN", "service bind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("KLGYN", "service unbind");
        return super.onUnbind(intent);
    }

    public boolean isRunning() {
        return runnable.isRunning();
    }

    public void stop() {
        runnable.stop();
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    private void sendMessage(Handler handler, int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

    public class MyBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

}
