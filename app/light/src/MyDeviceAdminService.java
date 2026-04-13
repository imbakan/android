package balikbayan.box.light;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

public class MyDeviceAdminService extends Service {

    private IBinder binder;
    private Handler handler;
    protected Server server;

    // ito ay event para malaman kung ang screen ay on o off
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {

                if (server.client != null)
                    server.client.send(Client.LIGHT_ON);

            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {

                if (server.client != null)
                    server.client.send(Client.LIGHT_OFF);
            }
        }
    };

    // ito ay event para malaman kung inadjust ang brightness (10 - 255)
    private ContentObserver contentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, @Nullable Uri uri) {
            super.onChange(selfChange, uri);
            int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);

            //Log.d("KLGYN", String.format(" MyDeviceAdminService : %10d", brightness));

            if (server.client != null) {
                server.client.send(Client.LIGHT_BRIGHTNESS);
                server.client.send(brightness);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // Register the receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(broadcastReceiver, filter);

        // Register the observer
        getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), false, contentObserver);

        binder = new MyDeviceAdminBinder();
        server = new Server(this);

        Log.d("KLGYN", "service create");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister the observer
        getContentResolver().unregisterContentObserver(contentObserver);

        // Unregister the receiver
        unregisterReceiver(broadcastReceiver);

        Log.d("KLGYN", "service destroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("KLGYN", "service start");

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

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public Handler getHandler() {
        return handler;
    }

    // ito ang magrereturn ng service object sa MainActivity
    public class MyDeviceAdminBinder extends Binder {
        public MyDeviceAdminService getService() {
            return MyDeviceAdminService.this;
        }
    }
}
