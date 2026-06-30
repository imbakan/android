// Bluetooth - client side
// Android 14 API Level 34

package balikbayan.box.bt_client;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements FragmentTV.EventListener{

    private Toolbar toolbar;
    private ViewPager2 viewPager;
    private EditText editText1, editText2;
    private Handler handler;
    private Client client;
    private boolean access_granted, running;

    // reply sa requestPermissionStorage
    StorageContract contract1 = new StorageContract();
    ActivityResultLauncher<Void> launcher1 = registerForActivityResult(contract1, new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if(o)
                requestPermissionImageMedia();
            else
                access_granted = false;
        }
    });

    // reply sa requestPermissionImageMedia
    ActivityResultLauncher<String> launcher2 = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted)
            requestPermissionAudioMedia();
        else
            access_granted = false;
    });

    // reply sa requestPermissionAudioMedia
    ActivityResultLauncher<String> launcher3 = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted)
            requestPermissionVideoMedia();
        else
            access_granted = false;
    });

    // reply sa requestPermissionVideoMedia
    ActivityResultLauncher<String> launcher4 = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted)
            requestPermissionBTConnect();
        else
            access_granted = false;
    });

    // reply sa requestPermissionBTConnect
    ActivityResultLauncher<String> launcher5 = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted)
            requestPermissionBTEnability();
        else
            access_granted = false;
    });

    // reply sa requestPermissionBTEnability
    BluetoothContract contract6 = new BluetoothContract();
    ActivityResultLauncher<Void> launcher6 = registerForActivityResult(contract6, new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if(o) {
                initialize();
            } else {
                access_granted = false;
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(Color.BLACK);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("OFFLINE");
        setSupportActionBar(toolbar);

        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);

        editText1.setKeyListener(null);
        editText2.setKeyListener(null);

        viewPager = findViewById(R.id.viewPager);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d("KLGYN", String.format("%10d", position));
            }
        });

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        FragmentTV fragment1 =  new FragmentTV(this, this);
        FragmentLV fragment2 =  new FragmentLV();

        adapter.add(fragment1);
        adapter.add(fragment2);
        viewPager.setAdapter(adapter);

        //viewPager.setCurrentItem(1);
        //viewPager.setCurrentItem(0);

        //  +------------------------------------------------------------------------+
        //  |                          handler message                               |
        //  +------------------------------------------------------------------------+

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {

                switch (msg.what) {
                    case Client.MESSAGE:                onMessage(msg);             break;
                    case Client.CONNECTING:             onConnecting(msg);          break;
                    case Client.RUNNING:                onRunning(msg);             break;
                    case Client.SHUTTING_DOWN:          onShuttingDown(msg);        break;
                    case Client.POPULATE_WITH_DEVICE:   onPopulateWithDevice(msg);  break;
                    case Client.POPULATE_WITH_DRIVE:    onPopulateWithDrive(msg);   break;
                    case Client.POPULATE_WITH_FOLDER:   onPopulateWithFolder(msg);  break;
                    case Client.DEPOPULATE_BY_DEVICE:   onDepopulateByDevice(msg);  break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };


        //  +------------------------------------------------------------------------+
        //  |                            permission                                  |
        //  +------------------------------------------------------------------------+

        access_granted = running = false;

        requestPermissionStorage();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.mnuConnect).setEnabled(access_granted && !running);
        menu.findItem(R.id.mnuDisconnect).setEnabled(access_granted && running);
        menu.findItem(R.id.mnuDownload).setEnabled(access_granted);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mnuConnect)
            onClientConnect();
        else if (id == R.id.mnuDisconnect)
            onClientDisconnect();
        else if (id == R.id.mnuDownload)
            onFileDownload();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    private void onClientConnect() {
        ConnectDialog dlg = new ConnectDialog(this, new ConnectDialog.OnClickListener() {
            @Override
            public void onClick(BluetoothDevice device) {
                client.connect(device);
                new Thread(client).start();
            }
        });

        dlg.show(getSupportFragmentManager(), "device dialog");
    }

    private void onClientDisconnect() {
        client.shutdown();
    }

    private void onFileDownload() {

    }

    private void onMessage(Message msg) {
    }

    private void onConnecting(Message msg) {
        toolbar.setTitle("CONNECTING ...");
        running = true;
    }

    private void onRunning(Message msg) {
        toolbar.setTitle("ONLINE");
        running = true;
    }

    private void onShuttingDown(Message msg) {
        toolbar.setTitle("OFFLINE");
        running = false;

        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        FragmentTV fragment = (FragmentTV) adapter.getItem(0);
        TreeViewAdapter adapter1 = fragment.getAdapter();
        int n = adapter1.getItemCount();
        adapter1.clear();
        adapter1.notifyItemRangeRemoved(0, n);
    }

    private void onPopulateWithDevice(Message msg) {
        Bundle bundle = (Bundle) msg.obj;
        ArrayList<String> array1 = bundle.getStringArrayList("Array1");
        long[] array2 = bundle.getLongArray("Array2");

        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        FragmentTV fragment = (FragmentTV) adapter.getItem(0);
        TreeViewAdapter adapter1 = fragment.getAdapter();

        int pos = adapter1.getItemCount();

        //for (String sz : array1)
        //    Log.d("KLGYN", String.format("...%40s", sz));

        String str;
        int k, n;

        n = array1.size();
        k = 0;

        while (!array1.isEmpty()) {

            str = array1.remove(0);

            //Log.d("KLGYN", String.format("ooo%40s", str));

            adapter1.add(new TreeViewItem(null, str, R.raw.device1, 0, 0, array2[k++]));
        }

        adapter1.notifyItemRangeInserted(pos, n);
    }

    private void onPopulateWithDrive(Message msg) {
        ArrayList<String> array = (ArrayList<String>) msg.obj;
        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        FragmentTV fragment = (FragmentTV) adapter.getItem(0);
        TreeViewAdapter adapter1 = fragment.getAdapter();
        TreeViewItem item = adapter1.getClickItem();
        adapter1.expandItem(item, array, R.raw.drive1);
    }

    private void onPopulateWithFolder(Message msg) {
        ArrayList<String> array = (ArrayList<String>) msg.obj;
        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        FragmentTV fragment = (FragmentTV) adapter.getItem(0);
        TreeViewAdapter adapter1 = fragment.getAdapter();
        TreeViewItem item = adapter1.getClickItem();
        adapter1.expandItem(item, array, R.raw.folder1);
    }

    private void onDepopulateByDevice(Message msg) {
        long id = (long) msg.obj;
        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        FragmentTV fragment = (FragmentTV) adapter.getItem(0);
        TreeViewAdapter adapter1 = fragment.getAdapter();
        TreeViewItem item;
        int i, n;

        // hanapin ang device na ireremove sa tree view
        item = null;

        n = adapter1.getItemCount();

        for (i=0; i < n; i++) {

            item = adapter1.getItem(i);

            if (item.getDepth() != 0) continue;

            //Log.d("KLGYN", String.format("%40s%30d%30d", item.getString(), item.getId(), id));

            if (item.getId() == id) break;
        }

        // icollapse kung di nakacollapse
        if (!item.isCollapse())
            adapter1.collapseItem(item);

        // tapos iremove ang device;
        i = adapter1.getPosition(item);
        adapter1.remove(i);
        adapter1.notifyItemRemoved(i);
    }

    private byte[] getPathName(TreeViewItem item, int[] size1) {
        TreeViewItem parent;
        ArrayList<String> array = new ArrayList<>();
        int size, count, len;
        ByteBuffer bb;

        parent = item;

        while (parent != null) {

            array.add(0, parent.getString());
            parent = parent.getParent();
        }

        size = 0;

        for (String str : array)
            size += str.getBytes(StandardCharsets.UTF_16LE).length;

        count = array.size();
        size += ((count + 1) * Short.BYTES);

        size1[0] = size;

        //Log.d("KLGYN", String.format("%10d", size));

        bb = ByteBuffer.allocate(size);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort((short) count);

        for (String str1 : array) {

            len = str1.length();

            bb.putShort((short)len);
            bb.put(str1.getBytes(StandardCharsets.UTF_16LE));
        }

        return bb.array();
    }

    private void requestPermissionStorage() {

        if(Environment.isExternalStorageManager())
            requestPermissionImageMedia();
        else
            launcher1.launch(null);
    }

    private void requestPermissionImageMedia() {

        if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED)
            requestPermissionAudioMedia();
        else
            launcher2.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
    }

    private void requestPermissionAudioMedia() {

        if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED)
            requestPermissionVideoMedia();
        else
            launcher3.launch(android.Manifest.permission.READ_MEDIA_AUDIO);
    }

    private void requestPermissionVideoMedia() {

        if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED)
            requestPermissionBTConnect();
        else
            launcher4.launch(android.Manifest.permission.READ_MEDIA_VIDEO);
    }

    private void requestPermissionBTConnect() {

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
            requestPermissionBTEnability();
        else
            launcher5.launch(Manifest.permission.BLUETOOTH_CONNECT);
    }

    private void requestPermissionBTEnability() {
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adaptor = manager.getAdapter();

        if (adaptor != null && adaptor.isEnabled()) {
            initialize();
        } else {
            launcher6.launch(null);
        }
    }

    private void initialize() {
        access_granted = true;

        String name = Build.MANUFACTURER.toString().toUpperCase() + " " + Build.MODEL.toString();
        client = new Client(this, handler, name);
    }

    @Override
    public void onItemClick(TreeViewItem item) {

        //Log.d("KLGYN", String.format("%20d%20d main item click", item.getDepth(), item.getId()));

        long id, size;
        int[] size1 = new int[1];
        int depth;
        byte[] buffer;

        depth = item.getDepth();
        id = item.getId();

        if (depth == 0) {

            size = Integer.BYTES;

            client.send(Client.FORWARD);
            client.send(id);
            client.send(size);

            client.send(Client.REQ_DRIVE);

        } else {

            buffer = getPathName(item, size1);

            size = 2 * Integer.BYTES + size1[0];

            client.send(Client.FORWARD);
            client.send(id);
            client.send(size);

            client.send(Client.REQ_FOLDER);
            client.send(buffer, size1[0]);

        }
    }

    @Override
    public void onItemSelected(TreeViewItem item) {

    }

    @Override
    public void onItemUnselected() {

    }
}
