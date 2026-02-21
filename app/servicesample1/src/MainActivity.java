
// Android 6 Api 23

package balikbayan.box.servicesample1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView textView;
    private MyService service;
    private Handler handler;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            MyService.MyBinder binder = (MyService.MyBinder) iBinder;
            service = binder.getService();
            service.setHandler(handler);

            Log.d("KLGYN", "service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        textView = findViewById(R.id.textView);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MyService.COUNT: onCounting(msg); break;
                    default:
                        super.handleMessage(msg);
                }
                super.handleMessage(msg);
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MyService.class);
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

        boolean running = false;

        if (service != null)
            running = service.isRunning();

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
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
    }

    private void onServiceStop() {
        service.stop();
    }

    private void onCounting(Message msg) {
        long count = (long)msg.obj;
        textView.setText(String.format(Locale.US,"%d", count));
    }

}
