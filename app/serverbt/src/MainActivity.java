
// bluetooth - server side
// Android 13 - API 33

package balikbayan.box.bt_server;

import android.Manifest;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements ListViewAdapter.EventListener {

    private Toolbar toolbar;
    private Handler handler;
    private RecyclerView recyclerView1;
    private Server server;
    private boolean access_granted, running, item_selected;

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

        recyclerView1 = findViewById(R.id.recyclerView1);

        ListViewAdapter adapter = new ListViewAdapter(this, this);

        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        recyclerView1.setAdapter(adapter);

        recyclerView1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

            }
        });

        //  +------------------------------------------------------------------------+
        //  |                          handler message                               |
        //  +------------------------------------------------------------------------+

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what){
                    case Server.MESSAGE:
                    case Client.MESSAGE:                onMessage(msg);             break;
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

        access_granted = running = false;

        requestPermissionBTConnect();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.mnuRun).setEnabled(access_granted && !running);
        menu.findItem(R.id.mnuShutdown).setEnabled(access_granted && running);
        menu.findItem(R.id.mnuClose).setEnabled(access_granted && item_selected);

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
        ListViewAdapter adapter = (ListViewAdapter) recyclerView1.getAdapter();
        Client client = adapter.getItem();
        client.shutdown();
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
        server = new Server(handler, this);

        onServerRun();
    }

    private void sendMessage(Handler handler, int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

    private void onMessage(Message msg) {
    }

    private void onServerRunning(Message msg) {
        toolbar.setTitle("ONLINE");
        running = true;
    }

    private void onServerShuttingDown(Message msg) {
        toolbar.setTitle("OFFLINE");
        running = false;
    }

    private void onCreateClient(Message msg) {
        ListViewAdapter adapter =  (ListViewAdapter) recyclerView1.getAdapter();
        BluetoothSocket socket = (BluetoothSocket) msg.obj;
        Client client = new Client(handler, socket, adapter);
        new Thread(client).start();
    }

    private void onClientRunning(Message msg) {
        Client client, item;
        ListViewAdapter adapter;
        int i, n, pos;

        // idagdag ang bagong client sa list view
        client = (Client)msg.obj;
        adapter = (ListViewAdapter) recyclerView1.getAdapter();
        adapter.add(client);

        pos = adapter.getItemCount() - 1;
        adapter.notifyItemInserted(pos);

        // isend ang mga nasa list view sa kumonektang client
        n = adapter.getItemCount();

        for (i=0; i < n; i++) {

            item = adapter.getItem(i);

            //Log.d("KLGYN", String.format("%s %d", item.getName(), item.getId()));

            client.send(Client.ATTRIBUTE);
            client.send(item.getId());
            client.send(item.getName());
        }

        client.send(Client.DEVICE);

        // isend ang kumonektang client sa mga nasa list view
        for (i=0; i<n; i++) {

            item = adapter.getItem(i);

            // hindi kasali ang kumonektang client
            if (item.getId() == client.getId()) continue;

            //Log.d("KLGYN", String.format("%-30s%20d %-30s%20d", item.getName(), item.getId(), client.getName(), client.getId()));

            item.send(Client.ATTRIBUTE);
            item.send(client.getId());
            item.send(client.getName());

            item.send(Client.DEVICE);

        }
    }

    private void onClientShuttingDown(Message msg) {

        Client client = (Client)msg.obj;
        ListViewAdapter adapter = (ListViewAdapter) recyclerView1.getAdapter();
        Client item;
        int i, k,n;

        // alisin ang client nito sa list view
        k = adapter.getPosition(client);
        adapter.unhighlighItem();
        adapter.remove(k);
        adapter.notifyItemRemoved(k);

        // isend ang nagleave na client sa mga nasa list view para iremove
        n = adapter.getItemCount();

        for (i=0; i < n; i++) {

            item = adapter.getItem(i);

            Log.d("KLGYN", String.format("%40s%20d%40s%20d", item.getName(), item.getId(), client.getName(), client.getId()));

            item.send(Client.LEAVE);
            item.send(client.getId());
        }

        item_selected = false;
    }

    @Override
    public void onItemSelected(Client client) {
        item_selected = true;
    }

    @Override
    public void onItemUnselected() {
        item_selected = false;
    }
}
