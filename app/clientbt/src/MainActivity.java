
// client-server model
// client side
// Bluetooth
// Android 13 Api 33

package balikbayan.box.clientbt;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TreeViewEventListener {

    private final int BLUETOOTH_REQUEST_CODE = 1001;

    private RecyclerView recyclerView1, recyclerView2;
    private TextView textView1;
    private EditText editText1, editText2;

    private Client client;
    private Handler handler;
    private boolean access_granted, running, item_selected;

    BluetoothContract contract = new BluetoothContract();

    ActivityResultLauncher<Void> launcher = registerForActivityResult(contract, new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if(o) {
                access_granted = true;
                initialize();
            } else {
                access_granted = false;
            }
        }
    });

    OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            Toast.makeText(getApplicationContext(), "TUMATAKBO PA ANG CLIENT", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String str = getResources().getString(R.string.app_name);
        setTitle(str + " - offline");

        item_selected = running = false;

        editText1 = findViewById(R.id.editText1);
        editText1.setKeyListener(null);

        editText2 = findViewById(R.id.editText2);
        editText2.setKeyListener(null);

        textView1 = findViewById(R.id.textView1);

        recyclerView1 = findViewById(R.id.recyclerView1);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        TreeViewAdapter adapter1 = new TreeViewAdapter(this, (TreeViewEventListener) this);
        recyclerView1.setAdapter(adapter1);

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        //  +------------------------------------------------------------------------+
        //  |                          handler message                               |
        //  +------------------------------------------------------------------------+

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what){
                    case ConnectDialog.CONNECT:                 onConnect(msg);                 break;
                    case Client.LOG_MESSAGE:                    onLogMessage(msg);              break;
                    case Client.CONNECTING:                     onConnecting(msg);              break;
                    case Client.RUNNING:                        onRunning(msg);                 break;
                    case Client.SHUTTING_DOWN:                  onShuttingDown(msg);            break;
                    case Client.POPULATE_WITH_DEVICES:          onPopulateWithDevices(msg);     break;
                    case Client.POPULATE_WITH_DEVICE:           onPopulateWithDevice(msg);      break;
                    case Client.DEPOPULATE_BY_DEVICE:           onDepopulateByDevice(msg);      break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };

        //  +------------------------------------------------------------------------+
        //  |                         request permission                             |
        //  +------------------------------------------------------------------------+

        requestPermissionDevice();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == BLUETOOTH_REQUEST_CODE)
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                requestPermissionBluetooth();
            else
                access_granted = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.mnuConnect).setEnabled(access_granted && !running);
        menu.findItem(R.id.mnuDisconnect).setEnabled(access_granted && running);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.mnuConnect)
            onClientConnect();
        else if(id == R.id.mnuDisconnect)
            onClientDisconnect();
        else if(id == R.id.mnuDownload)
            onToolsDownload();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    private void onClientConnect() {
        ConnectDialog dlg = new ConnectDialog(handler, this);
        dlg.show(getSupportFragmentManager(), "connect dialog");
    }

    private void onClientDisconnect() {
        client.shutdown();
    }

    private void onToolsDownload() {
    }

    private void requestPermissionDevice() {

        if(checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
            requestPermissionBluetooth();
        else
            requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_REQUEST_CODE);
    }

    private void requestPermissionBluetooth() {

        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adaptor = manager.getAdapter();

        if (adaptor != null && adaptor.isEnabled()) {
            access_granted = true;
            initialize();
        } else {
            launcher.launch(null);
        }
    }

    private void initialize() {

        String name = Build.MANUFACTURER.toString().toUpperCase() + " " + Build.MODEL.toString();
        client = new Client(handler, name);
    }

    private void sendMessage(int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

    private void onLogMessage(Message msg) {
        String str1, str2, str3;

        str1 = editText2.getText().toString();
        str2 = (String) msg.obj;
        str3 = str1 + "\n" + str2;
        editText2.setText(str3);
    }

    private void onConnect(Message msg) {
        BluetoothDevice device = (BluetoothDevice) msg.obj;
        client.connect(device);
        new Thread(client).start();
    }

    private void onConnecting(Message msg) {

        running = true;

        String str = getResources().getString(R.string.app_name);
        setTitle(str + " - Connecting");
    }

    private void onRunning(Message msg) {

        running = true;

        String str = getResources().getString(R.string.app_name);
        setTitle(str + " - Online");

        onBackPressedCallback.setEnabled(running);
    }

    private void onShuttingDown(Message msg) {
        Client client = (Client)msg.obj;

        running = false;

        String str = getResources().getString(R.string.app_name);
        setTitle(str + " - Offline");

        TreeViewAdapter adapter =  (TreeViewAdapter) recyclerView1.getAdapter();
        int n = adapter.getItemCount();
        adapter.clear();
        adapter.notifyItemRangeRemoved(0, n);

        onBackPressedCallback.setEnabled(running);

        sendMessage(Client.LOG_MESSAGE, String.format("The client thread 0x%x has exited.", client.getId()));
    }

    private void onPopulateWithDevices(Message msg) {
        ArrayList<Attribute> array = (ArrayList<Attribute>)msg.obj;
        TreeViewAdapter adapter;
        Attribute attribute;
        int i, n;

        adapter =  (TreeViewAdapter) recyclerView1.getAdapter();

        n = array.size();

        for(i=0; i<n; i++) {

            attribute = array.remove(0);
            adapter.add(new TreeViewItem(null, attribute.getString(), attribute.getValue(), R.raw.device1, 0, 0));
        }

        adapter.notifyItemRangeInserted(0, n);
        textView1.setText(String.format(Locale.US, "%d devices", n));
    }

    private void onPopulateWithDevice(Message msg) {
        Attribute attribute = (Attribute)msg.obj;
        TreeViewAdapter adapter;
        int k;

        adapter =  (TreeViewAdapter) recyclerView1.getAdapter();
        adapter.add(new TreeViewItem(null, attribute.getString(), attribute.getValue(), R.raw.device1, 0, 0));
        k = adapter.getItemCount() - 1;
        adapter.notifyItemInserted(k);
    }

    private void onDepopulateByDevice(Message msg) {
        long value = (long)msg.obj;
        TreeViewAdapter adapter;
        TreeViewItem item = null;
        int i, n;

        adapter =  (TreeViewAdapter) recyclerView1.getAdapter();
        n = adapter.getItemCount();

        for (i=0; i<n; i++) {
            item = adapter.getItem(i);
            if (item.getHierarchy() == 0 && item.getValue() == value)
                break;
        }

        adapter.remove(item);
        adapter.notifyItemRemoved(i);

    }

    @Override
    public void onItemClick(TreeViewItem item) {

    }

    @Override
    public void onItemLongClick() {

    }
}
