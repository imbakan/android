package balikbayan.box.bt_client;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

public class Client implements Runnable {

    private final UUID MY_UUID = UUID.fromString("b1899020-c25d-489b-a700-42304d6adbbc");

    private final int BUFFER_SIZE = 1024;

    public static final int MESSAGE         = 2001;
    public static final int CONNECTING      = 2002;
    public static final int RUNNING         = 2003;
    public static final int SHUTTING_DOWN   = 2004;

    private final int NEED_DATA     = 3001;
    private final int SET_ORDER     = 3002;
    private final int GET_INTEGER   = 3003;
    private final int GET_LONG      = 3004;
    private final int GET_STRING    = 3005;
    private final int GET_CLIENT_ID = 3006;
    private final int GET_BUFFERS   = 3007;

    private final int BUFFERS               = 4001;
    private final int STRINGS               = 4002;
    private final int COLLECT_STRING        = 4003;
    private final int COLLECT_ATTRIBUTE = 4004;

    private final int FIRST_RUN = 5001;
    private final int JOIN      = 5003;
    private final int CLIENT_ID = 5004;
    private final int LEAVE      = 5005;

    private final int ATTRIBUTE = 6001;
    private final int DEVICE    = 6002;
    private final int DRIVE     = 6003;
    private final int FOLDER    = 6004;

    public static final int FORWARD    = 7001;

    public static final int REQ_DRIVE     = 8001;
    public static final int REQ_FOLDER    = 8002;

    private final int ENUMERATE_DRIVE   = 9001;
    private final int ENUMERATE_FOLDER  = 9002;

    public static final int POPULATE_WITH_DEVICE  = 9006;
    public static final int POPULATE_WITH_DRIVE   = 9007;
    public static final int POPULATE_WITH_FOLDER  = 9008;
    public static final int DEPOPULATE_BY_DEVICE  = 9009;

    private BluetoothSocket socket;
    private BluetoothDevice device;
    private OutputStream outputstream;
    private Handler handler;
    private Context context;

    private long id;
    private String name;

    private ArrayList<String> storage_paths = new ArrayList<>();
    private ArrayList<String> storage_names = new ArrayList<>();

    public Client(Context context, Handler handler, String name) {
        this.context = context;
        this.handler = handler;
        this.name = name;
    }

    public void shutdown() {
        try {
            socket.close();
        } catch (IOException e) {
            sendMessage(handler, MESSAGE, e.toString());
        }
    }

    public void connect(BluetoothDevice device) {
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
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
            sendMessage(handler, MESSAGE, e.toString());
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
            sendMessage(handler, MESSAGE, e.toString());
        }
    }

    public void send(String str) {
        byte[] b;

        send(str.length());

        try {
            b = str.getBytes(StandardCharsets.UTF_16LE);
            outputstream.write(b);
        } catch (IOException e) {
            sendMessage(handler, MESSAGE, e.toString());
        }
    }

    public void send(byte[] b, int size) {

        if (size == 0) return;

        send(size);

        try {
            outputstream.write(b, 0, size);
        } catch (IOException e) {
            sendMessage(handler, MESSAGE, e.toString());
        }
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

            case CLIENT_ID:
                order.add(GET_LONG);
                order.add(GET_CLIENT_ID);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case REQ_DRIVE:
                order.add(ENUMERATE_DRIVE);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case REQ_FOLDER:
                order.add(GET_INTEGER);
                order.add(GET_BUFFERS);
                order.add(ENUMERATE_FOLDER);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case ATTRIBUTE:
                order.add(GET_LONG);
                order.add(GET_INTEGER);
                order.add(GET_STRING);
                order.add(COLLECT_ATTRIBUTE);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case DEVICE:
                order.add(POPULATE_WITH_DEVICE);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case DRIVE:
                order.add(POPULATE_WITH_DRIVE);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case FOLDER:
                order.add(POPULATE_WITH_FOLDER);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;

            case LEAVE:
                order.add(GET_LONG);
                order.add(DEPOPULATE_BY_DEVICE);
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

        //Log.d("KLGYN", str[0]);

        index[0] += req_size;
        next[0] = order.remove(0);
    }

    private void onGetClientId(ArrayList<Integer> order, int[] next, long value, long[] id) {
        id[0] = value;
        next[0] = order.remove(0);
    }

    private void onGetBuffer(byte[] buffer, int[] index, ArrayList<Integer> order, int[] next, int size, ByteBuffer[] value) {
        int i, k,avail_size, req_size;

        avail_size = index[1] - index[0];
        req_size = size;
        byte[] b;

        if (avail_size < req_size) {
            order.add(0, GET_BUFFERS);
            next[0] = NEED_DATA;
            return;
        }

        //Log.d("KLGYN", String.format("%10d%10d", avail_size, req_size));

        b = new byte[size];

        k = index[0];

        for (i = 0; i < size; i++)
		    b[i] = buffer[k++];

        value[0] = ByteBuffer.allocate(size);
        value[0].order(ByteOrder.LITTLE_ENDIAN);
        value[0].put(b);

        index[0] += req_size;
        next[0] = order.remove(0);
    }

    private void onCollectString(ArrayList<Integer> order, int[] next, ArrayList<String> array, String str) {
        array.add(str);
        next[0] = order.remove(0);
    }

    private void onCollectAttribute(ArrayList<Integer> order, int[] next, ArrayList<String> array1, String str, ArrayList<Long> array2, long value) {

        array1.add(str);
        array2.add(value);

        next[0] = order.remove(0);
    }

    private void onEnumerateDrive(ArrayList<Integer> order, int[] next, long id) {

        long size;
        int count;

        getDeviceStorages(storage_paths, storage_names);

        //for (String path : storage_paths)
        //    Log.d("KLGYN", path);

        size = 0;

        for (String name : storage_names)
            size += (name.getBytes(StandardCharsets.UTF_16LE).length + Integer.BYTES);

        count = storage_names.size();
        size += ((long) (count + 1) * Integer.BYTES);

        //Log.d("KLGYN", String.format("%10d", size));

        send(FORWARD);
        send(id);
        send(size);

        for (String str : storage_names) {

            send(STRINGS);
            send(str);
        }

        send(DRIVE);

        next[0] = order.remove(0);
    }

    private void onEnumerateFolder(ArrayList<Integer> order, int[] next, ByteBuffer bb, long id) {

        ArrayList<String> array1 = new ArrayList<>();
        ArrayList<String> array2 = new ArrayList<>();
        long size;
        int count;
        String pathname, str;

        getPathName(bb, array1);
        pathname = getPathName(storage_names, storage_paths, array1);

        //Log.d("KLGYN", pathname);

        if (!getDirectory(pathname, array2)) {
            Toast.makeText(context, "Get directories failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        size = 0;

        for (String name : array2)
            size += (name.getBytes(StandardCharsets.UTF_16LE).length + Integer.BYTES);

        count = array2.size();
        size += ((long) (count + 1) * Integer.BYTES);

        send(FORWARD);
        send(id);
        send(size);

        while (!array2.isEmpty()) {

            str = array2.remove(0);

            send(STRINGS);
            send(str);
        }

        send(FOLDER);

        next[0] = order.remove(0);
    }

    private void onPopulateWithDevice(ArrayList<Integer> order, int[] next, ArrayList<String> array1, ArrayList<Long> array2) {

        //for (String str : array1)
        //    Log.d("KLGYN", String.format("%40s", str));

        Bundle bundle = new Bundle();

        bundle.putStringArrayList("Array1", array1);

        int k, n = array2.size();
        long[] value = new long[n];

        k = 0;

        while (!array2.isEmpty()) {
            value[k++] = array2.remove(0);
        }

        bundle.putLongArray("Array2", value);

        sendMessage(handler, POPULATE_WITH_DEVICE, bundle);

        next[0] = order.remove(0);
    }

    private void onPopulateWithDrive(ArrayList<Integer> order, int[] next, ArrayList<String> array) {

        sendMessage(handler, POPULATE_WITH_DRIVE, array);

        next[0] = order.remove(0);
    }

    private void onPopulateWithFolder(ArrayList<Integer> order, int[] next, ArrayList<String> array) {

        sendMessage(handler, POPULATE_WITH_FOLDER, array);

        next[0] = order.remove(0);
    }

    private void onDepopulateByDevice(ArrayList<Integer> order, int[] next, long value) {

        sendMessage(handler, DEPOPULATE_BY_DEVICE, value);

        next[0] = order.remove(0);
    }

    private void onFirstRun(ArrayList<Integer> order, int[] next) {
        send(JOIN);
        send(name);
        next[0] = order.remove(0);
    }

    private void sendMessage(Handler handler, int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

    // A B = negative       B A = positive
    private int compareString(String str1, String str2) {

        int n1, n2;
        String s1, s2;

        n1 = str1.length();
        n2 = str2.length();

        if(n1 < n2) {
            s1 = str1;
            s2 = str2.substring(0, n1);
        } else {
            s1 = str1.substring(0, n2);
            s2 = str2;
        }

        return s1.compareToIgnoreCase(s2);
    }

    // ilagay ang storage sa array list
    private void getDeviceStorages(ArrayList<String> paths, ArrayList<String> names) {
        File file;
        String[] str = new String[2];
        String str3;
        int i;

        paths.clear();
        names.clear();

        // internal storage
        file = Environment.getExternalStorageDirectory();

        str[0] = file.toString();
        str[1] = "Internal Storage";

        paths.add(str[0]);
        names.add(str[1]);

        // external storage
        Set<String> volumes = MediaStore.getExternalVolumeNames(context);
        int n = volumes.size();

        if (n > 1) {

            String[] array = new String[n];
            volumes.toArray(array);

            str[0] = "/storage/" + array[1].toUpperCase();
            str[1] = "External Storage";

            paths.add(str[0]);
            names.add(str[1]);
        }
    }

    // kunin ang pathname na nasa bytebuffer at ilagay sa arraylist
    private void getPathName(ByteBuffer bb, ArrayList<String> array) {
        short i, m, n;
        byte[] b;
        bb.position(0);

        n = bb.getShort();

        for (i=0; i<n; i++) {

            m = (short) (bb.getShort() * Short.BYTES);
            b = new byte[m];
            bb.get(b, 0, m);
            String str = new String(b, 0, m, StandardCharsets.UTF_16LE);

            //Log.d("KLGYN", String.format("%10d %s", i, str));

            array.add(str);
        }

    }

    // buuin ang pathname na nasa ArrayList array
    // ito ay separated ng slash character
    private String getPathName(ArrayList<String> names, ArrayList<String> paths, ArrayList<String> array) {
        StringBuilder sb;
        String str1, str2, str3;
        int i, n;

        // alisin ang device name
        array.remove(0);

        // palitan ng pathname ang storage name
        str3 = "";
        str1 = array.remove(0);

        n = names.size();

        for (i=0; i<n; i++) {

            str2 = names.get(i);

            if (str1.compareTo(str2) == 0) {
                str3 = paths.get(i);
                break;
            }
        }

        // buuin ang pathname
        sb = new StringBuilder(str3);

        while (!array.isEmpty()) {

            str3 = array.remove(0);
            sb.append("/").append(str3);
        }

        return sb.toString();
    }

    // kunin ang mga directoty ng pathname at ilagay sa arraylist
    private boolean getDirectory(String pathname, ArrayList<String> array) {
        File[] files;
        File file;

        file = new File(pathname);
        files = file.listFiles();

        // error checking
        if(files == null) return false;

        for (File file1 : files)
            if (file1.isDirectory())
                array.add(file1.getName());

        array.sort(new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return compareString(s, t1);
            }
        });

        return true;
    }

    private void printOutput(int cmd) {
        switch (cmd) {
            case SET_ORDER:						Log.d("KLGYN", "SET_ORDER");            break;
            case NEED_DATA:						Log.d("KLGYN", "NEED_DATA");            break;
            case GET_INTEGER:					Log.d("KLGYN", "GET_INTEGER");          break;
            case GET_LONG:					    Log.d("KLGYN", "GET_LONG");             break;
            case GET_STRING:					Log.d("KLGYN", "GET_STRING");           break;
            case GET_BUFFERS:					Log.d("KLGYN", "GET_BUFFERS");          break;
            case COLLECT_STRING:		        Log.d("KLGYN", "COLLECT_STRING");       break;
            case GET_CLIENT_ID:		            Log.d("KLGYN", "GET_CLIENT_ID");        break;
            case COLLECT_ATTRIBUTE:		        Log.d("KLGYN", "COLLECT_ATTRIBUTE");    break;

            case ENUMERATE_DRIVE:				Log.d("KLGYN", "ENUMERATE_DRIVE");      break;
            case ENUMERATE_FOLDER:				Log.d("KLGYN", "ENUMERATE_FOLDER");      break;

            case POPULATE_WITH_DEVICE:			Log.d("KLGYN", "POPULATE_WITH_DEVICE"); break;
            case POPULATE_WITH_DRIVE:			Log.d("KLGYN", "POPULATE_WITH_DRIVE");  break;
            case POPULATE_WITH_FOLDER:			Log.d("KLGYN", "POPULATE_WITH_FOLDER");  break;
            case DEPOPULATE_BY_DEVICE:			Log.d("KLGYN", "DEPOPULATE_BY_DEVICE");  break;

            case FIRST_RUN:				        Log.d("KLGYN", "FIRST_RUN");            break;
            default:                            Log.d("KLGYN", String.format("*****%d*****", cmd));
        }
    }

    @Override
    public void run() {
        InputStream inputstream;
        ArrayList<Integer> order = new ArrayList<>();
        ArrayList<Long> ids = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ByteBuffer[] bb = new ByteBuffer[1];
        int[] buffer_size = new int[1];
        int[] index = new int[2];
        int[] next = new int[1];
        byte[] buffer = new byte[BUFFER_SIZE];
        boolean[] need_data = new boolean[1];
        int[] ivalue = new int[1];
        long[] lvalue = new long[1];
        long[] id = new long[1];
        String[] str = new String[1];
        int count;

        Log.d("KLGYN", "The client thread has started.");

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        sendMessage(handler, CONNECTING, null);

        buffer_size[0] = BUFFER_SIZE;
        count = 0;
        index[0] = index[1] = 0;
        need_data[0] = false;
        next[0] = FIRST_RUN;
        order.add(GET_INTEGER);
        order.add(SET_ORDER);

        try {

            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();

            inputstream = socket.getInputStream();
            outputstream = socket.getOutputStream();

            sendMessage(handler, RUNNING, null);

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

                printOutput(next[0]);

                switch (next[0]) {
                    case SET_ORDER:						onSetOrder(need_data, order, next, ivalue[0]);					            break;
                    case NEED_DATA:						onNeedData(buffer, index, buffer_size, need_data, order, next);             break;
                    case GET_INTEGER:					onGetInteger(buffer, index, order, next, ivalue);				            break;
                    case GET_LONG:					    onGetLong(buffer, index, order, next, lvalue);				                break;
                    case GET_STRING:					onGetString(buffer, index, order, next, ivalue[0], str);		            break;
                    case GET_CLIENT_ID:					onGetClientId(order, next, lvalue[0], id);		                            break;
                    case GET_BUFFERS:					onGetBuffer(buffer, index, order, next, ivalue[0], bb);		                break;
                    case FIRST_RUN:						onFirstRun(order, next);		                                            break;
                    case COLLECT_STRING:				onCollectString(order, next, names, str[0]);                                break;
                    case COLLECT_ATTRIBUTE:				onCollectAttribute(order, next, names, str[0], ids, lvalue[0]);             break;

                    case ENUMERATE_DRIVE:			    onEnumerateDrive(order, next, id[0]);		                                break;
                    case ENUMERATE_FOLDER:			    onEnumerateFolder(order, next, bb[0], id[0]);		                        break;

                    case POPULATE_WITH_DEVICE:			onPopulateWithDevice(order, next, names, ids);		                        break;
                    case POPULATE_WITH_DRIVE:			onPopulateWithDrive(order, next, names);		                            break;
                    case POPULATE_WITH_FOLDER:			onPopulateWithFolder(order, next, names);		                            break;
                    case DEPOPULATE_BY_DEVICE:			onDepopulateByDevice(order, next, lvalue[0]);		                        break;
                }
            }

        } catch (IOException | SecurityException e) {

        }

        shutdown();

        sendMessage(handler, SHUTTING_DOWN, this);

        Log.d("KLGYN", "The client thread has exited.");
    }
}
