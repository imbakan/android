package balikbayan.box.server_lan;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ListViewAdapter.OnEventListener {

    private final int port = 54105;

    private RecyclerView recyclerView1;
    private EditText editText1;

    private Toolbar toolbar;
    private Handler handler;

    private Server server;
    private boolean running, selected;

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

        // ILAGAY ITO SA INITIALIZE
        server = new Server(handler, port);

        String ip = getIPAddress();
        sendMessage(Server.LOG_MESSAGE, String.format("Ang IP Address ng server ay %s", ip));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.mnuRun).setEnabled(!running);
        menu.findItem(R.id.mnuShutdown).setEnabled(running);
        menu.findItem(R.id.mnuClose).setEnabled(selected);

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

    private String getIPAddress() {
        List<NetworkInterface> interfaces;
        List<InetAddress> addresses;
        String str, ip;
        boolean eureka;

        ip = "DI MAKUHA IP ADDRESS";
        eureka = false;

        try {
            interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface ni : interfaces) {

                addresses = Collections.list(ni.getInetAddresses());

                for (InetAddress ia : addresses) {

                    str = ia.getHostAddress();

                    if (!ia.isLoopbackAddress())
                        if (str.indexOf(':') < 0) {
                            eureka = true;
                            ip = str;
                        }

                    if (eureka) break;
                }

                if (eureka) break;
            }

        } catch (SocketException ignore) {
        }

        return ip;
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
        Socket socket = (Socket) msg.obj;
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
}
