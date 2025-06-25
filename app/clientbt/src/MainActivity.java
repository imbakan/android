package balikbayan.box.client_bt;

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
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText editText1;
    private ViewPager2 viewPager;
    private Client client;
    private Handler handler;
    private boolean access_granted, running;

    // reply sa requestPermissionStorage
    StorageContract contract = new StorageContract();
    ActivityResultLauncher<Void> launcher1 = registerForActivityResult(contract, new ActivityResultCallback<Boolean>() {
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
            requestPermissionBTConnect();
        else
            access_granted = false;
    });

    // reply sa requestPermissionBTConnect
    ActivityResultLauncher<String> launcher4 = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted)
            requestPermissionBTEnability();
        else
            access_granted = false;
    });

    // reply sa requestPermissionBTEnability
    BluetoothContract contract2 = new BluetoothContract();
    ActivityResultLauncher<Void> launcher5 = registerForActivityResult(contract2, new ActivityResultCallback<Boolean>() {
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

        editText1 = findViewById(R.id.editText3);
        editText1.setKeyListener(null);

        viewPager = findViewById(R.id.viewPager);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

            }
        });

        Fragment1 fragment1 = new Fragment1(this, new Fragment1.OnEventListener() {
            @Override
            public void onItemClick() {

            }

            @Override
            public void onItemSelected() {

            }

            @Override
            public void onItemUnselected() {

            }
        });

        Fragment2 fragment2 = new Fragment2();

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        adapter.add(fragment1);
        adapter.add(fragment2);
        viewPager.setAdapter(adapter);

        //  +------------------------------------------------------------------------+
        //  |                          handler message                               |
        //  +------------------------------------------------------------------------+

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {

                switch (msg.what) {
                    case Client.LOG_MESSAGE:
                        onLogMessage(msg);
                        break;
                    case Client.CONNECTING:
                        onConnecting(msg);
                        break;
                    case Client.RUNNING:
                        onRunning(msg);
                        break;
                    case Client.SHUTTING_DOWN:
                        onShuttingDown(msg);
                        break;
                    case Client.DEPOPULATE_BY_DEVICE:
                        onDepopulateByDevice(msg);
                        break;
                    case Client.POPULATE_WITH_DEVICE:
                        onPopulateWithDevice(msg);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };

        //  +------------------------------------------------------------------------+
        //  |                            permission                                  |
        //  +------------------------------------------------------------------------+
        // access order:
        // 1. storage (tap back press arrow nasa baba)
        // 2. image media
        // 3. audio media
        // 4. nearby device
        // 5. turn bluetooth on/off

        requestPermissionStorage();
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
        else if (id == R.id.mnuDebug1)
            onToolsDebug1();
        else if (id == R.id.mnuDebug2)
            onToolsDebug2();
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

        dlg.show(getSupportFragmentManager(), "connect dialog");
    }

    private void onClientDisconnect() {
        client.shutdown();
    }

    private void onFileDownload() {
        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        Fragment1 fragment = (Fragment1) adapter.getItem(0);
    }

    private void onToolsDebug1() {

    }

    private void onToolsDebug2() {
    }

    private void onLogMessage(Message msg) {
        String str1, str2, str3;

        str1 = editText1.getText().toString();
        str2 = (String) msg.obj;
        str3 = str1 + "\n" + str2;
        editText1.setText(str3);
        editText1.setSelection(editText1.length());
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
        Fragment1 fragment = (Fragment1) adapter.getItem(0);
        fragment.clear();
    }

    private void onDepopulateByDevice(Message msg) {
        long id = (long) msg.obj;

        ViewPagerAdapter adapter;
        Fragment1 fragment;

        adapter = (ViewPagerAdapter) viewPager.getAdapter();
        fragment = (Fragment1) adapter.getItem(0);
        fragment.removeRoot(id);
    }

    private void onPopulateWithDevice(Message msg) {
        ArrayList<Attribute> array = (ArrayList<Attribute>) msg.obj;
        ViewPagerAdapter adapter;
        Fragment1 fragment;

        adapter = (ViewPagerAdapter) viewPager.getAdapter();
        fragment = (Fragment1) adapter.getItem(0);
        fragment.addRoot(array);
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
            requestPermissionBTConnect();
        else
            launcher3.launch(android.Manifest.permission.READ_MEDIA_AUDIO);
    }

    private void requestPermissionBTConnect() {

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
            requestPermissionBTEnability();
        else
            launcher4.launch(Manifest.permission.BLUETOOTH_CONNECT);
    }

    private void requestPermissionBTEnability() {
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adaptor = manager.getAdapter();

        if (adaptor != null && adaptor.isEnabled()) {
            initialize();
        } else {
            launcher5.launch(null);
        }
    }

    private void initialize() {

        access_granted = true;

        String name = Build.MANUFACTURER.toString().toUpperCase() + " " + Build.MODEL.toString();
        client = new Client(handler, name);
    }

}
