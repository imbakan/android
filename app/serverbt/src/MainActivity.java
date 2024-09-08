
// client-server model
// server side
// Bluetooth
// Android 6 Api 23

package balikbayan.box.serverbt06;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ListViewEventListener {

    private RecyclerView recyclerView1;
    private EditText editText1;

    private Handler handler;
    private boolean access_granted, server_running, item_selected;

    private Server server;

    BluetoothContract contract = new BluetoothContract();

    ActivityResultLauncher<Void> launcher = registerForActivityResult(contract, new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            access_granted = o;
            if (o) server = new Server(handler, getApplicationContext());
        }
    });

    OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            Toast.makeText(getApplicationContext(), "TUMATAKBO PA ANG SERVER O CLIENT", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String str = getResources().getString(R.string.app_name);
        setTitle(str + " - offline");

        item_selected = server_running = false;

        editText1 = findViewById(R.id.editText1);
        editText1.setKeyListener(null);

        recyclerView1 = findViewById(R.id.recyclerView1);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        ListViewAdapter adapter = new ListViewAdapter(this, (ListViewEventListener) this);
        recyclerView1.setAdapter(adapter);

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        //  +------------------------------------------------------------------------+
        //  |                          handler message                               |
        //  +------------------------------------------------------------------------+

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what){
                    case Server.LOG_MESSAGE:
                    case Client.LOG_MESSAGE:            onLogMessage(msg);          break;
                    case Server.RUNNING:                onServerRunning(msg);       break;
                    case Server.SHUTTING_DOWN:          onServerShuttingDown(msg);  break;
                    case Server.CREATE_CLIENT:          onCreateClient(msg);        break;
                    case Client.RUNNING:                onClientRunning(msg);       break;
                    case Client.SHUTTING_DOWN:          onClientShuttingDown(msg);  break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };

        //  +------------------------------------------------------------------------+
        //  |                         request permission                             |
        //  +------------------------------------------------------------------------+

        requestPermissionBluetooth();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.mnuRun).setEnabled(access_granted && !server_running);
        menu.findItem(R.id.mnuShutdown).setEnabled(access_granted && server_running);
        menu.findItem(R.id.mnuClose).setEnabled(access_granted && item_selected);

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
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    // irun ang server
    private void onServerRun() {
        new Thread(server).start();
    }

    // ishutdown ang server
    private void onServerShutdown() {
        server.shutdown();
    }

    // iclose ang selected client
    private void onClientClose() {
        ListViewAdapter adapter =  (ListViewAdapter) recyclerView1.getAdapter();
        Client client = adapter.getSelectedItem();
        client.shutdown();
    }

    // isend sa log output
    private void sendMessage(int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

    private void requestPermissionBluetooth() {

        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adaptor = manager.getAdapter();

        if (adaptor != null && adaptor.isEnabled()) {
            access_granted = true;
            server = new Server(handler, this);
        } else {
            launcher.launch(null);
        }
    }

    // logging activity ng server
    private void onLogMessage(Message msg) {
        String str1, str2, str3;

        str1 = editText1.getText().toString();
        str2 = (String) msg.obj;
        str3 = str1 + "\n" + str2;
        editText1.setText(str3);
    }

    // running na ang server indicator at iwas backpressed
    private void onServerRunning(Message msg) {
        server_running = true;
        String str = getResources().getString(R.string.app_name);
        setTitle(str + " - online");

        onBackPressedCallback.setEnabled(server_running);
    }

    // shutdown na ang server indicator at pede backpressed
    private void onServerShuttingDown(Message msg) {
        server_running = false;
        String str = getResources().getString(R.string.app_name);
        setTitle(str + " - offline");

        ListViewAdapter adapter = (ListViewAdapter)recyclerView1.getAdapter();
        onBackPressedCallback.setEnabled(adapter.getItemCount() > 0);
    }

    // gumawa ng thread para sa kumonektang client
    private void onCreateClient(Message msg) {
        BluetoothSocket socket = (BluetoothSocket) msg.obj;
        ListViewAdapter adapter =  (ListViewAdapter) recyclerView1.getAdapter();
        Client client = new Client(handler, socket, adapter);
        new Thread(client).start();
    }

    //
    private void onClientRunning(Message msg) {
        Client client = (Client) msg.obj;
        Client item;
        ListViewAdapter adapter;
        int i, n;

        // isend ang id number sa kumonektang client
        client.send(Client.CONFIRM);
        client.send(client.getId());

        adapter =  (ListViewAdapter) recyclerView1.getAdapter();
        n = adapter.getItemCount();

        if (n > 0) {

            // isend ang mga pangalan ng client na nasa recycler view sa kumonektang client
            for (i=0; i<n; i++) {

                item = adapter.getItem(i);

                client.send(Client.ATTRIBUTES);
                client.send(item.getName());
                client.send(item.getId());
            }

            client.send(Client.REPLY_DEVICES);

            // isend ang pangalan ng kumonektang client sa mga client na nasa recycler view
            for (i=0; i<n; i++) {

                item = adapter.getItem(i);

                item.send(Client.REPLY_DEVICE);
                item.send(client.getName());
                item.send(client.getId());
            }
        }

        // iadd ang kumonektang client sa recycler view
        adapter.add(client);
        adapter.notifyItemInserted(n);

        sendMessage(Client.LOG_MESSAGE, String.format("The client thread 0x%x has started.", client.getId()));
    }

    //
    private void onClientShuttingDown(Message msg) {
        Client client = (Client)msg.obj;
        ListViewAdapter adapter;
        Client item;
        int i, n;

        // iremove ang client na 'to sa recycler view
        adapter =  (ListViewAdapter) recyclerView1.getAdapter();
        i = adapter.remove(client);
        adapter.notifyItemRemoved(i);

        sendMessage(Client.LOG_MESSAGE, String.format("The client thread 0x%x has exited.", client.getId()));

        // ipaalam sa mga client na nasa recycler view na wala na ang client na 'to
        n = adapter.getItemCount();

        for (i=0; i<n; i++) {

            item = adapter.getItem(i);

            item.send(Client.LEAVE);
            item.send(client.getId());
        }

        item_selected = false;

        n = adapter.getItemCount();
        onBackPressedCallback.setEnabled(server_running || n > 0);

    }

    @Override
    public void onItemSelected() {
        item_selected = true;
    }

    @Override
    public void onItemUnselected() {
        item_selected = false;
    }
}
