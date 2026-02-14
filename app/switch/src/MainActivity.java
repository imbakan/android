
// light switch
// client side
// Bluetooth
// Android 14 API Level 34

// App to turn on/off light remotely via bluetooth
// A companion app : Light app

package balikbayan.box.aswitch;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Switch aSwitch;
    private RadioGroup radioGroup;
    private RadioButton[] radioButton;
    private Client client;
    private Handler handler;
    private ArrayList<Integer> uiID = new ArrayList<>();
    private ArrayList<String> uiText = new ArrayList<>();
    private ArrayList<Integer> lightColor = new ArrayList<>();
    private int[] id = new int[20];
    private boolean access_granted, dont_send;

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

        aSwitch = findViewById(R.id.switch1);

        radioGroup = findViewById(R.id.radioGroup);

        uiID.add(R.id.radioButton1);
        uiID.add(R.id.radioButton2);
        uiID.add(R.id.radioButton3);
        uiID.add(R.id.radioButton4);
        uiID.add(R.id.radioButton5);
        uiID.add(R.id.radioButton6);
        uiID.add(R.id.radioButton7);
        uiID.add(R.id.radioButton8);
        uiID.add(R.id.radioButton9);
        uiID.add(R.id.radioButton10);

        uiID.add(R.id.radioButton11);
        uiID.add(R.id.radioButton12);
        uiID.add(R.id.radioButton13);
        uiID.add(R.id.radioButton14);
        uiID.add(R.id.radioButton15);
        uiID.add(R.id.radioButton16);
        uiID.add(R.id.radioButton17);
        uiID.add(R.id.radioButton18);
        uiID.add(R.id.radioButton19);
        uiID.add(R.id.radioButton20);

        uiText.add("Black Light Fluorescent");  lightColor.add(Color.rgb(167, 0, 255));
        uiText.add("Blue");                     lightColor.add(Color.rgb(0, 0, 255));
        uiText.add("Brown");                    lightColor.add(Color.rgb(150, 75, 0));
        uiText.add("Bulb (100 W)");             lightColor.add(Color.rgb(255, 214, 170));
        uiText.add("Bulb (40 W)");              lightColor.add(Color.rgb(255, 197, 143));
        uiText.add("Bulb");                     lightColor.add(Color.rgb(255, 194, 0));
        uiText.add("Candle");                   lightColor.add(Color.rgb(255, 147, 41));
        uiText.add("Cyan");                     lightColor.add(Color.rgb(0, 255, 255));
        uiText.add("Green");                    lightColor.add(Color.rgb(0, 255, 0));
        uiText.add("Grey");                     lightColor.add(Color.rgb(128, 128, 128));

        uiText.add("Indigo");                       lightColor.add(Color.rgb(75, 0, 130));
        uiText.add("Magenta");                      lightColor.add(Color.rgb(255, 0, 255));
        uiText.add("Orange");                       lightColor.add(Color.rgb(255, 165, 0));
        uiText.add("Red");                          lightColor.add(Color.rgb(255, 0, 0));
        uiText.add("Sodium Lamp");                  lightColor.add(Color.rgb(255, 209, 178));
        uiText.add("Sodium Lamp (High Pressure)");  lightColor.add(Color.rgb(255, 183, 76));
        uiText.add("Violet");                       lightColor.add(Color.rgb(127, 0, 255));
        uiText.add("White");                        lightColor.add(Color.rgb(255, 255, 255));
        uiText.add("Yellow");                       lightColor.add(Color.rgb(255, 255, 0));
        uiText.add("Neon");                         lightColor.add(Color.rgb(255, 61, 0));

        radioButton = new RadioButton[20];

        int i;
        for(i=0; i<20; i++) {
            radioButton[i] = findViewById(uiID.get(i));
            radioButton[i].setText(uiText.get(i));
        }

        aSwitch.setEnabled(false);
        setRadioGroupButtonEnabled(false);

        //  +------------------------------------------------------------------------+
        //  |                            light switch                                |
        //  +------------------------------------------------------------------------+

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {

                if (b)
                    setRadioGroupButtonEnabled(true);
                else
                    setRadioGroupButtonEnabled(false);

                // para lang iswitch ang switch na 'to
                // hindi i on/off ang light
                if (dont_send) {
                    dont_send = false;
                    return;
                }

                // turn on/off light
                if (b)
                    onToolsTurnON();
                else
                    onToolsTurnOFF();
            }
        });


        //  +------------------------------------------------------------------------+
        //  |                            light color                                 |
        //  +------------------------------------------------------------------------+

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup radioGroup, int i) {
                RadioButton checkedRadioButton = findViewById(i);
                int index = uiID.indexOf(i);

                //Log.d("KLGYN", String.format("%10d", index));

                client.send(Client.LIGHT_COLOR);
                client.send(lightColor.get(index));
            }
        });

        //  +------------------------------------------------------------------------+
        //  |                          handler message                               |
        //  +------------------------------------------------------------------------+

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what){
                    case Client.CONNECTING:     onClientConnecting(msg);    break;
                    case Client.RUNNING:        onClientRunning(msg);       break;
                    case Client.SHUTTING_DOWN:  onClientShuttingDown(msg);  break;
                    case Client.CURRENT_STATE:  onCurrentSate(msg);         break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };

        //  +------------------------------------------------------------------------+
        //  |                            permission                                  |
        //  +------------------------------------------------------------------------+

        access_granted = dont_send = false;

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

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.mnuConnect)
            onClientConnect();
        else if (id == R.id.mnuDisconnect)
            onClientDisconnect();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    private void onClientConnect() {
    }

    private void onClientDisconnect() {
        client.shutdown();
    }

    private void onClientConnecting(Message msg) {
        toolbar.setTitle("CONNECTING ...");
    }

    private void onClientRunning(Message msg) {
        toolbar.setTitle("ONLINE");
        aSwitch.setEnabled(true);
    }

    private void onClientShuttingDown(Message msg) {
        toolbar.setTitle("OFFLINE");
        aSwitch.setEnabled(false);
        setRadioGroupButtonEnabled(false);
    }

    private void onCurrentSate(Message msg) {
        int li_state = (int) msg.obj;
        int sw_pos = aSwitch.isChecked() ? Client.LIGHT_ON : Client.LIGHT_OFF;

        if (li_state == sw_pos) return;

        dont_send = true;
        boolean check = !aSwitch.isChecked();
        aSwitch.setChecked(check);
    }

    private void setRadioGroupButtonEnabled(boolean enabled) {
        int i;

        for (i=0; i<20; i++)
            radioButton[i].setEnabled(enabled);

    }

    private void autoConnect(Context context, String str ) {

        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        Set<BluetoothDevice> devices = manager.getAdapter().getBondedDevices();
        BluetoothDevice target = null;

        if(devices.size() > 0) {

            for (BluetoothDevice device : devices) {
                if (device.getName().contains(str)) {
                    target = device;
                    break;
                }
            }

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

       // ito ang device name kung saan nakainstall ang light app
        autoConnect(this, "Motorola");
    }

}
