package balikbayan.box.server_bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.Socket;

public class MainActivity extends AppCompatActivity implements ListViewAdapter.OnEventListener {

    private RecyclerView recyclerView1;
    private EditText editText1;

    private Toolbar toolbar;
    private Handler handler;

    private Server server;
    private boolean access_granted, running, selected;

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
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("OFFLINE");
        setSupportActionBar(toolbar);

        editText1 = findViewById(R.id.editText1);
        editText1.setKeyListener(null);

        ListViewAdapter adapter = new ListViewAdapter(this, this);
        recyclerView1 = findViewById(R.id.recyclerView1);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        recyclerView1.setAdapter(adapter);

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
        //  |                            permission                                  |
        //  +------------------------------------------------------------------------+
        // access order:
        // 1. nearby device
        // 2. turn bluetooth on/off

        requestPermissionBTConnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.mnuRun).setEnabled(access_granted && !running);
        menu.findItem(R.id.mnuShutdown).setEnabled(access_granted && running);
        menu.findItem(R.id.mnuClose).setEnabled(access_granted && selected);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.mnuRun)
            onServerRun();
        else if(id == R.id.mnuShutdown)
            onServerShutdown();
        else if(id == R.id.mnuClose)
            onClientClose();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    private void onServerRun() {
        new Thread(server).start();
    }

    private void onServerShutdown() {
        server.shutdown();
    }

    private void onClientClose() {
        ListViewAdapter adapter;
        Client client;

        adapter = (ListViewAdapter) recyclerView1.getAdapter();
        client = adapter.getItem();
        client.shutdown();
    }

    private void sendMessage(int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

    private void onLogMessage(Message msg) {
        String str1, str2, str3;

        str1 = editText1.getText().toString();
        str2 = (String) msg.obj;
        str3 = str1 + "\n" + str2;
        editText1.setText(str3);
    }

    private void onServerRunning(Message msg) {
        String str = (String) msg.obj;
        sendMessage(Server.LOG_MESSAGE, str);
        toolbar.setTitle("ONLINE");
        running = true;
    }

    private void onServerShuttingDown(Message msg) {
        String str = (String) msg.obj;
        sendMessage(Server.LOG_MESSAGE, str);
        toolbar.setTitle("OFFLINE");
        running = false;
    }

    private void onCreateClient(Message msg) {
        BluetoothSocket socket = (BluetoothSocket) msg.obj;
        Client client = new Client(handler, socket);
        new Thread(client).start();
    }

    private void onClientRunning(Message msg) {
        Client client1 = (Client) msg.obj;
        ListViewAdapter adapter;
        int k, n;

        // ang server ay nag-aasign ng id sa kumonektang client
        // isend 'to sa client
        new Thread(new Runnable() {
            @Override
            public void run() {
                client1.send(Client.CONNECTED);
                client1.send(client1.getId());
            }
        }).start();

        adapter = (ListViewAdapter) recyclerView1.getAdapter();

        // isend ang mga pangalan ng client na nasa list view sa kumonektang client
        n = adapter.getItemCount();

        if (n > 0) {

            // isend ang mga pangalan ng client na nasa list view sa kumonektang client
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Client client2;
                    int i;

                    for (i = 0; i < n; i++) {

                        client2 = adapter.getItem(i);

                        client1.send(Client.ATTRIBUTES);
                        client1.send(client2.getName());
                        client1.send(client2.getId());
                    }

                    client1.send(Client.REPLY_DEVICE);
                }
            }).start();

            // isend ang pangalan ng kumonektang client sa mga client na nasa list view
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Client client2;
                    int i;

                    for (i = 0; i < n; i++) {

                        client2 = adapter.getItem(i);

                        client2.send(Client.ATTRIBUTES);
                        client2.send(client1.getName());
                        client2.send(client1.getId());
                        client2.send(Client.REPLY_DEVICE);
                    }
                }
            }).start();
        }

        // iadd ang kumonektang client sa list view
        adapter.add(client1);
        k = adapter.getItemCount() - 1;
        adapter.notifyItemInserted(k);
    }

    private void onClientShuttingDown(Message msg) {
        Client client1 = (Client) msg.obj;
        ListViewAdapter adapter;
        int k;

        // iremove ang client na 'to sa list view
        adapter = (ListViewAdapter) recyclerView1.getAdapter();
        k = adapter.getPosition(client1);
        adapter.remove(k);
        adapter.notifyItemRemoved(k);

        selected = false;

        // ipaalam sa mga client na nasa list view na wala na ang client na 'to
        new Thread(new Runnable() {
            @Override
            public void run() {

                Client client2;
                int i, n;

                n = adapter.getItemCount();

                for (i = 0; i < n; i++) {

                    client2 = adapter.getItem(i);

                    client2.send(Client.LEAVE);
                    client2.send(client1.getId());
                }
            }
        }).start();
    }

    @Override
    public void onItemSelected() {
        selected = true;
    }

    @Override
    public void onItemUnselected() {
        selected = false;
    }

    private void requestPermissionBTConnect() {

        if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
            requestPermissionBTEnability();
        else
            launcher1.launch(android.Manifest.permission.BLUETOOTH_CONNECT);
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
        server = new Server(handler, this);
    }
}
