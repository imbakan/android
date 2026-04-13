
// light switch
// client side
// Bluetooth
// Android 14 API Level 34

// App to turn on/off light remotely via bluetooth
// A companion app : Light app

package balikbayan.box.switch2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Switch switch1;
    private Client client;
    private Handler handler;
    private int brightness;
    private boolean access_granted, no_echo;

    // reply sa requestPermissionBTConnect
    ActivityResultLauncher<String> launcher1 = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted)
            requestPermissionBTEnability();
        else
            access_granted = false;
    });

    // reply sa requestPermissionBTEnability
    BluetoothContract contract2 = new BluetoothContract();
    ActivityResultLauncher<Void> launcher2 = registerForActivityResult(contract2, new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if(o) {
                initialize();
            } else {
                access_granted = false;
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        switch1 = findViewById(R.id.switch1);
        switch1.setEnabled(false);

        //  +------------------------------------------------------------------------+
        //  |                               switch                                   |
        //  +------------------------------------------------------------------------+

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {

                if (b)
                    compoundButton.setText(R.string.switch_name_1);
                else
                    compoundButton.setText(R.string.switch_name_2);

                // ito ay para di ma-execute ang mga nasa baba ng if statement
                // ito ay para lang baguhin ang display ng swtch
                if (no_echo) {
                    no_echo = false;
                    return;
                }

                //Log.d("KLGYN", String.format("%s", b?"ON":"OFF"));

                int state = b ? Client.LIGHT_ON : Client.LIGHT_OFF;
                client.send(state);
            }
        });

        //  +------------------------------------------------------------------------+
        //  |                          handler message                               |
        //  +------------------------------------------------------------------------+

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what){
                    case Client.CONNECTING:         onClientConnecting(msg);    break;
                    case Client.RUNNING:            onClientRunning(msg);       break;
                    case Client.SHUTTING_DOWN:      onClientShuttingDown(msg);  break;
                    case Client.LIGHT_BRIGHTNESS:   onLightBrightness(msg);     break;
                    case Client.LIGHT_ON:           onLightOn(msg);             break;
                    case Client.LIGHT_OFF:          onLightOff(msg);            break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };

        //  +------------------------------------------------------------------------+
        //  |                            permission                                  |
        //  +------------------------------------------------------------------------+

        access_granted = no_echo = false;

        requestPermissionBTConnect();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean running = false;

        if (client != null)
            running = client.isRunning();

        menu.findItem(R.id.mnuConnect).setEnabled(access_granted && !running);
        menu.findItem(R.id.mnuDisconnect).setEnabled(access_granted && running);
        menu.findItem(R.id.mnuBrightness).setEnabled(access_granted && running);
        menu.findItem(R.id.mnuLight).setEnabled(access_granted && running);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mnuConnect)
            onClientConnect();
        else if (id == R.id.mnuDisconnect)
            onClientDisconnect();
        else if (id == R.id.mnuBrightness)
            onToolsBrightness();
        else if (id == R.id.mnuLight)
            onToolsLight();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    private void onClientConnect() {
        ConnectToDefault(this);
    }

    private void onClientDisconnect() {
        client.shutdown();
    }

    private void onToolsBrightness() {

        BrightnessDialog dlg = new BrightnessDialog(this, new BrightnessDialog.OnChangeListener() {
            @Override
            public void onChange(int i) {
                client.send(Client.LIGHT_BRIGHTNESS);
                client.send(i);
            }
        }, brightness);

        dlg.show(getSupportFragmentManager(), "brightness dialog");
    }

    private void onToolsLight() {

        LightColorDialog dlg = new LightColorDialog(this, new LightColorDialog.OnChangeListener() {
            @Override
            public void onChange(int i) {
                client.send(Client.LIGHT_COLOR);
                client.send(i);
            }
        }, brightness);

        dlg.show(getSupportFragmentManager(), "light dialog");
    }

    private void onClientConnecting(Message msg) {
        toolbar.setTitle("CONNECTING ...");
    }

    private void onClientRunning(Message msg) {
        toolbar.setTitle("ONLINE");
        switch1.setEnabled(true);
    }

    private void onClientShuttingDown(Message msg) {
        toolbar.setTitle("OFFLINE");
        switch1.setEnabled(false);
    }

    private void onLightBrightness(Message msg) {
        brightness = (int)msg.obj;
    }

    private void onLightOn(Message msg) {
        if (!switch1.isChecked()) {
            no_echo = true;
            switch1.setChecked(true);
        }
    }

    private void onLightOff(Message msg) {
        if (switch1.isChecked()) {
            no_echo = true;
            switch1.setChecked(false);
        }
    }

    private void ConnectToDefault(Context context) {

        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        Set<BluetoothDevice> devices = manager.getAdapter().getBondedDevices();
        BluetoothDevice target = null;
        String str = "Lenovo";

        if(devices.size() > 0) {

            for (BluetoothDevice device : devices) {
                if (device.getName().contains(str)) {
                    target = device;
                    break;
                }
            }

            Log.d("KLGYN", String.format("%-20s%-20s", target.getName(), target.getAddress()));

            client = new Client(handler, target);
            new Thread(client).start();
        }
    }

    private void requestPermissionBTConnect() {

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
            requestPermissionBTEnability();
        else
            launcher1.launch(Manifest.permission.BLUETOOTH_CONNECT);
    }

    private void requestPermissionBTEnability() {
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adaptor = manager.getAdapter();

        if (adaptor != null && adaptor.isEnabled()) {
            initialize();
        } else {
            launcher2.launch(null);
        }
    }

    private void initialize() {
        access_granted = true;
        ConnectToDefault(this);
    }

}
