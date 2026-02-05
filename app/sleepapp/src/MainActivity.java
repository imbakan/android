
// To put android 6 to sleep programmatically.
//
// Select menu Tools -> Test.
// Android device goes to sleep.

package balikbayan.box.sleepapp;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private boolean access_granted;

    MyDeviceAdminContract contract = new MyDeviceAdminContract();
    ActivityResultLauncher<Void> launcher = registerForActivityResult(contract, new ActivityResultCallback<Boolean>() {

        @Override
        public void onActivityResult(Boolean o) {
            if (o)
                initialize();
            else
                access_granted = false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        requestPermissionDeviceAdmin();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(this, MyDeviceAdminReceiver.class);
        boolean active = devicePolicyManager.isAdminActive(componentName);

        menu.findItem(R.id.mnuTest).setEnabled(access_granted && active);
        menu.findItem(R.id.mnuReset).setEnabled(access_granted && active);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.mnuTest)
            onToolsTest();
        else if (id == R.id.mnuReset)
            onToolsReset();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    private void onToolsTest() {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        devicePolicyManager.lockNow();
    }

    private void onToolsReset() {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(this, MyDeviceAdminReceiver.class);
        devicePolicyManager.removeActiveAdmin(componentName);
    }

    private void requestPermissionDeviceAdmin() {

        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(this, MyDeviceAdminReceiver.class);
        boolean active = devicePolicyManager.isAdminActive(componentName);

        if(active)
            initialize();
        else
            launcher.launch(null);
    }

    private void initialize() {
        access_granted = true;
    }

}
