
//          Open Dialog Box
//          Android 13 Api 33

package balikbayan.box.opendialogbox;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private final int MEDIA_IMAGES_REQUEST_CODE = 1001;
    private final int MEDIA_AUDIO_REQUEST_CODE = 1002;

    private Handler handler;
    private boolean access_granted;

    // nandito ang reply sa function requestPermissionStorage
    AllFilesAccessContract contract = new AllFilesAccessContract();
    ActivityResultLauncher<Void> launcher = registerForActivityResult(contract, new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if(o)
                requestPermissionImageMedia();
            else
                access_granted = false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  +------------------------------------------------------------------------+
        //  |                          handler message queue                         |
        //  +------------------------------------------------------------------------+

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {

                if (msg.what == OpenFileDialog.OPEN_FILE) {
                    onOpenFile(msg);
                } else {
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

        requestPermissionStorage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // nandito ang reply sa function requestPermissionImageMedia
        if(requestCode == MEDIA_IMAGES_REQUEST_CODE)
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                requestPermissionAudioMedia();
            else
                access_granted = false;

        // nandito ang reply sa function requestPermissionAudioMedia
        if(requestCode == MEDIA_AUDIO_REQUEST_CODE)
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                access_granted = true;
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
        menu.findItem(R.id.mnuOpen).setEnabled(access_granted);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.mnuOpen)
            onFileOpen();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    private void onFileOpen() {
        OpenFileDialog dlg = new OpenFileDialog(handler, this, "Internal Storage/DCIM/Camera");
        dlg.show(getSupportFragmentManager(), "open file dialog");
    }

    private void onOpenFile(Message msg) {
        String[] name = (String[]) msg.obj;

        Log.d("out_put", name[0]);
        Log.d("out_put", name[1]);
    }

    private void requestPermissionStorage() {

        if(Environment.isExternalStorageManager())
            requestPermissionImageMedia();
        else
            launcher.launch(null);
    }

    private void requestPermissionImageMedia() {

        if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED)
            requestPermissionAudioMedia();
        else
            requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, MEDIA_IMAGES_REQUEST_CODE);
    }

    private void requestPermissionAudioMedia() {

        if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED)
            access_granted = true;
        else
            requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_AUDIO}, MEDIA_AUDIO_REQUEST_CODE);
    }

}
