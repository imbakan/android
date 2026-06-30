
// receive ng server
//     +---------------+
//     |   Client Id   |
//     +---------------+
//     |      Size     |
//     +---------------+
//     |      Data     |
//     +---------------+

// send ng server
//     +---------------+
//     |   Client Id   |
//     +---------------+
//     |      Data     |
//     +---------------+

package balikbayan.box.bt_server;

import android.bluetooth.BluetoothSocket;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

public class Client implements Runnable {

    public static final int MESSAGE         = 2001;
    public static final int RUNNING         = 2002;
    public static final int SHUTTING_DOWN   = 2003;

    private final int BUFFER_SIZE = 32768;

    private final int NEED_DATA      = 3001;
    private final int SET_ORDER      = 3002;
    private final int GET_INTEGER    = 3003;
    private final int GET_LONG       = 3004;
    private final int GET_STRING     = 3005;

    private final int JOIN = 5003;
    private final int CLIENT_ID = 5004;
    public static final int LEAVE = 5005;

    public static final int ATTRIBUTE = 6001;
    public static final int DEVICE = 6002;

    private final int FORWARD = 7001;
    private final int GET_DATA_SIZE = 7002;
    private final int FORWARD_SOURCE = 7003;

    private BluetoothSocket socket;
    private OutputStream outputstream;
    private Handler handler;
    private ListViewAdapter adapter;
    private String ip, name, date;
    private long id;

    public Client(Handler handler, BluetoothSocket socket, ListViewAdapter adapter) {
        this.handler = handler;
        this.socket = socket;
        this.adapter = adapter;
    }

    public void shutdown() {
        try {
            socket.close();
        } catch (IOException e) {
            sendMessage(MESSAGE, e.toString());
        }
    }

    public void send(int value) {
        ByteBuffer bb;
        byte[] b;

        try {
            bb = ByteBuffer.allocate(Integer.BYTES);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.putInt(value);
            b = bb.array();
            outputstream.write(b);
        } catch (IOException e){
            sendMessage(MESSAGE, e.toString());
        }
    }

    public void send(long value) {
        ByteBuffer bb;
        byte[] b;

        try {
            bb = ByteBuffer.allocate(Long.BYTES);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.putLong(value);
            b = bb.array();
            outputstream.write(b);
        } catch (IOException e){
            sendMessage(MESSAGE, e.toString());
        }
    }

    public void send(String str) {
        byte[] b;

        send(str.length());

        try {
            b = str.getBytes(StandardCharsets.UTF_16LE);
            outputstream.write(b);
        } catch (IOException e) {
            sendMessage(MESSAGE, e.toString());
        }
    }

    public void send(byte[] buffer, int offset, int size) {

        try {
            outputstream.write(buffer, offset, size);
        } catch (IOException e){
            sendMessage(MESSAGE, e.toString());
        }
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public long getId() {
        return id;
    }

    private void onSetOrder(boolean[] need_data, ArrayList<Integer> order, int[] next, int msg) {

        need_data[0] = false;

        switch (msg) {
            case JOIN:
                order.add(GET_INTEGER);
                order.add(GET_STRING);
                order.add(RUNNING);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case FORWARD:
                order.add(GET_LONG);
                order.add(GET_LONG);
                order.add(GET_DATA_SIZE);
                order.add(FORWARD_SOURCE);
                order.add(FORWARD);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            default:
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
        }

        next[0] = order.remove(0);
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
        bb.order(ByteOrder.LITTLE_ENDIAN);
        value[0] = bb.getInt();

        index[0] += req_size;
        next[0] = order.remove(0);
    }

    private void onGetLong(byte[] buffer, int[] index, ArrayList<Integer> order, int[] next, ArrayList<Long> array) {
        ByteBuffer bb;
        int avail_size, req_size;

        avail_size = index[1] - index[0];
        req_size = Long.BYTES;

        if (avail_size < req_size) {
            order.add(0, GET_LONG);
            next[0] = NEED_DATA;
            return;
        }

        bb = ByteBuffer.wrap(buffer, index[0], req_size);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        //value[0] = bb.getLong();
        array.add(bb.getLong());

        index[0] += req_size;
        next[0] = order.remove(0);
    }

    private void onGetString(byte[] buffer, int[] index, ArrayList<Integer> order, int[] next, int len, String[] str) {
        int avail_size, req_size;

        avail_size = index[1] - index[0];
        req_size = len * Short.BYTES;

        if (avail_size < req_size) {
            order.add(0, GET_STRING);
            next[0] = NEED_DATA;
            return;
        }

        str[0] = new String(buffer, index[0], req_size, StandardCharsets.UTF_16LE);

        index[0] += req_size;
        next[0] = order.remove(0);
    }

    private void onRunning(ArrayList<Integer> order, int[] next, String str) {
        Calendar c;
        SimpleDateFormat sdf;
        long now;

        name = str;

        //Log.d("KLGYN", String.format("client on running %s", name));

        c = Calendar.getInstance();
        now = c.getTimeInMillis();
        sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
        date = sdf.format(now);

        sendMessage(RUNNING, this);

        next[0] = order.remove(0);
    }

    private void onGetDataSize(ArrayList<Integer> order, int[] next, ArrayList<Long> array, long[] size, Client[] client) {
        Client item;
        long id;
        int i, n;

        id = array.remove(0);
        size[0] = array.remove(0);

        //Log.d("KLGYN", String.format("client forward %d %d", id ,size[o));

        // kunin ang destination client base sa id number
        n = adapter.getItemCount();

        Log.d("KLGYN", String.format("list view count %d", n));

        for (i=0; i<n; i++) {

            item = adapter.getItem(i);

            Log.d("KLGYN", String.format("list view %s %d", item.getName(), item.getId()));

            if (id == item.getId()) {
                client[0] = item;
                break;
            }
        }

        next[0] = order.remove(0);
    }

    // iforward ang data na galing sa client
    private void onForward(byte[] buffer, int[] index, int[] buffer_size, boolean[] need_data, ArrayList<Integer> order, int[] next, Client[] client, long[] data_size) {
        long avail_size;

        avail_size = index[1] - index[0];

        if (avail_size < data_size[0]) {

            if (avail_size > 0)
                client[0].send(buffer, index[0], (int) avail_size);
            index[0] = index[1] = 0;
            buffer_size[0] = BUFFER_SIZE;
            data_size[0] -= avail_size;
            need_data[0] = true;

        } else {

            client[0].send(buffer, index[0], (int) data_size[0]);
            index[0] += (int) data_size[0];
            next[0] = order.remove(0);
        }
    }

    private void onForwardSource(ArrayList<Integer> order, int[] next, Client[] client) {

        Log.d("KLGYN", String.format("on forward source client id %d", id));

        client[0].send(CLIENT_ID);
        client[0].send(id);

        next[0] = order.remove(0);
    }

    private void sendMessage(int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

    private void printOutput(int cmd) {
        switch (cmd) {
            case SET_ORDER:						Log.d("KLGYN", "SET_ORDER"); break;
            case NEED_DATA:						Log.d("KLGYN", "NEED_DATA"); break;
            case GET_INTEGER:					Log.d("KLGYN", "GET_INTEGER"); break;
            case GET_LONG:					    Log.d("KLGYN", "GET_LONG"); break;
            case GET_STRING:					Log.d("KLGYN", "GET_STRING"); break;
            case GET_DATA_SIZE:					Log.d("KLGYN", "GET_DATA_SIZE"); break;
            case FORWARD:		                Log.d("KLGYN", "FORWARD"); break;
            case FORWARD_SOURCE:		        Log.d("KLGYN", "FORWARD_SOURCE"); break;
            case RUNNING:				        Log.d("KLGYN", "RUNNING"); break;
            default:                            Log.d("KLGYN", "**************");
        }
    }

    @Override
    public void run() {
        InputStream inputstream;
        ArrayList<Integer> order = new ArrayList<>();
        ArrayList<Long> array_l = new ArrayList<>();
        ArrayList<String> array_s = new ArrayList<>();
        Client[] client = new Client[1];
        int[] buffer_size = new int[1];
        int[] index = new int[2];
        int[] next = new int[1];
        int[] ivalue = new int[1];
        long[] size = new long[1];
        String[] str = new String[1];
        byte[] buffer = new byte[BUFFER_SIZE];
        boolean[] need_data = new boolean[1];
        int count;

        Log.d("KLGYN", "client thread has started.");

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        id = SystemClock.elapsedRealtime();

        try {

            buffer_size[0] = BUFFER_SIZE;
            count = 0;
            index[0] = index[1] = 0;
            need_data[0] = true;
            next[0] = GET_INTEGER;
            order.add(SET_ORDER);

            inputstream = socket.getInputStream();
            outputstream = socket.getOutputStream();

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

                    //Log.d("KLGYN", String.format("%d bytes", count));

                    if (count <= 0) break;

                    index[1] += count;
                    need_data[0] = false;
                }

                //printOutput(next[0]);

                switch (next[0]) {
                    case SET_ORDER:						onSetOrder(need_data, order, next, ivalue[0]);					                break;
                    case NEED_DATA:						onNeedData(buffer, index, buffer_size, need_data, order, next);                 break;
                    case GET_INTEGER:					onGetInteger(buffer, index, order, next, ivalue);				                break;
                    case GET_LONG:					    onGetLong(buffer, index, order, next, array_l);				                    break;
                    case GET_STRING:					onGetString(buffer, index, order, next, ivalue[0], str);		                break;
                    case GET_DATA_SIZE:					onGetDataSize(order, next, array_l, size, client);		                        break;
                    case FORWARD:		                onForward(buffer, index, buffer_size, need_data, order, next, client, size);    break;
                    case FORWARD_SOURCE:		        onForwardSource(order, next, client);                                           break;
                    case RUNNING:				        onRunning(order, next, str[0]);		                                            break;
                }
            }

        } catch (IOException | SecurityException e) {
            Log.d("KLGYN", e.toString());
            sendMessage(MESSAGE, e.toString());
        }

        shutdown();

        sendMessage(SHUTTING_DOWN, this);

        Log.d("KLGYN", "Client thread has exited.");

    }
}
