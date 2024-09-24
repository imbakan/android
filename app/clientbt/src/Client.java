package balikbayan.box.clientbt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class Client implements Runnable {

    private final UUID MY_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    public static final int LOG_MESSAGE     = 2001;
    public static final int CONNECTING      = 2002;
    public static final int RUNNING         = 2003;
    public static final int SHUTTING_DOWN   = 2004;

    private final int BUFFER_SIZE = 32768;

    private final int NEED_DATA      = 3101;
    private final int SET_ORDER      = 3102;
    private final int GET_INTEGER    = 3103;
    private final int GET_LONG       = 3104;
    private final int GET_STRING     = 3105;

    private final int ATTRIBUTES         = 3201;
    public static final int STRINGS      = 3202;
    private final int COLLECT_STRING     = 3203;
    private final int COLLECT_ATTRIBUTE  = 3204;

    private final int JOIN               = 3301;
    private final int LEAVE              = 3302;
    private final int CLIENT_RUNNING     = 3303;
    private final int CONFIRM            = 3304;
    private final int ID_RECEIVED        = 3305;
    private final int RUN_ONCE           = 3306;
    public static final int FORWARD      = 3307;

    private final int REPLY_DEVICE       = 3401;
    private final int REPLY_DEVICES      = 3402;
    private final int REPLY_DRIVE        = 3403;
    private final int REPLY_DIRECTORY    = 3404;
    private final int REPLY_FILE         = 3405;

    public static final int REQUEST_DRIVE       = 3501;
    public static final int REQUEST_DIRECTORY   = 3502;
    public static final int REQUEST_FILE        = 3503;
    public static final int REQUEST_CONTENT     = 3504;

    public static final int DEPOPULATE_BY_DEVICE        = 3601;
    public static final int POPULATE_WITH_DEVICE        = 3602;
    public static final int POPULATE_WITH_DEVICES       = 3603;
    public static final int POPULATE_WITH_DRIVES        = 3604;
    public static final int POPULATE_WITH_DIRECTORIES   = 3605;
    public static final int POPULATE_WITH_FILES         = 3606;

    private final int ENUMERATE_DRIVES      = 3701;
    private final int ENUMERATE_DIRECTORIES = 3702;
    private final int ENUMERATE_FILES       = 3703;
    private final int ENUMERATE_CONTENTS    = 3704;

    private BluetoothSocket socket;
    private BluetoothDevice device;
    private OutputStream outputstream;
    private Handler handler;
    private Context context;
    private long my_id;
    private String my_name;
    private String[] storage_name;
    private String[] storage_path;

    public Client(Handler handler, Context context, String my_name) {
        this.handler = handler;
        this.context = context;
        this.my_name = my_name;
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

    public void connect(BluetoothDevice device) {
        this.device = device;
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

    public void send(byte[] buffer, int offset, int size) {

        send(size);

        try {
            outputstream.write(buffer, offset, size);
        } catch (IOException e){
            sendMessage(LOG_MESSAGE, e.toString());
        }
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

    // A B = negative       B A = positive
    private int compareString(ListViewItem item1, ListViewItem item2) {

        int n1, n2;
        String s1, s2, str1, str2;

        str1 = item1.getString1();
        str2 = item2.getString1();

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

    private void initialize(Context context) {
        ArrayList<String> array1 = new ArrayList<>();
        ArrayList<String> array2 = new ArrayList<>();
        Set<String> volumes;
        String[] array;
        String str, str1, str2;
        int i, n;

        // internal storage
        File file = Environment.getExternalStorageDirectory();
        str = file.toString();
        array1.add("Internal Storage");
        array2.add(str);

        // external storage
        volumes = MediaStore.getExternalVolumeNames(context);
        n = volumes.size();
        if (n > 0) {
            array = new String[n];
            volumes.toArray(array);
            str = "/storage/" + array[1];
            array1.add("External Storage");
            array2.add(str);
        }

        n = array1.size();

        storage_name = new String[n];
        storage_path = new String[n];

        i = 0;

        while (!array1.isEmpty()) {

            str1 = array1.remove(0);
            str2 = array2.remove(0);

            storage_name[i] = str1;
            storage_path[i] = str2;

            ++i;
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
            case CONFIRM:
                order.add(GET_LONG);
                order.add(ID_RECEIVED);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;
            case REPLY_DEVICE:
                order.add(GET_INTEGER);
                order.add(GET_STRING);
                order.add(GET_LONG);
                order.add(POPULATE_WITH_DEVICE);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;
            case REPLY_DEVICES:
                order.add(POPULATE_WITH_DEVICES);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;
            case REPLY_DRIVE:
                order.add(POPULATE_WITH_DRIVES);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;
            case REPLY_DIRECTORY:
                order.add(POPULATE_WITH_DIRECTORIES);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;
            case REPLY_FILE:
                order.add(POPULATE_WITH_FILES);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;
            case REQUEST_DRIVE:
                order.add(GET_LONG);
                order.add(ENUMERATE_DRIVES);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;
            case REQUEST_DIRECTORY:
                order.add(GET_LONG);
                order.add(ENUMERATE_DIRECTORIES);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;
            case REQUEST_FILE:
                order.add(GET_LONG);
                order.add(ENUMERATE_FILES);
                order.add(GET_INTEGER);
                order.add(SET_ORDER);
                break;
            case REQUEST_CONTENT:
                order.add(GET_LONG);
                order.add(ENUMERATE_CONTENTS);
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

    private void onCollectString(ArrayList<Integer> order, int[] next, ArrayList<String> array, String str) {
        array.add(str);
        next[0] = order.remove(0);
    }

    private void onCollectAttribute(ArrayList<Integer> order, int[] next, ArrayList<Attribute> array, String str, long value) {
        array.add(new Attribute(str, value));
        next[0] = order.remove(0);
    }

    private void onRunOnce(ArrayList<Integer> order, int[] next) {
        send(JOIN);
        send(my_name);
        next[0] = order.remove(0);
    }

    private void onIdReceived(ArrayList<Integer> order, int[] next, long value) {
        my_id = value;
        sendMessage(Client.LOG_MESSAGE, String.format("The client thread 0x%x has started.", my_id));
        next[0] = order.remove(0);
    }

    private void onDepopulateByDevice(ArrayList<Integer> order, int[] next, long value) {
        sendMessage(DEPOPULATE_BY_DEVICE, value);
        next[0] = order.remove(0);
    }

    private void onPopulateWithDevice(ArrayList<Integer> order, int[] next, String str, long value) {
        Attribute attribute = new Attribute(str, value);
        sendMessage(POPULATE_WITH_DEVICE, attribute);
        next[0] = order.remove(0);
    }

    private void onPopulateWithDevices(ArrayList<Integer> order, int[] next, ArrayList<Attribute> array) {
        sendMessage(POPULATE_WITH_DEVICES, array);
        next[0] = order.remove(0);
    }

    private void onPopulateWithDrives(ArrayList<Integer> order, int[] next, ArrayList<String> array) {
        sendMessage(POPULATE_WITH_DRIVES, array);
        next[0] = order.remove(0);
    }

    private void onPopulateWithDirectories(ArrayList<Integer> order, int[] next, ArrayList<String> array) {
        sendMessage(POPULATE_WITH_DIRECTORIES, array);
        next[0] = order.remove(0);
    }

    private void onPopulateWithFiles(ArrayList<Integer> order, int[] next, ArrayList<String> array) {
        sendMessage(POPULATE_WITH_FILES, array);
        next[0] = order.remove(0);
    }

    private void onEnumerateDrives(ArrayList<Integer> order, int[] next, long value) {
        int i, n, count;
        long size;

        count = 0;

        for (String name : storage_name)
            count += name.length();

        n = storage_name.length;

        size = 2L * n * Integer.SIZE / 8 + (long) count * Short.SIZE / 8 + Integer.SIZE / 8;

        send(FORWARD);
        send(value);
        send(size);

        for (i=0; i<n; i++) {

            send(STRINGS);
            send(storage_name[i]);
        }

        send(REPLY_DRIVE);

        next[0] = order.remove(0);
    }

    private void onEnumerateDirectories(ArrayList<Integer> order, int[] next, ArrayList<String> array, long value) {
        File[] files;
        File file;
        ArrayList<String> array1 = new ArrayList<>();
        String str;
        String drive;
        StringBuilder path;
        int i;
        long size, count;

        // buuin ang path name
        str = "";
        drive = array.remove(0);

        for (i=0; i<storage_name.length; i++)
            if (drive.compareTo(storage_name[i]) == 0) {
                str = storage_path[i];
                break;
            }

        path = new StringBuilder(str);

        while (!array.isEmpty()) {
            str = array.remove(0);
            path.append("/").append(str);
        }

        // kunin ang mga directory
        file = new File(path.toString());
        files = file.listFiles();

        if(files != null)
            for (File file1 : files)
                if (file1.isDirectory())
                    array1.add(file1.getName());

        // isend kung merong isesend
        if (!array1.isEmpty()) {

            // isort
            array1.sort(new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    return compareString(s, t1);
                }
            });

            // kompyutin ang size na isesend sa destination client
            count = 0L;
            for (String str1 : array1)
                count += str1.length();

            size = 2L * array1.size() * Integer.BYTES + count * Short.BYTES + Integer.BYTES;

            // isend ang mga ito sa server
            // ang value ay id number ng destination client
            send(FORWARD);
            send(value);
            send(size);

            while (!array1.isEmpty()) {

                str = array1.remove(0);

                send(STRINGS);
                send(str);
            }

            send(REPLY_DIRECTORY);
        }

        next[0] = order.remove(0);
    }

    private void onEnumerateFiles(ArrayList<Integer> order, int[] next, ArrayList<String> array, long value) {
        File[] files;
        File file;
        ArrayList<ListViewItem> array1 = new ArrayList<>();
        ListViewItem item;
        String str, str1, str2, str3, str4;
        String drive;
        StringBuilder path;
        int i, YYYY, MM, DD, hh, mm, ss;
        long size, count, count1, count2;
        Calendar c;

        // buuin ang path name
        str = "";
        drive = array.remove(0);

        for (i=0; i<storage_name.length; i++)
            if (drive.compareTo(storage_name[i]) == 0) {
                str = storage_path[i];
                break;
            }

        path = new StringBuilder(str);

        while (!array.isEmpty()) {
            str = array.remove(0);
            path.append("/").append(str);
        }

        // kunin ang mga file
        file = new File(path.toString());
        files = file.listFiles();

        count1 = count2 = 0;

        if(files != null)
            for (File file1 : files)
                if (file1.isFile()) {

                    c = Calendar.getInstance();
                    c.setTimeInMillis(file1.lastModified());
                    YYYY = c.get(Calendar.YEAR);
                    MM = c.get(Calendar.MONTH) + 1;
                    DD = c.get(Calendar.DAY_OF_MONTH);
                    hh = c.get(Calendar.HOUR_OF_DAY);
                    mm = c.get(Calendar.MINUTE);
                    ss = c.get(Calendar.SECOND);

                    if (hh == 12) {
                        str4 = "PM";
                    } else if (hh > 12) {
                        str4 = "PM";
                        hh -= 12;
                    } else {
                        str4 = "AM";
                    }

                    str1 = file1.getName();
                    str2 = String.format(Locale.US, "%d-%d-%d %2d:%02d:%02d %s", MM, DD, YYYY, hh, mm, ss, str4);
                    str3 = String.format(Locale.US, "%,d", file1.length());

                    array1.add(new ListViewItem(str1, str2, str3));

                    ++count1;

                } else {
                    ++count2;
                }

        // isend kung merong isesend
        if (!array1.isEmpty()) {

            // isort
            array1.sort(new Comparator<ListViewItem>() {
                @Override
                public int compare(ListViewItem listViewItem, ListViewItem t1) {
                    return compareString(listViewItem, t1);
                }
            });

            // kompyutin ang size na isesend sa destination client
            count = 0L;
            for (ListViewItem item1 : array1)
                count += (item1.getString1().length() + item1.getString2().length() + item1.getString3().length());

            str = String.format(Locale.US, "%d files %d folders", count1, count2);

            size = 6L * array1.size() * Integer.BYTES + count * Short.BYTES + Integer.BYTES;
            size += (2L * Integer.BYTES + (long) Short.BYTES * str.length());

            // isend ang mga ito sa server
            // ang value ay id number ng destination client
            send(FORWARD);
            send(value);
            send(size);

            send(STRINGS);
            send(str);

            while (!array1.isEmpty()) {

                item = array1.remove(0);

                send(STRINGS);
                send(item.getString1());

                send(STRINGS);
                send(item.getString2());

                send(STRINGS);
                send(item.getString3());
            }

            send(REPLY_FILE);
        }

        next[0] = order.remove(0);
    }

    private void onEnumerateContents(ArrayList<Integer> order, int[] next, ArrayList<String> array, long value) {
        StringBuilder path;
        String str, drive;
        int i;

        // buuin ang path name
        str = "";
        drive = array.remove(0);

        for (i=0; i<storage_name.length; i++)
            if (drive.compareTo(storage_name[i]) == 0) {
                str = storage_path[i];
                break;
            }

        path = new StringBuilder(str);

        while (!array.isEmpty()) {
            str = array.remove(0);
            path.append("/").append(str);
        }

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
        byte[] buffer = new byte[BUFFER_SIZE];
        boolean[] need_data = new boolean[1];
        int count;

        int[] ivalue = new int[1];
        long[] lvalue = new long[1];
        String[] str = new String[1];

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        sendMessage(CONNECTING, null);

        try {

            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();

            inputstream = socket.getInputStream();
            outputstream = socket.getOutputStream();

            sendMessage(RUNNING, null);

            initialize(context);

            buffer_size[0] = BUFFER_SIZE;
            count = 0;
            index[0] = index[1] = 0;
            need_data[0] = false;
            next[0] = RUN_ONCE;
            order.add(GET_INTEGER);
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
                    case COLLECT_STRING:				onCollectString(order, next, array_s, str[0]);					break;
                    case COLLECT_ATTRIBUTE:				onCollectAttribute(order, next, array_a, str[0], lvalue[0]);	break;
                    case RUN_ONCE:						onRunOnce(order, next);											break;
                    case ID_RECEIVED:					onIdReceived(order, next, lvalue[0]);							break;
                    case DEPOPULATE_BY_DEVICE:			onDepopulateByDevice(order, next, lvalue[0]);					break;
                    case POPULATE_WITH_DEVICE:			onPopulateWithDevice(order, next, str[0], lvalue[0]);			break;
                    case POPULATE_WITH_DEVICES:			onPopulateWithDevices(order, next, array_a);					break;
                    case POPULATE_WITH_DRIVES:			onPopulateWithDrives(order, next, array_s);						break;
                    case POPULATE_WITH_DIRECTORIES:		onPopulateWithDirectories(order, next, array_s);				break;
                    case POPULATE_WITH_FILES:		    onPopulateWithFiles(order, next, array_s);				        break;

                    case ENUMERATE_DRIVES:				onEnumerateDrives(order, next, lvalue[0]);						break;
                    case ENUMERATE_DIRECTORIES:			onEnumerateDirectories(order, next, array_s, lvalue[0]);		break;
                    case ENUMERATE_FILES:			    onEnumerateFiles(order, next, array_s, lvalue[0]);		        break;
                    case ENUMERATE_CONTENTS:			onEnumerateContents(order, next, array_s, lvalue[0]);		    break;
                }
            }

        } catch (IOException | SecurityException e) {
            sendMessage(LOG_MESSAGE, e.toString());
        }

        if(socket != null) shutdown();

        sendMessage(SHUTTING_DOWN, this);
    }
}
