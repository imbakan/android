package balikbayan.box.client_lan;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final int port = 54105;
    private final String default_ip = "192.168.1.5";

    private Toolbar toolbar;
    private EditText editText1;
    private ViewPager2 viewPager;
    private Client client;
    private Handler handler;
    private boolean running;

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

        Fragment1 fragment1 =  new Fragment1(this, new Fragment1.OnEventListener() {
            @Override
            public void onItemClick() {
                onToolsDebug2();
            }

            @Override
            public void onItemSelected() {

            }

            @Override
            public void onItemUnselected() {

            }
        });

        Fragment2 fragment2 =  new Fragment2();

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

                switch (msg.what){
                    case Client.LOG_MESSAGE:                    onLogMessage(msg);              break;
                    case Client.CONNECTING:                     onConnecting(msg);              break;
                    case Client.RUNNING:                        onRunning(msg);                 break;
                    case Client.SHUTTING_DOWN:                  onShuttingDown(msg);            break;
                    case Client.DEPOPULATE_BY_DEVICE:           onDepopulateByDevice(msg);      break;
                    case Client.POPULATE_WITH_DEVICE:           onPopulateWithDevice(msg);      break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };

        // ITO AY ILALAGAY SA INITIALIZE
        String name = Build.MANUFACTURER.toString().toUpperCase() + " " + Build.MODEL.toString();
        client = new Client(handler, port, name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.mnuConnect).setEnabled(!running);
        menu.findItem(R.id.mnuDisconnect).setEnabled(running);
        menu.findItem(R.id.mnuDownload).setEnabled(false);

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
            onFileDownload();
        else if(id == R.id.mnuDebug1)
            onToolsDebug1();
        else if(id == R.id.mnuDebug2)
            onToolsDebug2();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    private void onClientConnect() {

        ConnectDialog dlg = new ConnectDialog(default_ip, new ConnectDialog.OnClickListener() {
            @Override
            public void onClick(String str) {
                client.connect(str);
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
        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        Fragment1 fragment = (Fragment1) adapter.getItem(0);
        ListViewItem1 item = fragment.getItem();
        ArrayList<String> array = new ArrayList<>();

        array.add("Isa");
        array.add("Dalawa");
        array.add("Tatlo");
        array.add("apat");
        array.add("lima");

        fragment.expand(item, array, R.raw.drive1);
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

}
