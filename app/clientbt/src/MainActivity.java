
// client-server model
// client side
// Bluetooth
// Android 13 Api 33

package balikbayan.box.clientbt;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TreeViewEventListener, ListViewEventListener {

    private final int MEDIA_IMAGES_REQUEST_CODE = 1001;
    private final int MEDIA_AUDIO_REQUEST_CODE = 1002;
    private final int BLUETOOTH_REQUEST_CODE = 1003;

    private RecyclerView recyclerView1, recyclerView2;
    private TextView textView1;
    private EditText editText1, editText2;

    private Client client;
    private Handler handler;
    private boolean access_granted, running, item_selected;

    AllFilesAccessContract contract1 = new AllFilesAccessContract();

    ActivityResultLauncher<Void> launcher1 = registerForActivityResult(contract1, new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if(o)
                requestPermissionImageMedia();
            else
                access_granted = false;
        }
    });

    BluetoothContract contract2 = new BluetoothContract();

    ActivityResultLauncher<Void> launcher2 = registerForActivityResult(contract2, new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if(o) {
                access_granted = true;
                initialize(getApplicationContext());
            } else {
                access_granted = false;
            }
        }
    });

    OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            Toast.makeText(getApplicationContext(), "TUMATAKBO PA ANG CLIENT", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String str = getResources().getString(R.string.app_name);
        setTitle(str + " - offline");

        item_selected = running = false;

        editText1 = findViewById(R.id.editText1);
        editText1.setKeyListener(null);

        editText2 = findViewById(R.id.editText2);
        editText2.setKeyListener(null);

        textView1 = findViewById(R.id.textView1);

        recyclerView1 = findViewById(R.id.recyclerView1);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        TreeViewAdapter adapter1 = new TreeViewAdapter(this, (TreeViewEventListener) this);
        recyclerView1.setAdapter(adapter1);

        recyclerView2 = findViewById(R.id.recyclerView2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        ListViewAdapter adapter2 = new ListViewAdapter(this, (ListViewEventListener) this);
        recyclerView2.setAdapter(adapter2);

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        //  +------------------------------------------------------------------------+
        //  |                          handler message                               |
        //  +------------------------------------------------------------------------+

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what){
                    case ConnectDialog.CONNECT:                 onConnect(msg);                 break;
                    case Client.LOG_MESSAGE:                    onLogMessage(msg);              break;
                    case Client.CONNECTING:                     onConnecting(msg);              break;
                    case Client.RUNNING:                        onRunning(msg);                 break;
                    case Client.SHUTTING_DOWN:                  onShuttingDown(msg);            break;
                    case Client.POPULATE_WITH_DEVICES:          onPopulateWithDevices(msg);     break;
                    case Client.POPULATE_WITH_DEVICE:           onPopulateWithDevice(msg);      break;
                    case Client.DEPOPULATE_BY_DEVICE:           onDepopulateByDevice(msg);      break;
                    case Client.POPULATE_WITH_DRIVES:           onPopulateWithDrives(msg);      break;
                    case Client.POPULATE_WITH_DIRECTORIES:      onPopulateWithDirectories(msg); break;
                    case Client.POPULATE_WITH_FILES:            onPopulateWithFiles(msg);       break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };

        //  +------------------------------------------------------------------------+
        //  |                         request permission                             |
        //  +------------------------------------------------------------------------+
        // access order:
        // 1. storage
        // 2. image media
        // 3. audio media
        // 4. nearby device
        // 5. bluetooth

        requestPermissionStorage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MEDIA_IMAGES_REQUEST_CODE)
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                requestPermissionAudioMedia();
            else
                access_granted = false;

        if(requestCode == MEDIA_AUDIO_REQUEST_CODE)
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                requestPermissionBluetooth();
            else
                access_granted = false;

        if(requestCode == BLUETOOTH_REQUEST_CODE)
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                requestPermissionBluetooth();
            else
                access_granted = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.mnuConnect).setEnabled(access_granted && !running);
        menu.findItem(R.id.mnuDisconnect).setEnabled(access_granted && running);
        menu.findItem(R.id.mnuDownload).setEnabled(access_granted && item_selected);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.mnuConnect)
            onClientConnect();
        else if(id == R.id.mnuDisconnect)
            onClientDisconnect();
        else if(id == R.id.mnuDownload)
            onToolsDownload();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    private void onClientConnect() {
        ConnectDialog dlg = new ConnectDialog(handler, this);
        dlg.show(getSupportFragmentManager(), "connect dialog");
    }

    private void onClientDisconnect() {
        client.shutdown();
    }

    private void onToolsDownload() {
        TreeViewAdapter adapter1;
        TreeViewItem item1, parent;
        ArrayList<String> array =  new ArrayList<>();
        String str;
        ListViewAdapter adapter2;
        ListViewItem item2;
        long count, size;

        // unang iadd ang file name
        adapter2 = (ListViewAdapter) recyclerView2.getAdapter();
        item2 = adapter2.getSelectedItem();

        array.add(0, item2.getString1());

        // tapos, iadd ang mga path name
        adapter1 = (TreeViewAdapter) recyclerView1.getAdapter();
        item1 = adapter1.getSelectedItem();

        parent = item1;

        while (parent != null) {
            array.add(0, parent.getString());
            parent = parent.getParent();
        }

        // iremove ang device name
        str = array.remove(0);

        // kompyutin ang size na isesend sa destination client
        count = 0;
        for (String str1 : array)
            count += str1.length();

        size = 2L * Integer.BYTES * array.size() + count * Short.BYTES + Integer.BYTES + Long.BYTES;

        // isend sa server
        client.send(Client.FORWARD);
        client.send(item1.getValue());
        client.send(size);

        while (!array.isEmpty()) {

            str = array.remove(0);

            client.send(Client.STRINGS);
            client.send(str);
        }

        client.send(Client.REQUEST_CONTENT);
        client.send(client.getId());
    }

    private void requestPermissionDevice() {

        if(checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
            requestPermissionBluetooth();
        else
            requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_REQUEST_CODE);
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
            requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, MEDIA_IMAGES_REQUEST_CODE);
    }

    private void requestPermissionAudioMedia() {

        if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED)
            requestPermissionDevice();
        else
            requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_AUDIO}, MEDIA_AUDIO_REQUEST_CODE);
    }

    private void requestPermissionBluetooth() {
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adaptor = manager.getAdapter();

        if (adaptor != null && adaptor.isEnabled()) {
            access_granted = true;
            initialize(getApplicationContext());
        } else {
            launcher2.launch(null);
        }
    }

    private void expandTreeView(TreeViewItem item, int icon, ArrayList<String> array) {
        TreeViewAdapter adapter ;
        int i, k, n, icon2, padding, hierarchy;
        String str;

        if (array.isEmpty()) return;

        item.setCollapse(false);
        padding = item.getPadding();
        hierarchy = item.getHierarchy();

        // baguhin ang icon ng parent item
        adapter = (TreeViewAdapter) recyclerView1.getAdapter();
        k = adapter.getPosition(item);
        icon2 = item.changeIcon(item.getIcon());
        item.setIcon(icon2);
        adapter.notifyItemChanged(k);

        // iadd ang mga child sa parent
        k = adapter.getPosition(item) + 1;
        n = array.size();
        i = 0;

        while (!array.isEmpty()) {
            str = array.remove(0);
            adapter.insert(new TreeViewItem(item, str, 0L, icon, padding + TreeViewItem.INDENT, hierarchy + 1), k + i);
            ++i;
        }

        adapter.notifyItemRangeInserted(k, n);
        //adapter.notifyDataSetChanged();
    }

    private void collapseTreeView(TreeViewItem item) {
        TreeViewAdapter adapter;
        ArrayList<TreeViewItem> array = new ArrayList<>();
        TreeViewItem treeViewItem;
        int i, k, n, icon, hierarchy;

        adapter = (TreeViewAdapter) recyclerView1.getAdapter();

        // ilagay sa array list ang lahat ng mga child ng parent item
        n = adapter.getItemCount();
        k = adapter.getPosition(item) + 1;

        hierarchy = item.getHierarchy();

        for(i=k; i<n; i++) {
            treeViewItem = adapter.getItem(i);
            if(hierarchy >= treeViewItem.getHierarchy()) break;
            array.add(treeViewItem);
        }

        // kung wala, skip na 'to
        if(array.isEmpty()) return;

        // baguhin ang icon ng parent item
        i = adapter.getPosition(item);
        icon = item.changeIcon(item.getIcon());
        item.setIcon(icon);
        item.setCollapse(true);
        adapter.notifyItemChanged(i);

        // iremove ang mga child ng parent
        n = array.size();

        while (!array.isEmpty()) {
            treeViewItem = array.remove(0);
            adapter.remove(treeViewItem);
        }

        adapter.notifyItemRangeRemoved(k, n);
        //adapter.notifyDataSetChanged();
    }

    private void initialize(Context context) {

        String name = Build.MANUFACTURER.toString().toUpperCase() + " " + Build.MODEL.toString();
        client = new Client(handler, context, name);
    }

    private void sendMessage(int what, Object obj) {
        Message msg;

        msg = handler.obtainMessage(what, obj);
        msg.sendToTarget();
    }

    private void onLogMessage(Message msg) {
        String str1, str2, str3;

        str1 = editText2.getText().toString();
        str2 = (String) msg.obj;
        str3 = str1 + "\n" + str2;
        editText2.setText(str3);
        editText1.setSelection(editText1.length());
    }

    private void onConnect(Message msg) {
        BluetoothDevice device = (BluetoothDevice) msg.obj;
        client.connect(device);
        new Thread(client).start();
    }

    private void onConnecting(Message msg) {

        running = true;

        String str = getResources().getString(R.string.app_name);
        setTitle(str + " - Connecting");
    }

    private void onRunning(Message msg) {

        running = true;

        String str = getResources().getString(R.string.app_name);
        setTitle(str + " - Online");

        onBackPressedCallback.setEnabled(running);
    }

    private void onShuttingDown(Message msg) {
        Client client = (Client)msg.obj;
        TreeViewAdapter adapter1;
        ListViewAdapter adapter2;
        int n;

        running = false;

        String str = getResources().getString(R.string.app_name);
        setTitle(str + " - Offline");

        adapter1 =  (TreeViewAdapter) recyclerView1.getAdapter();
        n = adapter1.getItemCount();
        adapter1.clear();
        adapter1.notifyItemRangeRemoved(0, n);

        adapter2 =  (ListViewAdapter) recyclerView2.getAdapter();
        n = adapter2.getItemCount();
        adapter2.clear();
        adapter2.notifyItemRangeRemoved(0, n);

        editText1.setText("");
        textView1.setText("");

        onBackPressedCallback.setEnabled(running);

        sendMessage(Client.LOG_MESSAGE, String.format("The client thread 0x%x has exited.", client.getId()));
    }

    private void onPopulateWithDevices(Message msg) {
        ArrayList<Attribute> array = (ArrayList<Attribute>)msg.obj;
        TreeViewAdapter adapter;
        Attribute attribute;
        int i, n;

        adapter =  (TreeViewAdapter) recyclerView1.getAdapter();

        n = array.size();

        for(i=0; i<n; i++) {

            attribute = array.remove(0);
            adapter.add(new TreeViewItem(null, attribute.getString(), attribute.getValue(), R.raw.device1, 0, 0));
        }

        adapter.notifyItemRangeInserted(0, n);

        textView1.setText(String.format(Locale.US, "%d devices", n));
    }

    private void onPopulateWithDevice(Message msg) {
        Attribute attribute = (Attribute)msg.obj;
        TreeViewAdapter adapter;
        int k;

        adapter =  (TreeViewAdapter) recyclerView1.getAdapter();
        adapter.add(new TreeViewItem(null, attribute.getString(), attribute.getValue(), R.raw.device1, 0, 0));
        k = adapter.getItemCount() - 1;
        adapter.notifyItemInserted(k);

        k = adapter.getItemCount();
        textView1.setText(String.format(Locale.US, "%d devices", k));
    }

    private void onDepopulateByDevice(Message msg) {
        long value = (long)msg.obj;
        TreeViewAdapter adapter1;
        ListViewAdapter adapter2;
        TreeViewItem item = null;
        int i, n;

        adapter1 =  (TreeViewAdapter) recyclerView1.getAdapter();
        n = adapter1.getItemCount();

        // kunin ang item na ereremove
        for (i=0; i<n; i++) {
            item = adapter1.getItem(i);
            if (item.getHierarchy() == 0 && item.getValue() == value)
                break;
        }

        // remove muna ang mga child
        collapseTreeView(item);

        // tapos iremove ang mismong item
        adapter1.remove(item);
        adapter1.notifyItemRemoved(i);

        //
        adapter2 =  (ListViewAdapter) recyclerView2.getAdapter();
        n = adapter2.getItemCount();
        adapter2.clear();
        adapter2.notifyItemRangeRemoved(0, n);

        editText1.setText("");
        textView1.setText("");
    }

    private void onPopulateWithDrives(Message msg) {
        ArrayList<String> array = (ArrayList<String>)msg.obj;
        TreeViewAdapter adapter;
        TreeViewItem item;
        String str;

        adapter = (TreeViewAdapter) recyclerView1.getAdapter();
        item = adapter.getSelectedItem();

        expandTreeView(item, R.raw.drive1, array);
    }

    private void onPopulateWithDirectories(Message msg) {
        ArrayList<String> array = (ArrayList<String>)msg.obj;
        TreeViewAdapter adapter;
        TreeViewItem item;
        String str;

        adapter = (TreeViewAdapter) recyclerView1.getAdapter();
        item = adapter.getSelectedItem();

        expandTreeView(item, R.raw.folder1, array);
    }

    private void onPopulateWithFiles(Message msg) {
        ArrayList<String> array = (ArrayList<String>)msg.obj;
        ListViewAdapter adapter;
        String str1, str2, str3;
        int n;

        str1 = array.remove(0);
        textView1.setText(str1);

        adapter = (ListViewAdapter) recyclerView2.getAdapter();
        n = array.size();

        while (!array.isEmpty()) {

            str1 = array.remove(0);
            str2 = array.remove(0);
            str3 = array.remove(0);

           adapter.add(new ListViewItem(str1, str2, str3));
        }

        adapter.notifyItemRangeInserted(0, n);
    }

    @Override
    public void onItemClick(TreeViewItem item) {
        TreeViewItem parent;
        ArrayList<String> array =  new ArrayList<>();
        ListViewAdapter adapter;
        String str;
        long size, str_count, char_count;
        int n;

        if(item.isCollapse()) {

            //mag send sa server
            if (item.getIcon() == R.raw.device1) {

                size = Integer.BYTES + Long.BYTES;

                client.send(Client.FORWARD);
                client.send(item.getValue());           // ito ang destination client para sa request
                client.send(size);

                client.send(Client.REQUEST_DRIVE);
                client.send(client.getId());            // ito ang destination client para sa reply

            } else {

                // kunin ang path name
                parent = item;

                while (parent != null) {
                    array.add(0, parent.getString());
                    parent = parent.getParent();
                }

                // alisin ang unang element
                // ito yung device name
                str = array.remove(0);

                // kompyutin ang size na isesend sa destination client
                str_count = array.size();
                char_count = 0;

                for (String str1 : array)
                    char_count += str1.length();

                size = 2L * Integer.BYTES * str_count + char_count * Short.BYTES + Integer.BYTES + Long.BYTES;

                // isend sa server
                client.send(Client.FORWARD);
                client.send(item.getValue());
                client.send(size);

                while (!array.isEmpty()) {

                    str = array.remove(0);

                    client.send(Client.STRINGS);
                    client.send(str);
                }

                client.send(Client.REQUEST_DIRECTORY);
                client.send(client.getId());
            }
        } else {

            collapseTreeView(item);
            editText1.setText("");
            textView1.setText("");

            adapter =  (ListViewAdapter) recyclerView2.getAdapter();
            n = adapter.getItemCount();
            adapter.clear();
            adapter.notifyItemRangeRemoved(0, n);

        }
    }

    @Override
    public void onItemLongClick(TreeViewItem item) {
        TreeViewItem parent;
        ArrayList<String> array =  new ArrayList<>();
        ListViewAdapter adapter;
        StringBuilder sb;
        String str;
        long size, str_count, char_count;
        int n;

        adapter = (ListViewAdapter) recyclerView2.getAdapter();
        n = adapter.getItemCount();
        adapter.clear();
        adapter.notifyItemRangeRemoved(0, n);

        // kunin ang path name
        parent = item;

        while (parent != null) {
            array.add(0, parent.getString());
            parent = parent.getParent();
        }

        str = array.remove(0);

        // kompyutin ang size na isesend sa destination client
        str_count = array.size();
        char_count = 0;

        for (String str1 : array)
            char_count += str1.length();

        size = 2L * Integer.BYTES * str_count + char_count * Short.BYTES + Integer.BYTES + Long.BYTES;

        // isend sa server
        client.send(Client.FORWARD);
        client.send(item.getValue());
        client.send(size);

        sb = new StringBuilder();

        while (!array.isEmpty()) {

            str = array.remove(0);

            sb.append(str).append(" \u25ba ");

            client.send(Client.STRINGS);
            client.send(str);
        }

        client.send(Client.REQUEST_FILE);
        client.send(client.getId());

        str = sb.toString();
        char_count = str.length();
        editText1.setText(str.substring(0, (int)char_count-3));

        textView1.setText("0 file 0 folder");
    }

    @Override
    public void onItemUnselected() {
        item_selected = false;
    }

    @Override
    public void onItemSelected(ListViewItem item) {
        item_selected = true;
    }
}
