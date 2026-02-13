package balikbayan.box.light;

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

public class MyDeviceAdminService extends Service {

    public static final int BOUND = 9001;

    public static final String STATE = "balikbayan.box.light.current.state";

    private IBinder binder;
    private Handler handler;
    private Messenger messenger;
    private Server server;

    @Override
    public void onCreate() {
        super.onCreate();

        binder = new MyDeviceAdminBinder();
        server = new Server(this);

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

        int state = intent.getIntExtra(STATE, 0);

        server.init(this, handler, state);
        new Thread(server).start();

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

    private void onBound(Message msg) {
        handler = (Handler) msg.obj;
        Log.d("KLGYN", "service bound");
    }

    public void shutdownServer() {
        server.shutdown();
    }

    public void shutdownClient() {
        server.shutdownClient();
    }

    public boolean isServerRunning() {
        return server.isRunning();
    }

    public boolean isClientRunning() {
        return server.isClientRunning();
    }

    public void flipSwitchON() {
        server.send(Client.CURRENT_STATE);
        server.send(Client.LIGHT_ON);
    }

    public void flipSwitchOFF() {
        server.send(Client.CURRENT_STATE);
        server.send(Client.LIGHT_OFF);
    }

    public IBinder getMessengerBinder() {
        messenger = new Messenger(new MyDeviceAdminHandler(getMainLooper()));
        return messenger.getBinder();
    }

    public void saveCurrentState(int state) {
        server.saveCurrentState(state);
    }

    private void sendMessage(Handler handler, int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

    public class MyDeviceAdminBinder extends Binder {
        public MyDeviceAdminService getService() {
            return MyDeviceAdminService.this;
        }
    }

    // ang message na isesend ng MainActivity ay dito marereceive
    private class MyDeviceAdminHandler extends Handler {

        public MyDeviceAdminHandler(@NonNull Looper looper) {
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
