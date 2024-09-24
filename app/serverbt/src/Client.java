package balikbayan.box.serverbt06;

import android.bluetooth.BluetoothSocket;
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
import java.util.Calendar;
import java.util.Locale;

public class Client implements Runnable {

    public static final int LOG_MESSAGE     = 2001;
    public static final int RUNNING         = 2002;
    public static final int SHUTTING_DOWN   = 2003;

    private final int BUFFER_SIZE = 32768;

    private final int NEED_DATA     = 3101;
    private final int SET_ORDER     = 3102;
    private final int GET_INTEGER   = 3103;
    private final int GET_LONG_1    = 3104;
    private final int GET_LONG_2    = 3105;
    private final int GET_STRING    = 3106;
    private final int GET_CLIENT    = 3107;

    public static final int ATTRIBUTES  = 3201;
    private final int STRINGS           = 3202;

    private final int JOIN              = 3301;
    public static final int LEAVE       = 3302;
    //  private final int CLIENT_RUNNING    = 3303;
    public static final int CONFIRM     = 3304;
    private final int FORWARD           = 3307;
    private final int RESEND            = 3308;

    public static final int REPLY_DEVICE    = 3401;
    public static final int REPLY_DEVICES   = 3402;

    private BluetoothSocket socket;
    private OutputStream outputstream;
    private Handler handler;
    private String my_name, my_log_on_time;
    private long my_id;
    private ListViewAdapter adapter;

    public Client(Handler handler, BluetoothSocket socket, ListViewAdapter adapter) {
        this.handler = handler;
        this.socket = socket;
        this.adapter = adapter;
    }

    private String getBuffer(String msg, byte[] b, int index, int count) {
        int i, k;
        StringBuilder str;

        str = new StringBuilder();

        str.append(msg);

        k = index;

        for (i=0; i<count; i++)
            str.append(String.format(" %02x", b[k++]));

        return str.toString();
    }

    public long getId() {
        return my_id;
    }

    public String getName() {
        return my_name;
    }

    public String getLogOnTime() {
        return my_log_on_time;
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
            bb = ByteBuffer.allocate(Integer.SIZE / 8);
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
            bb = ByteBuffer.allocate(Long.SIZE / 8);
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

    public void send(byte[] buffer, int offset, int size) {

        try {
            outputstream.write(buffer, offset, size);
        } catch (IOException e){
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
            case JOIN:
                order.add(GET_INTEGER);
                order.add(GET_STRING);
                order.add(RUNNING);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;
            case FORWARD:
                order.add(GET_LONG_1);
                order.add(GET_LONG_2);
                order.add(GET_CLIENT);
                order.add(RESEND);
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
        req_size = Integer.SIZE / 8;

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

    private void onGetLong(byte[] buffer, int[] index, ArrayList<Integer> order, int[] next, long[] value, int element) {
        ByteBuffer bb;
        int avail_size, req_size;

        avail_size = index[1] - index[0];
        req_size = Long.SIZE / 8;

        if (avail_size < req_size) {
            order.add(0, element);
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
        req_size = len * Short.SIZE / 8;

        if (avail_size < req_size) {
            order.add(0, GET_STRING);
            next[0] = NEED_DATA;
            return;
        }

        str[0] = new String(buffer, index[0], req_size, StandardCharsets.UTF_16LE);

        index[0] += req_size;
        next[0] = order.remove(0);
    }

    private void onGetClient(ArrayList<Integer> order, int[] next, long[] value, Client[] client) {
        Client item;
        int i, n;

        n = adapter.getItemCount();

        for (i=0; i<n; i++) {
            item = adapter.getItem(i);
            if (item.getId() == value[0]) {
                client[0] = item;
                break;
            }
        }

        next[0] = order.remove(0);
    }

    private void onRunning(ArrayList<Integer> order, int[] next, String[] str) {
        Calendar c;
        String str3;
        int YYYY, MM, DD, hh, mm, ss;

        c = Calendar.getInstance();
        YYYY = c.get(Calendar.YEAR);
        MM = c.get(Calendar.MONTH) + 1;
        DD = c.get(Calendar.DAY_OF_MONTH);
        hh = c.get(Calendar.HOUR_OF_DAY);
        mm = c.get(Calendar.MINUTE);
        ss = c.get(Calendar.SECOND);

        if (hh == 12) {
            str3 = "PM";
        } else if (hh > 12) {
            str3 = "PM";
            hh -= 12;
        } else {
            str3 = "AM";
        }

        my_name = str[0];
        my_log_on_time = String.format(Locale.US, "%d-%d-%d %2d:%02d:%02d %s", MM, DD, YYYY, hh, mm, ss, str3);

        sendMessage(RUNNING, this);

        next[0] = order.remove(0);
    }

    private void onResend(byte[] buffer, int[] index, int[] buffer_size, boolean[] need_data, ArrayList<Integer> order, int[] next, Client[] client, long[] data_size) {
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

    @Override
    public void run() {

        InputStream inputstream;
        ArrayList<Integer> order = new ArrayList<>();
        ArrayList<String> array = new ArrayList<>();
        int[] buffer_size = new int[1];
        int[] index = new int[2];
        int[] next = new int[1];
        byte[] buffer = new byte[BUFFER_SIZE];
        boolean[] need_data = new boolean[1];
        int count;

        int[] ivalue = new int[1];
        long[] lvalue1 = new long[1];
        long[] lvalue2 = new long[1];
        String[] str = new String[1];
        Client[] client = new Client[1];

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        my_id = SystemClock.elapsedRealtime();

        try {

            inputstream = socket.getInputStream();
            outputstream = socket.getOutputStream();

            buffer_size[0] = BUFFER_SIZE;
            count = 0;
            index[0] = index[1] = 0;
            need_data[0] = true;
            next[0] = GET_INTEGER;
            order.add(SET_ORDER);

            // loop
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
                    case SET_ORDER:		onSetOrder(need_data, order, next, ivalue[0]);									break;
                    case NEED_DATA:		onNeedData(buffer, index, buffer_size, need_data, order, next);					break;
                    case GET_INTEGER:	onGetInteger(buffer, index, order, next, ivalue);								break;
                    case GET_LONG_1:	onGetLong(buffer, index, order, next, lvalue1, next[0]);						break;
                    case GET_LONG_2:	onGetLong(buffer, index, order, next, lvalue2, next[0]);						break;
                    case GET_STRING:	onGetString(buffer, index, order, next, ivalue[0], str);						break;
                    case GET_CLIENT:	onGetClient(order, next, lvalue1, client);										break;
                    case RUNNING:	    onRunning(order, next, str);													break;
                    case RESEND:		onResend(buffer, index, buffer_size, need_data, order, next, client, lvalue2);  break;
                }
            }

        } catch (IOException | SecurityException e) {
            //Log.d("KLGYN", e.toString());
            sendMessage(LOG_MESSAGE, e.toString());
        }

        shutdown();

        sendMessage(SHUTTING_DOWN, this);
    }
}
