
// To wake up an Android 6 device and display an Activity programmatically.
//
// Select menu Tools -> Test.
// Press Power key.
// In Android Studio, watch logcat, count from 0 to 9
// App display again.

package balikbayan.box.wakeapp;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        // kapag nag wake up ang phone, hindi ididisplay nito ang app na 'to
        // para idisplay ito, idisable ang keyguard
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.mnuTest)
            onToolsTest();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    private void onToolsTest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i;

                // 10 seconds delay
                for (i=0; i<10; i++) {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                    }

                    Log.d("KLGYN", String.format("%10d", i));
                }

                // wake up from sleep
                PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,"MyApp:MyWakelockTag");
                wakeLock.acquire();
                wakeLock.release();
            }
        }).start();
    }
}
