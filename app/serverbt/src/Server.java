package balikbayan.box.server_bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.UUID;

public class Server implements Runnable {

    private final String NAME = "Bluetooth Server";
    private final UUID MY_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    public static final int LOG_MESSAGE     = 1001;
    public static final int RUNNING         = 1002;
    public static final int SHUTTING_DOWN   = 1003;
    public static final int CREATE_CLIENT   = 1004;

    private BluetoothServerSocket server_socket = null;
    private Handler handler;
    private Context context;

    public Server(Handler handler, Context context) {
        this.handler = handler;
        this.context = context;
    }

    public void shutdown() {
        try {
            server_socket.close();
        } catch (IOException e) {
            sendMessage(LOG_MESSAGE, e.toString());
        }
    }

    private void sendMessage(int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

    @Override
    public void run() {
        BluetoothManager manager;
        BluetoothAdapter adapter;
        BluetoothSocket socket = null;

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        //sendMessage(LOG_MESSAGE, "Server thread has started.");
        sendMessage(RUNNING, "Server thread has started.");

        try {

            manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            adapter = manager.getAdapter();
            server_socket = adapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);

            while(true) {

                socket = server_socket.accept();

                if (socket != null)
                    sendMessage(CREATE_CLIENT, socket);
            }

        } catch (IOException | SecurityException e) {
            sendMessage(LOG_MESSAGE, e.toString());
        }

        shutdown();

        //sendMessage(LOG_MESSAGE, "Server thread has exited.");
        sendMessage(SHUTTING_DOWN, "Server thread has exited.");
    }
}
