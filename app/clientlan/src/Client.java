package balikbayan.box.client_lan;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Client implements Runnable {

    public static final int LOG_MESSAGE     = 2001;
    public static final int CONNECTING      = 2002;
    public static final int RUNNING         = 2003;
    public static final int SHUTTING_DOWN   = 2004;

    private final int BUFFER_SIZE = 32768;

    private final int NEED_DATA      = 3001;
    private final int SET_ORDER      = 3002;
    private final int GET_INTEGER    = 3003;
    private final int GET_LONG       = 3004;
    private final int GET_STRING     = 3005;

    private final int ATTRIBUTES         = 4001;
    private final int STRINGS            = 4002;
    private final int COLLECT_STRING     = 4003;
    private final int COLLECT_ATTRIBUTE  = 4004;

    private final int FIRST_RUN      = 5001;
    private final int CONNECTED      = 5002;
    private final int JOIN           = 5003;
    private final int LEAVE          = 5004;
    private final int FORWARD        = 5005;
    private final int RESEND         = 5006;
    private final int CLIENT_RUNNING = 5007;

    private final int REQUEST_DRIVE      = 6001;
    private final int REQUEST_DIRECTORY  = 6002;
    private final int REQUEST_FILE       = 6003;
    private final int REQUEST_CONTENT    = 6004;

    private final int REPLY_DEVICE       = 7001;
    private final int REPLY_DRIVE        = 7002;
    private final int REPLY_DIRECTORY    = 7003;
    private final int REPLY_FILE         = 7004;
    private final int REPLY_CONTENT_1    = 7005;
    private final int REPLY_CONTENT_2    = 7006;
    private final int REPLY_CONTENT_3    = 7007;

    private final int ENUMERATE_DRIVES       = 8001;
    private final int ENUMERATE_DIRECTORIES  = 8002;
    private final int ENUMERATE_FILES        = 8003;
    private final int ENUMERATE_CONTENTS     = 8004;

    public static final int DEPOPULATE_BY_DEVICE = 9001;
    public static final int POPULATE_WITH_DEVICE = 9002;
    private final int POPULATE_WITH_DRIVES       = 9003;
    private final int POPULATE_WITH_DIRECTORIES  = 9004;
    private final int POPULATE_WITH_FILES        = 9005;
    private final int POPULATE_WITH_CONTENT_1    = 9006;
    private final int POPULATE_WITH_CONTENT_2    = 9007;
    private final int POPULATE_WITH_CONTENT_3    = 9008;

    private Socket socket;
    private OutputStream outputstream;
    private Handler handler;
    private int port;
    private long id;
    private String ip, name;

    public Client(Handler handler, int port, String name) {
        this.handler = handler;
        this.port = port;
        this.name = name;
    }

    public void connect(String ip) {
        this.ip = ip;
    }

    public void shutdown() {
        try {
            socket.close();
        } catch (IOException e) {
            sendMessage(LOG_MESSAGE, e.toString());
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
            sendMessage(LOG_MESSAGE, e.toString());
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
            sendMessage(LOG_MESSAGE, e.toString());
        }
    }

    public void send(String str) {
        byte[] b;

        send(str.length());

        try {
            b = str.getBytes(StandardCharsets.UTF_16LE);
            outputstream.write(b);
        } catch (IOException e) {
            sendMessage(LOG_MESSAGE, e.toString());
        }
    }

    private void sendMessage(int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
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

    private void onSetOrder(boolean[] need_data, ArrayList<Integer> order, int[] next, int msg) {

        need_data[0] = false;

        switch (msg) {
            case STRINGS:
                order.add(GET_INTEGER);
                order.add(GET_STRING);
                order.add(COLLECT_STRING);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case ATTRIBUTES:
                order.add(GET_INTEGER);
                order.add(GET_STRING);
                order.add(GET_LONG);
                order.add(COLLECT_ATTRIBUTE);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case LEAVE:
                order.add(GET_LONG);
                order.add(DEPOPULATE_BY_DEVICE);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case CONNECTED:
                order.add(GET_LONG);
                order.add(CONNECTED);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case REPLY_DEVICE:
                order.add(POPULATE_WITH_DEVICE);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case REPLY_DRIVE:
                order.add(POPULATE_WITH_DRIVES);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case REPLY_DIRECTORY:
                order.add(SET_ORDER);
                break;

            case REPLY_FILE:
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case REQUEST_DRIVE:
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case REQUEST_DIRECTORY:
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case REQUEST_FILE:
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case REQUEST_CONTENT:
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
        bb.order(ByteOrder.LITTLE_ENDIAN);
        value[0] = bb.getInt();

        index[0] += req_size;
        next[0] = order.remove(0);
    }

    private void onGetLong(byte[] buffer, int[] index, ArrayList<Integer> order, int[] next, long[] value) {
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
        value[0] = bb.getLong();

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

    private void onCollectAttribute(ArrayList<Integer> order, int[] next, ArrayList<Attribute> array, String str, long value) {
        array.add(new Attribute(str, value));
        next[0] = order.remove(0);
    }

    private void onFirstRun(ArrayList<Integer> order, int[] next) {
        send(JOIN);
        send(name);
        next[0] = order.remove(0);
    }

    private void onConnected(ArrayList<Integer> order, int[] next, long value) {
        id = value;
        next[0] = order.remove(0);
    }

    private void onDepopulateByDevice(ArrayList<Integer> order, int[] next, long value) {
        sendMessage(DEPOPULATE_BY_DEVICE, value);
        next[0] = order.remove(0);
    }

    private void onPopulateWithDevice(ArrayList<Integer> order, int[] next, ArrayList<Attribute> array) {
        sendMessage(POPULATE_WITH_DEVICE, array);
        next[0] = order.remove(0);
    }

    @Override
    public void run() {
        InputStream inputstream;
        ArrayList<Integer> order = new ArrayList<>();
        ArrayList<String> array_s = new ArrayList<>();
        ArrayList<Attribute> array_a = new ArrayList<>();
        int[] buffer_size = new int[1];
        int[] index = new int[2];
        int[] next = new int[1];
        int[] ivalue = new int[1];
        long[] lvalue = new long[1];
        String[] str = new String[1];
        byte[] buffer = new byte[BUFFER_SIZE];
        boolean[] need_data = new boolean[1];
        int count;

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        sendMessage(LOG_MESSAGE, "The client thread has started.");
        sendMessage(CONNECTING, null);

        try {

            buffer_size[0] = BUFFER_SIZE;
            count = 0;
            index[0] = index[1] = 0;
            need_data[0] = false;
            next[0] = FIRST_RUN;
            order.add(GET_INTEGER);
            order.add(SET_ORDER);

            socket = new Socket(ip, port);

            inputstream = socket.getInputStream();
            outputstream = socket.getOutputStream();

            sendMessage(RUNNING, null);

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

                    if (count <= 0) break;

                    index[1] += count;
                    need_data[0] = false;
                }

                switch (next[0]) {
                    case SET_ORDER:						onSetOrder(need_data, order, next, ivalue[0]);					break;
                    case NEED_DATA:						onNeedData(buffer, index, buffer_size, need_data, order, next); break;
                    case GET_INTEGER:					onGetInteger(buffer, index, order, next, ivalue);				break;
                    case GET_LONG:					    onGetLong(buffer, index, order, next, lvalue);				    break;
                    case GET_STRING:					onGetString(buffer, index, order, next, ivalue[0], str);		break;
                    case COLLECT_ATTRIBUTE:				onCollectAttribute(order, next, array_a, str[0], lvalue[0]);    break;
                    case FIRST_RUN:						onFirstRun(order, next);		                                break;
                    case CONNECTED:                     onConnected(order, next, lvalue[0]);                            break;
                    case DEPOPULATE_BY_DEVICE:			onDepopulateByDevice(order, next, lvalue[0]);					break;
                    case POPULATE_WITH_DEVICE:			onPopulateWithDevice(order, next, array_a);						break;
                }
            }

        } catch (IOException | SecurityException e) {
            sendMessage(LOG_MESSAGE, e.toString());
        }

        if(socket != null) shutdown();

        sendMessage(SHUTTING_DOWN, this);
        sendMessage(LOG_MESSAGE, "The client thread has exited.");

    }
}
