
// light
// server side
// Bluetooth
// Android 6 Api 23

// App to turn on/off light remotely via bluetooth
// A companion app : Switch app

package balikbayan.box.light;

import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private View layout;
    private Toolbar toolbar;

    private Handler handler;
    private Messenger messenger;
    private MyDeviceAdminService service;

    private boolean access_granted;

    MyDeviceAdminContract contract1 = new MyDeviceAdminContract();

    ActivityResultLauncher<Void> launcher1 = registerForActivityResult(contract1, new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if (o)
                requestPermissionBluetooth();
            else
                access_granted = false;
        }
    });

    BluetoothContract contract2 = new BluetoothContract();

    ActivityResultLauncher<Void> launcher2 = registerForActivityResult(contract2, new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if (o)
                initialize();
            else
                access_granted = false;
        }
    });

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (service == null || intent == null || intent.getAction() == null) return;

            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {

                service.saveCurrentState(Client.LIGHT_ON);

                if (service.isClientRunning()) service.flipSwitchON();

            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {

                service.saveCurrentState(Client.LIGHT_OFF);

                if (service.isClientRunning()) service.flipSwitchOFF();
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            MyDeviceAdminService.MyDeviceAdminBinder binder = (MyDeviceAdminService.MyDeviceAdminBinder) iBinder;
            service = binder.getService();

            messenger = new Messenger(service.getMessengerBinder());
            sendMessage(messenger, MyDeviceAdminService.BOUND, handler);

            Log.d("KLGYN", "service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("KLGYN", "main create");

        EdgeToEdge.enable(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //  +------------------------------------------------------------------------+
        //  |                          handler message                               |
        //  +------------------------------------------------------------------------+

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what){
                    case Server.RUNNING:        onServerRunning(msg);       break;
                    case Server.SHUTTING_DOWN:  onServerShuttingDown(msg);  break;
                    case Client.RUNNING:        onClientRunning(msg);       break;
                    case Client.SHUTTING_DOWN:  onClientShuttingDown(msg);  break;
                    case Client.LIGHT_ON:       onLightOn(msg);             break;
                    case Client.LIGHT_OFF:      onLightOff(msg);            break;
                    case Client.LIGHT_COLOR:    onLightColor(msg);          break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };

        //  +------------------------------------------------------------------------+
        //  |                         request permission                             |
        //  +------------------------------------------------------------------------+

        access_granted = false;

        requestPermissionDeviceAdmin();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, MyDeviceAdminService.class);
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

        if (service == null) return super.onPrepareOptionsMenu(menu);

        boolean server_running = service.isServerRunning();
        boolean client_running = service.isClientRunning();

        menu.findItem(R.id.mnuRun).setEnabled(access_granted & !server_running);
        menu.findItem(R.id.mnuShutdown).setEnabled(access_granted & server_running);
        menu.findItem(R.id.mnuClose).setEnabled(access_granted & client_running);
        menu.findItem(R.id.mnuDebug).setEnabled(access_granted);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.mnuRun)
            onServerRun();
        else if (id == R.id.mnuShutdown)
            onServerShutdown();
        else if (id == R.id.mnuClose)
            onClientClose();
        else if (id == R.id.mnuDebug)
            onToolsDebug();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    private void onServerRun() {
        Intent intent = new Intent(this, MyDeviceAdminService.class);
        intent.putExtra(MyDeviceAdminService.STATE, Client.LIGHT_ON);
        startService(intent);
    }

    private void onServerShutdown() {
        service.shutdownServer();
    }

    private void onClientClose() {
        service.shutdownClient();
    }

    private void onToolsDebug() {

    }

    private void onServerRunning(Message msg) {

        setLightColor(0, 0, 255);

        // Register the receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(broadcastReceiver, filter);

    }

    private void onServerShuttingDown(Message msg) {

        setLightColor(255, 255, 255);

        // Unregister the receiver
        unregisterReceiver(broadcastReceiver);
    }

    private void onClientRunning(Message msg) {
    }

    private void onClientShuttingDown(Message msg) {
    }

    private void onLightOn(Message msg) {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,"MyApp:MyWakelockTag");
        wakeLock.acquire();
        wakeLock.release();
    }

    private void onLightOff(Message msg) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        devicePolicyManager.lockNow();
    }

    private void onLightColor(Message msg) {
        int color = (int) msg.obj;
        setLightColor(color);
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

    private void setLightColor(int red, int green, int blue) {
        int color = Color.rgb(red, green, blue);
        toolbar.setBackgroundColor(color);
        layout.setBackgroundColor(color);
        getWindow().setStatusBarColor(color);
    }

    private void setLightColor(int color) {
        toolbar.setBackgroundColor(color);
        layout.setBackgroundColor(color);
        getWindow().setStatusBarColor(color);
    }

    private void requestPermissionDeviceAdmin() {

        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(this, MyDeviceAdminReceiver.class);
        boolean active = devicePolicyManager.isAdminActive(componentName);

        if(active)
            requestPermissionBluetooth();
        else
            launcher1.launch(null);
    }

    private void requestPermissionBluetooth() {

        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adaptor = manager.getAdapter();

        if (adaptor != null && adaptor.isEnabled())
            initialize();
        else
            launcher2.launch(null);
    }

    private void initialize() {
        access_granted = true;
    }

}
