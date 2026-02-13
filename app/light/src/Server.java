package balikbayan.box.light;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class Server implements Runnable {

    private final String NAME = "Bluetooth Server";
    private final UUID MY_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    public static final int RUNNING         = 1002;
    public static final int SHUTTING_DOWN   = 1003;

    private BluetoothServerSocket server_socket = null;
    private Context context;
    private Handler handler;
    private Client client;
    private boolean running;
    private int state;
    private MyDeviceAdminService service;

    public Server(MyDeviceAdminService service) {
        client = new Client();
        running = false;
        this.service = service;
    }

    public void init(Context context, Handler handler, int state) {
        this.context = context;
        this.handler = handler;
        this.state = state;
    }

    public void shutdown() {
        try {
            server_socket.close();
        } catch (IOException e) {
            Log.d("KLGYN", e.toString());
        }
    }

    public void shutdownClient() {
        client.shutdown();
    }

    public void send(int value) {
        client.send(value);
    }

    private void sendMessage(Handler handler, int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

    public void saveCurrentState(int state) {
        this.state = state;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isClientRunning() {
        return client.isRunning();
    }

    @Override
    public void run() {
        BluetoothManager manager;
        BluetoothAdapter adapter;
        BluetoothSocket socket = null;

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        sendMessage(handler, RUNNING, null);

        running = true;

        try {

            manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            adapter = manager.getAdapter();

            server_socket = adapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);

            while(true) {

                socket = server_socket.accept();

                if (socket != null) {

                    if (client.isRunning()) client.shutdown();

                    client.init(socket, handler, state);
                    new Thread(client).start();

                }
            }
        } catch (IOException | SecurityException e) {
            Log.d("KLGYN", e.toString());
        }

        shutdown();

        running = false;

        sendMessage(handler, SHUTTING_DOWN, null);

        service.stopSelf();
    }
}
