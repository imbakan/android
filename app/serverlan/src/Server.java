package balikbayan.box.server_lan;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

    public static final int LOG_MESSAGE     = 1001;
    public static final int RUNNING         = 1002;
    public static final int SHUTTING_DOWN   = 1003;
    public static final int CREATE_CLIENT   = 1004;

    private ServerSocket server_socket = null;
    private Handler handler;
    private int port;

    public Server(Handler handler, int port) {
        this.handler = handler;
        this.port = port;
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

        Socket socket = null;

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        //sendMessage(LOG_MESSAGE, "Server thread has started.");
        sendMessage(RUNNING, "Server thread has started.");

        try {

            server_socket = new ServerSocket(port);

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
