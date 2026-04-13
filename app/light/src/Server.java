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
    private final UUID MY_UUID = UUID.fromString("b1899020-c25d-489b-a700-42304d6adbbc");

    public static final int RUNNING         = 1001;
    public static final int SHUTTING_DOWN   = 1003;

    private BluetoothServerSocket server_socket = null;
    private MyDeviceAdminService service;
    protected Client client;
    private boolean running;

    public Server(MyDeviceAdminService service) {
        this.service = service;                   // ito ay gagamitin para iistop ang service
        running = false;
    }

    public void shutdown() {
        try {
            server_socket.close();
        } catch (IOException e) {
            Log.d("KLGYN", e.toString());
        }
    }
    public boolean isRunning() {
        return running;
    }

    private void sendMessage(Handler handler, int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

    private void doDelay(long millis) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(millis);
                } catch (InterruptedException ignore) {
                }

                Handler handler = service.getHandler();

                if (handler != null)
                    sendMessage(handler, RUNNING, null);
            }
        }).start();
    }

    @Override
    public void run() {
        BluetoothManager manager;
        BluetoothAdapter adapter;
        BluetoothSocket socket = null;
        Context context = (Context) service;
        Handler handler;

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        doDelay(500);

        running = true;

        try {

            manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            adapter = manager.getAdapter();

            server_socket = adapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);

            while(true) {

                socket = server_socket.accept();

                if (socket != null) {

                    handler = service.getHandler();

                    client = new Client(context, socket, handler);
                    new Thread(client).start();

                }
            }
        } catch (IOException | SecurityException e) {
            Log.d("KLGYN", e.toString());
        }

        shutdown();

        running = false;

        service.stopSelf();

        handler = service.getHandler();

        if (handler != null)
            sendMessage(handler, SHUTTING_DOWN, null);

        Log.d("KLGYN", "server thread stop");

    }
}
