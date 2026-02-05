
// Android 6 Api 23

package balikbayan.box.sampleservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView textView;
    private Handler handler;
    private MySampleService service;
    private Messenger messenger;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            MySampleService.MySampleBinder binder = (MySampleService.MySampleBinder) iBinder;
            service = binder.getService();

            messenger = new Messenger(service.getMessengerBinder());
            sendMessage(messenger, MySampleService.BOUND, handler);

            Log.d("KLGYN", "service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("KLGYN", "service disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what){
                    case MySampleRunnable.COUNT: onCount(msg); break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MySampleService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(serviceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        boolean running = service.isCounterRunning();

        menu.findItem(R.id.mnuStart).setEnabled(!running);
        menu.findItem(R.id.mnuStop).setEnabled(running);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.mnuStart)
            onServiceStart();
        else if (id == R.id.mnuStop)
            onServiceStop();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    private void onServiceStart() {
        Intent intent = new Intent(this, MySampleService.class);
        startService(intent);
    }

    // pwedeng gamitin ito para iistop ang service
    // Intent intent = new Intent(this, MySampleService.class);
    // stopService(intent);
    //  o ang stopCounter para iistop sa loob ng service
    private void onServiceStop() {
        service.stopCounter();
    }

    private void onCount(Message msg) {
        long count = (long) msg.obj;
        textView.setText(String.format(Locale.US,"%d", count));
        Log.d("KLGYN", String.format("%20d", count));

    }

    private void sendMessage(Messenger messenger, int what, Object obj) {
        Message msg;

        try {
            msg = Message.obtain(null, what, obj);
            messenger.send(msg);
        } catch (RemoteException e) {
            Log.d("KLGYN", e.toString());
        }
    }

}
