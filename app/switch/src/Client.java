package balikbayan.box.aswitch;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

public class Client implements Runnable {

    private final UUID MY_UUID = UUID.fromString("b1899020-c25d-489b-a700-42304d6adbbc");

    private final int BUFFER_SIZE = 1024;

    public static final int CONNECTING      = 2002;
    public static final int RUNNING         = 2003;
    public static final int SHUTTING_DOWN   = 2004;

    private final int NEED_DATA     = 3101;
    private final int SET_ORDER     = 3102;
    private final int GET_INTEGER   = 3103;

    private final int FIRST_RUN      = 4001;

    public static final int LIGHT_ON        = 5001;
    public static final int LIGHT_OFF       = 5002;
    public static final int CURRENT_STATE   = 5003;

    public static final int LIGHT_COLOR = 6001;

    private BluetoothSocket socket;
    private BluetoothDevice device;
    private OutputStream outputstream;
    private Handler handler;
    private boolean running;

    public Client(Handler handler, BluetoothDevice device) {
        this.handler = handler;
        this.device = device;
        running = false;
    }

    public void shutdown() {
        try {
            socket.close();
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

    public void send(int value) {
        ByteBuffer bb;
        byte[] b;

        try {
            bb = ByteBuffer.allocate(Integer.BYTES);
            bb.putInt(value);
            b = bb.array();
            outputstream.write(b);
        } catch (IOException e){
            Log.d("KLGYN", e.toString());
        }
    }

    //   |<--------------------------------- BUFFER_SIZE ------------------------------->|
    //             |<------------ on hand ----------->|<---------- buffer size --------->|
    //   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
    //   |  0 |  1 |  2 |  3 |  4 |  5 |  6 |  7 |  8 |  9 | 10 | 11 | 12 | 13 | 14 | 15 |
    //   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
    //                ^                                  ^
    //                |                                  |
    //             index[0]                           index[1]

    private void onNeedData(byte[] buffer, int[] index, int[] buffer_size, boolean[] need_data, ArrayList<Integer> order, int[] next) {
        int i, k, n;

        n = index[1] - index[0];
        k = index[0];
        for (i = 0; i < n; i++)
            buffer[i] = buffer[k++];

        index[0] = 0;
        index[1] = n;

        buffer_size[0] = BUFFER_SIZE - index[1];
        need_data[0] = true;

        next[0] = order.remove(0);
    }

    private void onSetOrder(boolean[] need_data, ArrayList<Integer> order, int[] next, int task) {

        need_data[0] = false;

        switch (task) {
            case CURRENT_STATE:
                order.add(GET_INTEGER);
                order.add(CURRENT_STATE);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case LIGHT_ON:
                order.add(LIGHT_ON);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case LIGHT_OFF:
                order.add(LIGHT_OFF);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            default:
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
        }

        next[0] = order.remove(0);
    }

    private void onGetInteger(byte[] buffer, int[] index, ArrayList<Integer> order, int[] next, int[] value) {
        ByteBuffer bb;
        int avail_size, req_size;

        avail_size = index[1] - index[0];
        req_size = Integer.BYTES;

        if (avail_size < req_size) {
            order.add(0, GET_INTEGER);
            next[0] = NEED_DATA;
            return;
        }

        bb = ByteBuffer.wrap(buffer, index[0], req_size);
        value[0] = bb.getInt();

        index[0] += req_size;
        next[0] = order.remove(0);
    }

    private void onCurrentState(ArrayList<Integer> order, int[] next, int value) {
        sendMessage(handler, CURRENT_STATE, value);
        next[0] = order.remove(0);
    }

    private void outputTask(int task) {
        String str;

        switch (task) {
            case SET_ORDER: str = "SET_ORDER"; break;
            case NEED_DATA: str = "NEED_DATA"; break;
            case GET_INTEGER: str = "GET_INTEGER"; break;
            case CURRENT_STATE: str = "CURRENT_STATE"; break;
            case LIGHT_ON: str = "LIGHT_ON"; break;
            case LIGHT_OFF: str = "LIGHT_OFF"; break;
            default:  str = "EWAN";
        }

        Log.d("KLGYN", str);
    }

    @Override
    public void run() {
        InputStream inputstream;
        ArrayList<Integer> order = new ArrayList<>();
        int[] buffer_size = new int[1];
        int[] index = new int[2];
        int[] next = new int[1];
        int[] value = new int[1];
        byte[] buffer = new byte[BUFFER_SIZE];
        boolean[] need_data = new boolean[1];
        int count;

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        sendMessage(handler, CONNECTING, null);

        try {

            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();

            inputstream = socket.getInputStream();
            outputstream = socket.getOutputStream();

            sendMessage(handler, RUNNING, null);

            running = true;

            buffer_size[0] = BUFFER_SIZE;
            count = 0;
            index[0] = index[1] = 0;
            need_data[0] = true;
            next[0] = GET_INTEGER;
            order.add(SET_ORDER);

            while (true) {

                //   |<--------------------------------- BUFFER_SIZE ------------------------------->|
                //                                                |<---------- buffer size --------->|
                //             |<------------ on hand ----------->|<------ received------->|
                //   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
                //   |  0 |  1 |  2 |  3 |  4 |  5 |  6 |  7 |  8 |  9 | 10 | 11 | 12 | 13 | 14 | 15 |
                //   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
                //                ^                                  ^
                //                |                                  |
                //             index[0]                           index[1]

                if (need_data[0]) {

                    count = inputstream.read(buffer, index[1], buffer_size[0]);

                    Log.d("KLGYN", String.format("%10d bytes", count));

                    if (count <= 0) break;

                    index[1] += count;
                    need_data[0] = false;
                }

                //Log.d("KLGYN", String.format("%10d", next[0]));
                outputTask(next[0]);

                switch (next[0]) {
                    case SET_ORDER:		onSetOrder(need_data, order, next, value[0]);					break;
                    case NEED_DATA:		onNeedData(buffer, index, buffer_size, need_data, order, next); break;
                    case GET_INTEGER:	onGetInteger(buffer, index, order, next, value);				break;
                    case CURRENT_STATE: onCurrentState(order, next, value[0]);							break;
                }

            }

        } catch (IOException | SecurityException e) {
            Log.d("KLGYN", e.toString());
        }

        shutdown();

        running = false;

        sendMessage(handler, SHUTTING_DOWN, null);
    }
}
