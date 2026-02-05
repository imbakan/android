package balikbayan.box.sampleservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MySampleService extends Service {

    public static final int BOUND = 9001;

    private IBinder binder;
    private Handler handler;
    private Messenger messenger;
    private MySampleRunnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new MySampleBinder();
        runnable = new MySampleRunnable(MySampleService.this);
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

    public boolean isCounterRunning() {
        return runnable.isRunning();
    }

    // ito ay maaaccess sa MainActivity
    public void stopCounter() {
        runnable.stop();
    }

    // ang handler object ay ang gagamitin ng service nato para makapagsend sa MainActivity
    private void onBound(Message msg) {
        handler = (Handler) msg.obj;
        runnable.setHandler(handler);
    }

    // irereturn nito ang messenger object sa MainActivity
    // gagamitin ng MainActivity ang messenger object para makapagsend dito
    public IBinder getMessengerBinder() {
        messenger = new Messenger(new MySampleHandler(getMainLooper()));
        return messenger.getBinder();
    }

    // irereturn nito ang service object sa MainAcivity
    // ang service object na to ang gagamitin ng MainActivity para maaccess ang method ng service class
    public class MySampleBinder extends Binder {
        public MySampleService getService() {
            return MySampleService.this;
        }
    }

    // ang message na isesend ng MainActivity ay dito marereceive
    private class MySampleHandler extends Handler {

        public MySampleHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case BOUND: onBound(msg); break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
