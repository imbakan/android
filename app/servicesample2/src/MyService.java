package balikbayan.box.servicesample2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MyService extends Service {

    private final String CHANNEL_ID = "Channel_Id";
    private final String CHANNEL_NAME = "Channel_Name";
    private final String CONTENT_TITLE = "Content_Title";
    private final String CONTENT_TEXT = "Content_Text";

    private final int NOTIFICATION_ID = 1;

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

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(CONTENT_TITLE)
                .setContentText(CONTENT_TEXT)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
                .setSilent(true)
                .build();

        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);

        // hanggang dito ang code para sa service
        // ang susunod dito ay ang ipaprocess ng service

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

    // ang handler na 'to ay galing sa MainActivity
    // ito ang gagamitin para ang service ay makapagsend sa MainActivity
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    private void sendMessage(Handler handler, int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

    // gagamitin ng MainActivity ang service object para maaccess ang mga method nito
    public class MyBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

}
