
// File manager
// Android 6  Api 23

package balikbayan.box.fileman_a6;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText editText1, editText2;
    private RecyclerView recyclerView1, recyclerView2;
    private ArrayList<String> storage_path;
    private ArrayList<String> storage_name;
    private boolean access_granted, directory_selected, file_selected;

    // reply sa requestPermissionStorage
    ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted)
            initialize(recyclerView1);
        else
            access_granted = false;
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        //  +------------------------------------------------------------------------+
        //  |                             edit text                                  |
        //  +------------------------------------------------------------------------+


        editText1 = findViewById(R.id.editText1);
        editText1.setKeyListener(null);

        editText2 = findViewById(R.id.editText2);
        editText2.setKeyListener(null);

        editText1.setText("");
        editText2.setText("");

        //  +------------------------------------------------------------------------+
        //  |                        tree recycler view                              |
        //  +------------------------------------------------------------------------+

        TreeViewAdapter adapter1 = new TreeViewAdapter(this, new TreeViewAdapter.EventListener() {
            @Override
            public void onItemChanged(TreeViewItem item, TreeViewAdapter adapter) {

                ArrayList<String> paths = new ArrayList<>();
                ArrayList<String> directory = new ArrayList<>();
                String[] pathname = new String[2];

                fillArrayWithFolderName(paths, item);
                getPathName(pathname, paths, storage_path, storage_name);
                fillArrayWithDirectory(pathname[0], directory);

                if (directory.isEmpty())
                    Toast.makeText(MainActivity.this, "No more sub folder.", Toast.LENGTH_SHORT).show();
                else
                    adapter.expandItem(item, directory, R.raw.folder1);
            }

            @Override
            public void onItemSelected(TreeViewItem item) {

                ArrayList<String> paths = new ArrayList<>();
                ArrayList<ListViewItem> array = new ArrayList<>();
                String[] pathname = new String[2];
                long[] total = new long[1];

                fillArrayWithFolderName(paths, item);
                getPathName(pathname, paths, storage_path, storage_name);

                fillArrayWithFile(pathname[0], array, total);
                populateWithFile(recyclerView2, array);

                editText1.setText(pathname[1]);
                editText1.setTag(pathname[0]);
                editText2.setText(String.format(Locale.US, "Total %,d files [%,d bytes]", array.size(), total[0]));

                 directory_selected = true;
            }

            @Override
            public void onItemUnselected() {
                directory_selected = false;
            }
        });

        recyclerView1 = findViewById(R.id.recyclerView1);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        recyclerView1.setAdapter(adapter1);

        recyclerView1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    TreeViewAdapter adapter = (TreeViewAdapter) recyclerView1.getAdapter();
                    adapter.restore(recyclerView1);
                } catch (RuntimeException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //  +------------------------------------------------------------------------+
        //  |                        list recycler view                              |
        //  +------------------------------------------------------------------------+

        ListViewAdapter adapter2 = new ListViewAdapter(this, new ListViewAdapter.EventListener() {
            @Override
            public void onItemSelected(ListViewItem item) {
                file_selected = true;
            }

            @Override
            public void onItemUnselected() {
                file_selected = false;
            }
        });

        recyclerView2 = findViewById(R.id.recyclerView2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setAdapter(adapter2);

        recyclerView2.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ListViewAdapter adapter = (ListViewAdapter) recyclerView2.getAdapter();
                adapter.restore();
            }
        });

        //  +------------------------------------------------------------------------+
        //  |                             permission                                 |
        //  +------------------------------------------------------------------------+

        requestPermissionStorage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.mnuCreate).setEnabled(access_granted && directory_selected);
        menu.findItem(R.id.mnuRename1).setEnabled(access_granted && directory_selected);
        menu.findItem(R.id.mnuRemove).setEnabled(access_granted && directory_selected);
        menu.findItem(R.id.mnuMove1).setEnabled(access_granted && directory_selected);

        menu.findItem(R.id.mnuRename2).setEnabled(access_granted && file_selected);
        menu.findItem(R.id.mnuDelete).setEnabled(access_granted && file_selected);
        menu.findItem(R.id.mnuMove2).setEnabled(access_granted && file_selected);
        menu.findItem(R.id.mnuOpen).setEnabled(access_granted && file_selected);

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
        else if (id ==  R.id.mnuOpen)
            onFileOpen();
        else if (id ==  R.id.mnuRename2)
            onFileRename();
        else if (id ==  R.id.mnuDelete)
            onFileDelete();
        else if (id ==  R.id.mnuMove2)
            onFileMove();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    private void onDirectoryCreate() {

        EditTextDialog dlg = new EditTextDialog(this, new EditTextDialog.OnClickListener() {
            @Override
            public void onClick(String str) {
                TreeViewAdapter adapter;
                TreeViewItem item, parent;
                ArrayList<String> arrayList = new ArrayList<>();
                ArrayList<String> directory = new ArrayList<>();
                File file;
                String[] pathname = new String[2];
                int padding, depth, pos;

                adapter = (TreeViewAdapter) recyclerView1.getAdapter();
                item = adapter.getItem();
                fillArrayWithFolderName(arrayList, item);
                getPathName(pathname, arrayList, storage_path, storage_name);

                file = new File(pathname[0], str);

                if (file.mkdirs()) {

                    // alisin muna ang highlight ng item
                    // at ibalik ito sa RecyclerView.getViewTreeObserver().addOnGlobalLayoutListener
                    adapter.save();
                    adapter.unhighlight();

                    if (item.isCollapse()) {

                        // kung naka collapse di na kailangan iadd ang bagong directory
                        // maaadd na ito pag inexpand
                        fillArrayWithDirectory(pathname[0], directory);

                        if (!directory.isEmpty())
                            adapter.expandItem(item, directory, R.raw.folder1);

                    } else {

                        // kung naka expand na iadd ang bagong directory
                        parent = item.getParent();
                        padding = item.getPadding() + TreeViewAdapter.INDENT;
                        depth = item.getDepth() + 1;
                        pos = adapter.getPosition(item) + 1;

                        adapter.insert(new TreeViewItem(parent, str, R.raw.folder1, padding, depth), pos);
                        adapter.notifyItemInserted(pos);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Create directory failed.", Toast.LENGTH_SHORT).show();
                }

            }
        }, R.string.text_name_1, getResources().getString(R.string.text_name_0));

        dlg.show(getSupportFragmentManager(), "create directory dialog");
    }

    private void onDirectoryRename() {
        TreeViewAdapter adapter;
        TreeViewItem item;
        String str;

        adapter = (TreeViewAdapter) recyclerView1.getAdapter();
        item = adapter.getItem();
        str = item.getString();

        EditTextDialog dlg = new EditTextDialog(this, new EditTextDialog.OnClickListener() {
            @Override
            public void onClick(String str) {
                File file1, file2;
                ArrayList<String> arrayList = new ArrayList<>();
                String[] pathname = new String[2];
                int pos;
                String pathname1, pathname2;

                fillArrayWithFolderName(arrayList, item);
                getPathName(pathname, arrayList, storage_path, storage_name);
                pathname1 = pathname[0];

                arrayList.remove(0);
                getPathName(pathname, arrayList, storage_path, storage_name);
                pathname2 = pathname[0] + "/" + str;

                //Log.d("KLGYN", pathname1);
                //Log.d("KLGYN", pathname2);

                file1 = new File(pathname1);
                file2 = new File(pathname2);

                if (file1.renameTo(file2)) {

                    adapter.save();
                    adapter.unhighlight();

                    pos = adapter.getPosition(item);
                    item.setString(str);
                    adapter.notifyItemChanged(pos);

                } else {
                    Toast.makeText(MainActivity.this, "Rename directory failed.", Toast.LENGTH_SHORT).show();
                }

            }
        }, R.string.text_name_2, str);

        dlg.show(getSupportFragmentManager(), "rename directory dialog");
    }

    private void onDirectoryRemove() {
        TreeViewAdapter adapter;
        TreeViewItem item;
        String str, msg;

        adapter = (TreeViewAdapter) recyclerView1.getAdapter();
        item = adapter.getItem();
        str = item.getString();

        msg = "Are you sure you want to permanently remove folder " + str + " and all of its content ?";

        MessageDialog dlg = new MessageDialog(this, new MessageDialog.OnClickListener() {
            @Override
            public void onClick() {
                File file;
                ArrayList<String> arrayList = new ArrayList<>();
                String[] pathname = new String[2];
                int pos;

                fillArrayWithFolderName(arrayList, item);
                getPathName(pathname, arrayList, storage_path, storage_name);

                file = new File(pathname[0]);

                if (file.delete()) {

                    // alisin ang hightlight bago iremove
                    adapter.unhighlight();
                    pos = adapter.getPosition(item);
                    adapter.remove(pos);
                    adapter.notifyItemRemoved(pos);

                } else {
                    Toast.makeText(MainActivity.this, "Remove directory failed.", Toast.LENGTH_SHORT).show();
                }
            }
        }, R.string.text_name_3, msg);

        dlg.show(getSupportFragmentManager(), "remove directory dialog");
    }

    private void onDirectoryMove() {
        SelectFolderDialog dlg = new SelectFolderDialog(this, new SelectFolderDialog.OnClickListener() {
            @Override
            public void onClick(String str) {
                File file1, file2;
                TreeViewAdapter adapter;
                TreeViewItem item;
                ArrayList<String> arrayList = new ArrayList<>();
                String[] pathname = new String[2];
                String pathname1, pathname2;
                int pos;

                adapter = (TreeViewAdapter) recyclerView1.getAdapter();
                item = adapter.getItem();

                fillArrayWithFolderName(arrayList, item);
                getPathName(pathname, arrayList, storage_path, storage_name);
                pathname1 = pathname[0];
                pathname2 = str + "/" + item.getString();

                //Log.d("KLGYN", pathname1);
                //Log.d("KLGYN", pathname2);

                file1 = new File(pathname1);
                file2 = new File(pathname2);

                if (file1.renameTo(file2)) {

                    adapter.unhighlight();

                    if (!item.isCollapse())
                        adapter.collapseItem(item);

                    pos = adapter.getPosition(item);
                    adapter.remove(pos);
                    adapter.notifyItemRemoved(pos);

                } else {
                    Toast.makeText(MainActivity.this, "Move directory failed.", Toast.LENGTH_SHORT).show();
                }

            }
        }, R.string.text_name_4);

        dlg.show(getSupportFragmentManager(), "move directory dialog");
    }

    private void onFileOpen() {
        ListViewAdapter adapter;
        ListViewItem item;
        File file;
        String filename;

        adapter = (ListViewAdapter) recyclerView2.getAdapter();
        item = adapter.getItem();
        filename = (String) editText1.getTag() + "/" + item.getString1();
        file = new File(filename);

        playVideo(this, file);

    }

    private void onFileRename() {
        ListViewAdapter adapter;
        ListViewItem item;
        String str1;

        adapter = (ListViewAdapter) recyclerView2.getAdapter();
        item = adapter.getItem();
        str1 = item.getString1();

        EditTextDialog dlg = new EditTextDialog(this, new EditTextDialog.OnClickListener() {
            @Override
            public void onClick(String str2) {
                File file1, file2;
                int pos;
                String filename1, filename2;

                filename1 = (String) editText1.getTag() + "/" + str1;
                filename2 = (String) editText1.getTag()+ "/" + str2;

                //Log.d("KLGYN", filename1);
                //Log.d("KLGYN", filename2);

                file1 = new File(filename1);
                file2 = new File(filename2);

                if (file1.renameTo(file2)) {

                    adapter.save();
                    adapter.unhighlight();

                    pos = adapter.getPosition(item);
                    item.setString1(str2);
                    adapter.notifyItemChanged(pos);

                } else {
                    Toast.makeText(MainActivity.this, "Rename file failed.", Toast.LENGTH_SHORT).show();
                }

            }
        }, R.string.text_name_5, str1);

        dlg.show(getSupportFragmentManager(), "rename file dialog");
    }

    private void onFileDelete() {
        ListViewAdapter adapter;
        ListViewItem item;
        String str, msg;

        adapter = (ListViewAdapter) recyclerView2.getAdapter();
        item = adapter.getItem();
        str = item.getString1();

        msg = "Are you sure you want to permanently delete file " + str + " ?";

        MessageDialog dlg = new MessageDialog(this, new MessageDialog.OnClickListener() {
            @Override
            public void onClick() {
                File file;
                int pos;
                String filename;

                filename = (String) editText1.getTag() + "/" + str;
                file = new File(filename);

                if (file.delete()) {

                    adapter.unhighlight();
                    pos = adapter.getPosition(item);
                    adapter.remove(pos);
                    adapter.notifyItemRemoved(pos);

                } else  {
                    Toast.makeText(MainActivity.this, "Delete file failed.", Toast.LENGTH_SHORT).show();
                }
            }
        }, R.string.text_name_6, msg);

        dlg.show(getSupportFragmentManager(), "delete file dialog");
    }

    private void onFileMove() {

        SelectFolderDialog dlg = new SelectFolderDialog(this, new SelectFolderDialog.OnClickListener() {
            @Override
            public void onClick(String str) {
                ListViewAdapter adapter;
                ListViewItem item;
                File file1, file2;
                int pos;
                String filename, filename1, filename2;

                adapter = (ListViewAdapter) recyclerView2.getAdapter();
                item = adapter.getItem();
                filename = item.getString1();

                filename1 = (String) editText1.getTag() + "/" + filename;
                filename2 = str + "/" + filename;

                //Log.d("KLGYN", filename1);
                //Log.d("KLGYN", filename2);

                file1 = new File(filename1);
                file2 = new File(filename2);

                if (file1.renameTo(file2)) {

                    adapter.unhighlight();
                    pos = adapter.getPosition(item);
                    adapter.remove(pos);
                    adapter.notifyItemRemoved(pos);

                } else {
                    Toast.makeText(MainActivity.this, "Move file failed.", Toast.LENGTH_SHORT).show();
                }

            }
        }, R.string.text_name_7);

        dlg.show(getSupportFragmentManager(), "move file dialog");
    }

    private void requestPermissionStorage() {

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            initialize(recyclerView1);
        else
            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void initialize(RecyclerView recyclerView) {

        access_granted = true;

        storage_path = new ArrayList<>();
        storage_name = new ArrayList<>();

        fillArrayWithStorage(storage_path, storage_name);
        populateWithStorage(recyclerView, storage_name);
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

    private int compareObject(ListViewItem obj1, ListViewItem obj2) {
        String str1, str2;

        str1 = obj1.getString1();
        str2 = obj2.getString1();

        return compareString(str1, str2);
    }

    private void fillArrayWithStorage(ArrayList<String> storage_path, ArrayList<String> storage_name) {
        File file;
        File[] files;
        String[] str = new String[2];

        storage_path.clear();
        storage_name.clear();

        // internal storage
        file = Environment.getExternalStorageDirectory();

        str[0] = file.toString();
        str[1] = "Internal Storage";

        storage_path.add(str[0]);
        storage_name.add(str[1]);

        // external storage
        //
        // files.length equal 3                                 files.length equal 2
        //
        // index zero       /storage/32BB-3E9F                  index zero      /storage/emulated
        // index one        /storage/emulated                   index one       /storage/self
        // index two        /storage/self
        //
        file = new File("/storage");
        files = file.listFiles();

        if (files != null) {

            if (files.length == 3) {

                str[0] = files[0].toString();
                str[1] = "External Storage";

                storage_path.add(str[0]);
                storage_name.add(str[1]);
            }
        }
    }

    private void fillArrayWithDirectory(String pathname, ArrayList<String> array) {
        File[] files;
        File file;

        file = new File(pathname);
        files = file.listFiles();

        // error checking
        if(files == null) return;

        for (File file1 : files)
            if (file1.isDirectory())
                array.add(file1.getName());

        // sa android 6, walang sort function ang class ArrayList
        // kaya class Collection ang gamitin
        Collections.sort(array, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return compareString(s, t1);
            }
        });
    }

    private void fillArrayWithFile(String pathname, ArrayList<ListViewItem> array, long[] total) {
        File[] files;
        File file;
        Calendar c;
        SimpleDateFormat sdf;
        long date;
        String str1, str2, str3;

        array.clear();

        file = new File(pathname);
        files = file.listFiles();

        // error checking
        if(files == null) return;

        total[0] = 0;

        for (File file1 : files)
            if (!file1.isDirectory()) {

                total[0] += file1.length();

                str1 = file1.getName();

                c = Calendar.getInstance();
                c.setTimeInMillis(file1.lastModified());
                date = c.getTimeInMillis();
                sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
                str2 = sdf.format(date);

                str3 = String.format(Locale.US, "%,d", file1.length());

                array.add(new ListViewItem(str1, str2, str3));
            }

        // sa android 6, walang sort function ang class ArrayList
        // kaya class Collection ang gamitin
        Collections.sort(array, new Comparator<ListViewItem>() {
            @Override
            public int compare(ListViewItem listViewItem, ListViewItem t1) {
                return compareObject(listViewItem, t1);
            }
        });
    }

    // ilagay sa array ang mga folder name
    // ito ay kukunin mula child folder papuntang parent folder
    //
    // Tree View                        Array List
    // Drive                            Directory 3
    //   Directory 1                    Directory 2
    //     Directory 2                  Directory 1
    //       Directory 3                Drive
    //
    private void fillArrayWithFolderName(ArrayList<String> array, TreeViewItem item) {
        TreeViewItem parent;

        array.clear();

        parent = item;

        while (parent != null)  {
            array.add(parent.getString());
            parent = parent.getParent();
        }
    }

    private void getPathName(String[] pathname, ArrayList<String> folder_name, ArrayList<String> storage_path, ArrayList<String> storage_name) {
        StringBuilder sb1, sb2;
        int i, m, n;
        String str, str1, str2, str3;

        // ang storage name ay nasa huli ng array list
        // ilagay ito sa variable str1
        n = folder_name.size();
        str1 = folder_name.get(n-1);

        // palitan ng path name ang storage name na nakuha sa itaas
        // ilagay ito sa variable str3
        m = storage_name.size();

        str3 = "";

        for (i=0; i<m; i++) {

            str2 = storage_name.get(i);

            if (str1.compareTo(str2) == 0) {
                str3 = storage_path.get(i);
                break;
            }
        }

        // buuin ang pathname ilagay ito sa variable pathname[0]
        // ang variable pathname[1] ay para sa EditText for display only
        sb1 = new StringBuilder(str3);
        sb2 = new StringBuilder(str1);

        for (i=n-2; i>-1; --i) {
            str = folder_name.get(i);

            sb1.append("/").append(str);
            sb2.append(" ► ").append(str);
        }

        pathname[0] = sb1.toString();
        pathname[1] = sb2.toString();
    }

    private void populateWithStorage(RecyclerView recyclerView, ArrayList<String> array) {
        TreeViewAdapter adapter;
        int count;

        adapter = (TreeViewAdapter) recyclerView.getAdapter();

        for (String str : array)
            adapter.add(new TreeViewItem(null, str, R.raw.drive1, 8, 0));

        count = array.size();
        adapter.notifyItemRangeInserted(0, count);
    }

    private void populateWithFile(RecyclerView recyclerView, ArrayList<ListViewItem> array) {
        ListViewAdapter adapter;
        int count;

        adapter = (ListViewAdapter) recyclerView.getAdapter();

        adapter.unhighlight();

        count = adapter.getItemCount();
        adapter.clear();
        adapter.notifyItemRangeRemoved(0, count);

        count = array.size();
        adapter.addAll(array);
        adapter.notifyItemRangeInserted(0, count);
    }

    public void playVideo(Context context, File file) {

        // 1. Create a Uri from the file object
        Uri uri = Uri.fromFile(file);

        // 2. Create the View Intent
        Intent intent = new Intent(Intent.ACTION_VIEW);

        // 3. Set the data URI and the explicit MIME type
        intent.setDataAndType(uri, "video/*");

        // 4. (Optional but recommended) Ensure a chooser pops up if there are multiple players
        Intent chooser = Intent.createChooser(intent, "Open video with");

        // 5. Verify the intent can resolve to an app before starting to prevent crashes
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooser);
        }
    }

}
