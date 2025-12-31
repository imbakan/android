
// File Manager
// Ito ay pinogram ko gamit ang moto g play - 2024
// Android version 14

package balikbayan.box.fileman;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements FragmentTV.OnEventListener, FragmentLV.OnEventListener {

    private Toolbar toolbar;
    private ViewPager2 viewPager;
    private EditText editText1, editText2;
    private boolean access_granted, directory_page_selected, file_page_selected, directory_selected, file_selected;

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
            initialize();
        else
            access_granted = false;
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        window.setStatusBarColor(Color.BLACK);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);

        editText1.setKeyListener(null);
        editText2.setKeyListener(null);

        editText1.setText("");
        editText2.setText("");

        viewPager = findViewById(R.id.viewPager);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                directory_page_selected = position == 0;
                file_page_selected = position == 1;
            }
        });

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        FragmentTV fragment1 =  new FragmentTV(this, this);
        FragmentLV fragment2 =  new FragmentLV(this, this);

        adapter.add(fragment1);
        adapter.add(fragment2);
        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(1);
        viewPager.setCurrentItem(0);

        //  +------------------------------------------------------------------------+
        //  |                            permission                                  |
        //  +------------------------------------------------------------------------+

        // access order:
        // 1. storage
        // 2. image media
        // 3. audio media

        requestPermissionStorage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.mnuCreate).setEnabled(access_granted && directory_page_selected && directory_selected);
        menu.findItem(R.id.mnuRename1).setEnabled(access_granted && directory_page_selected && directory_selected);
        menu.findItem(R.id.mnuRemove).setEnabled(access_granted && directory_page_selected && directory_selected);
        menu.findItem(R.id.mnuMove1).setEnabled(access_granted && directory_page_selected && directory_selected);

        menu.findItem(R.id.mnuRename2).setEnabled(access_granted && file_page_selected && file_selected);
        menu.findItem(R.id.mnuDelete).setEnabled(access_granted && file_page_selected && file_selected);
        menu.findItem(R.id.mnuMove2).setEnabled(access_granted && file_page_selected && file_selected);
        //menu.findItem(R.id.mnuCopy).setEnabled(access_granted && file_page_selected && file_selected);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id ==  R.id.mnuCreate)
            onDirectoryCreate();
        else if (id ==  R.id.mnuRename1)
            onDirectoryRename();
        else if (id ==  R.id.mnuRemove)
            onDirectoryRemove();
        else if (id ==  R.id.mnuMove1)
            onDirectoryMove();
        else if (id ==  R.id.mnuRename2)
            onFileRename();
        else if (id ==  R.id.mnuDelete)
            onFileDelete();
        else if (id ==  R.id.mnuMove2)
            onFileMove();
        else if (id ==  R.id.mnuCopy)
            onFileCopy();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public void onFolderSelected(String[] str) {

        String[] text = new String[1];

        directory_selected = true;

        editText1.setText(str[1]);
        editText1.setTag(str[0]);

        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        FragmentLV fragment = (FragmentLV) adapter.getItem(1);
        fragment.populate(str[0], text);

        editText2.setText(text[0]);
    }

    @Override
    public void onFolderUnselected() {
        directory_selected = false;
    }

    @Override
    public void onFileSelected(String str) {
        file_selected = true;
    }

    @Override
    public void onFileUnselected() {
        file_selected = false;
    }

    private void onDirectoryCreate() {

        String str = "New Folder";

        NameDialog dlg = new NameDialog(this, new NameDialog.OnClickListener() {
            @Override
            public void onClick(String str) {

                ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
                FragmentTV fragment = (FragmentTV) adapter.getItem(0);
                String pathname = fragment.getPathName();

                //Log.d("KLGYN", String.format("%-50s     %s", pathname, str));

                File file = new File(pathname, str);

                if (file.mkdirs())
                    fragment.create(str);
                else
                    Toast.makeText(MainActivity.this, "Create directory failed.", Toast.LENGTH_SHORT).show();
            }
        }, R.string.text_name_1, str);

        dlg.show(getSupportFragmentManager(), "create directory dialog");
    }

    private void onDirectoryRename() {
        String[] name = new String[2];

        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        FragmentTV fragment = (FragmentTV) adapter.getItem(0);
        fragment.getPathName(name);

        //Log.d("KLGYN", String.format("%-50s     %s", name[0], name[1]));

        NameDialog dlg = new NameDialog(this, new NameDialog.OnClickListener() {
            @Override
            public void onClick(String str) {

                File file1 = new File(name[0], name[1]);
                File file2 = new File(name[0], str);

                if (file1.renameTo(file2))
                    fragment.rename(str);
                else
                    Toast.makeText(MainActivity.this, "Rename directory failed.", Toast.LENGTH_SHORT).show();
            }
        }, R.string.text_name_2, name[1]);

        dlg.show(getSupportFragmentManager(), "rename directory dialog");
    }

    private void onDirectoryRemove() {
        String[] name = new String[2];
        String str;

        ViewPagerAdapter adapter1 = (ViewPagerAdapter) viewPager.getAdapter();
        FragmentTV fragment1 = (FragmentTV) adapter1.getItem(0);
        fragment1.getPathName(name);

        str = "Are you sure you want to permanently remove folder " + name[1] + " and all of its content ?";

        ConfirmDialog dlg = new ConfirmDialog(this, new ConfirmDialog.OnClickListener() {
            @Override
            public void onClick() {

                File file = new File(name[0], name[1]);

                if (file.delete()) {

                    fragment1.remove();
                    editText1.setText("");
                    editText2.setText("");

                    ViewPagerAdapter adapter2 = (ViewPagerAdapter) viewPager.getAdapter();
                    FragmentLV fragment2 = (FragmentLV) adapter2.getItem(1);
                    fragment2.clear();

                } else {
                    Toast.makeText(MainActivity.this, "Remove directory failed.", Toast.LENGTH_SHORT).show();
                }
            }
        }, R.string.text_name_3, str);

        dlg.show(getSupportFragmentManager(), "remove directory dialog");
    }

    private void onDirectoryMove() {
        String[] name = new String[2];

        ViewPagerAdapter adapter1 = (ViewPagerAdapter) viewPager.getAdapter();
        FragmentTV fragment1 = (FragmentTV) adapter1.getItem(0);
        fragment1.getPathName(name);

        OpenFolderDialog dlg = new OpenFolderDialog(this, new OpenFolderDialog.OnClickListener() {
            @Override
            public void onClick(String str) {

                //Log.d("KLGYN", String.format("%-50s %s", name[0], name[1]));
                //Log.d("KLGYN", String.format("%-50s %s", str, name[1]));

                File file1 = new File(name[0], name[1]);
                File file2 = new File(str, name[1]);

                if (file1.renameTo(file2)) {

                    fragment1.remove();
                    editText1.setText("");
                    editText2.setText("");

                    ViewPagerAdapter adapter2 = (ViewPagerAdapter) viewPager.getAdapter();
                    FragmentLV fragment2 = (FragmentLV) adapter2.getItem(1);
                    fragment2.clear();

                } else {
                    Toast.makeText(MainActivity.this, "Move directory failed.", Toast.LENGTH_SHORT).show();
                }
            }
        }, R.string.text_name_4);

        dlg.show(getSupportFragmentManager(), "move directory dialog");
    }

    private void onFileRename() {

        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        FragmentLV fragment = (FragmentLV) adapter.getItem(1);
        String filename = fragment.getFileName();

        //Log.d("KLGYN", String.format("%-50s     %s", pathname, filename));

        NameDialog dlg = new NameDialog(this, new NameDialog.OnClickListener() {
            @Override
            public void onClick(String str) {
                File file1, file2;
                String pathname;

                pathname = (String) editText1.getTag();

                file1 = new File(pathname, filename);
                file2 = new File(pathname, str);

                if (file1.renameTo(file2))
                    fragment.rename(str);
                else
                    Toast.makeText(MainActivity.this, "Rename file failed.", Toast.LENGTH_SHORT).show();
            }
        }, R.string.text_name_5, filename);

        dlg.show(getSupportFragmentManager(), "rename file dialog");
    }

    private void onFileDelete() {

        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        FragmentLV fragment = (FragmentLV) adapter.getItem(1);
        String filename = fragment.getFileName();
        String str = "Are you sure you want to permanently delete file " + filename + " ?";

        ConfirmDialog dlg = new ConfirmDialog(this, new ConfirmDialog.OnClickListener() {
            @Override
            public void onClick() {

                String pathname = (String) editText1.getTag();
                File file = new File(pathname, filename);

                if (file.delete())
                    fragment.delete();
                else
                    Toast.makeText(MainActivity.this, "Delete file failed.", Toast.LENGTH_SHORT).show();
            }
        }, R.string.text_name_6, str);

        dlg.show(getSupportFragmentManager(), "delete file dialog");
    }

    private void onFileMove() {

        OpenFolderDialog dlg = new OpenFolderDialog(this, new OpenFolderDialog.OnClickListener() {
            @Override
            public void onClick(String str) {
                ViewPagerAdapter adapter;
                FragmentLV fragment;
                File file1, file2;
                String filename;

                String pathname = (String) editText1.getTag();

                adapter = (ViewPagerAdapter) viewPager.getAdapter();
                fragment = (FragmentLV) adapter.getItem(1);
                filename = fragment.getFileName();

                //Log.d("KLGYN", String.format("%-50s     %s", pathname, filename));
                //Log.d("KLGYN", String.format("%-50s     %s", str, filename));

                file1 = new File(pathname, filename);
                file2 = new File(str, filename);

                if (file1.renameTo(file2))
                    fragment.delete();
                else
                    Toast.makeText(MainActivity.this, "Move file failed.", Toast.LENGTH_SHORT).show();
            }
        }, R.string.text_name_7);

        dlg.show(getSupportFragmentManager(), "move file dialog");
    }

    private void onFileCopy() {

        OpenFolderDialog dlg = new OpenFolderDialog(this, new OpenFolderDialog.OnClickListener() {
            @Override
            public void onClick(String str) {

                ProgressDialog dlg = new ProgressDialog(MainActivity.this, new ProgressDialog.OnEventListener() {

                    CopyFileRunnable runnable;

                    @Override
                    public void onCancel() {
                        runnable.stop();
                    }

                    @Override
                    public void onShow(ProgressDialog dialog) {
                        ViewPagerAdapter adapter;
                        FragmentLV fragment;
                        File file1, file2;
                        String filename;

                        String pathname = (String) editText1.getTag();

                        adapter = (ViewPagerAdapter) viewPager.getAdapter();
                        fragment = (FragmentLV) adapter.getItem(1);
                        filename = fragment.getFileName();

                        runnable = new CopyFileRunnable(dialog, filename, pathname, str);
                        new Thread(runnable).start();
                    }
                });

                dlg.show(getSupportFragmentManager(), "progress dialog");
            }
        }, R.string.text_name_8);

        dlg.show(getSupportFragmentManager(), "copy file dialog");

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

        if (checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED)
            initialize();
        else
            launcher4.launch(android.Manifest.permission.READ_MEDIA_VIDEO);
    }

    private void initialize() {
        access_granted = true;
    }

    private class CopyFileRunnable implements Runnable {

        private Thread thread;
        private ProgressDialog dialog;
        private String filename, pathname1, pathname2;

        public CopyFileRunnable(ProgressDialog dialog, String filename, String pathname1, String pathname2) {
            this.dialog = dialog;
            this.filename = filename;
            this.pathname1 = pathname1;
            this.pathname2 = pathname2;
        }

        @Override
        public void run() {

            final int SIZE = 4096;

            File file1, file2;
            FileInputStream fis = null;
            FileOutputStream fos = null;
            byte[] buffer = new byte[SIZE];
            Message msg;
            Handler handler;
            int count, total, percent1, percent2, file_size;

            Log.d("KLGYN", "thread has started");

            Log.d("KLGYN", String.format("%-50s     %s", pathname1, filename));
            Log.d("KLGYN", String.format("%-50s     %s", pathname2, filename));

            thread =  Thread.currentThread();
            handler = dialog.getHandler();

            try {

                file1 = new File(pathname1, filename);
                fis = new FileInputStream(file1);

                file2 = new File(pathname2, filename);
                fos = new FileOutputStream(file2);

                file_size = fis.available();

                total = percent1 = percent2 = 0;
                count = SIZE;

                while (count == SIZE) {

                    if(thread.isInterrupted()) break;

                    count = fis.read(buffer);
                    fos.write(buffer, 0, count);

                    total += count;

                    percent2 = (int)( 100.0 * ((double)total / (double)file_size));

                    if (percent2 > percent1) {

                        //Log.d("KLGYN", "---------------------------------------------------------------------");

                        percent1 = percent2;
                        msg = handler.obtainMessage(ProgressDialog.PROGRESS, percent2);
                        msg.sendToTarget();
                    }

                    //Log.d("KLGYN", String.format("%10d%20d%10d%20d", count , total, percent2, file_size));
                }

                fis.close();
                fos.close();

            } catch (Exception e) {
                Log.d("KLGYN", e.toString());
            }

            dialog.dismiss();

            Log.d("KLGYN", "thread has exited");
        }

        public void stop() {
            if(thread != null)
                thread.interrupt();
        }
    }
}
