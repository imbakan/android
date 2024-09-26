// file manager
// android 6  api 23

package balikbayan.box.fileman06;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TreeViewEventListener, ListViewEventListener {

    private final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1001;

    private RecyclerView recyclerView1, recyclerView2;
    private TextView textView1;
    private EditText editText1;

    private boolean access_granted;
    private String[][] storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText1 = findViewById(R.id.editText1);
        editText1.setKeyListener(null);

        textView1 = findViewById(R.id.textView1);

        recyclerView1 = findViewById(R.id.recyclerView1);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        TreeViewAdapter adapter1 = new TreeViewAdapter(this, (TreeViewEventListener) this);
        recyclerView1.setAdapter(adapter1);

        recyclerView2 = findViewById(R.id.recyclerView2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        ListViewAdapter adapter2 = new ListViewAdapter(this, (ListViewEventListener) this);
        recyclerView2.setAdapter(adapter2);

        //  +------------------------------------------------------------------------+
        //  |                         request permission                             |
        //  +------------------------------------------------------------------------+

        requestPermissionStorage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                access_granted = true;
                initialize();
            } else {
                access_granted = false;
            }
    }

    @Override
    public void onItemExpanding(TreeViewItem item, TreeViewAdapter adapter) {
        TreeViewItem parent;
        ArrayList<String> array =  new ArrayList<>();
        String str;
        StringBuilder path;
        int i, n, pos, padding, depth, icon;
        File[] files;
        File file;

        // buuin ang path name
        parent = item;

        while (parent != null) {
            array.add(0, parent.getString());
            parent = parent.getParent();
        }

        str = array.remove(0);

        for (i=0; i<storage.length; i++)
            if (str.compareTo(storage[i][0]) == 0)
                break;

        path = new StringBuilder(storage[i][1]);

        while (!array.isEmpty()) {
            str = array.remove(0);
            path.append("/").append(str);
        }

        //Log.d("KLGYN", path.toString());

        // kunin ang mga directory
        file = new File(path.toString());
        files = file.listFiles();

        if(files != null)
            for (File file1 : files)
                if (file1.isDirectory())
                    array.add(file1.getName());

        // kung wala, walang gagawin
        if (array.isEmpty()) return;

        // isort
        Collections.sort(array, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return compareString(s, t1);
            }
        });

        // ilagay sa tree view
        padding = item.getPadding() + TreeViewItem.INDENT;
        depth = item.getDepth() + 1;
        pos = adapter.getPosition(item) + 1;
        i = 0;
        n = array.size();

        while (!array.isEmpty()) {
            str = array.remove(0);
            adapter.insert(new TreeViewItem(item, str, R.raw.folder1, padding, depth), pos + i);
        }

        adapter.notifyItemRangeInserted(pos, n);

        // baguhin ang icon ng parent item
        icon = item.changeIcon(item.getIcon());
        item.setIcon(icon);
        item.setCollapse(false);
        pos = adapter.getPosition(item);
        adapter.notifyItemChanged(pos);
    }

    @Override
    public void onItemExpanded(TreeViewItem item, TreeViewAdapter adapter) {
        ArrayList<TreeViewItem> array = new ArrayList<>();
        TreeViewItem item1;
        int i, pos, n, depth, icon;

        // kunin ang lahat na mga child ng parent item
        n = adapter.getItemCount();
        pos = adapter.getPosition(item) + 1;
        depth = item.getDepth();

        for(i=pos; i<n; i++) {
            item1 = adapter.getItem(i);
            if(depth >= item1.getDepth()) break;
            array.add(item1);
        }

        // kung wala, walang gagawin
        if (array.isEmpty()) return;

        // iremove ang mga child na nasa array list
        n = array.size();

        while (!array.isEmpty()) {
            item1 = array.remove(0);
            adapter.remove(item1);
        }

        adapter.notifyItemRangeRemoved(pos, n);

        // baguhin ang icon ng parent item
        icon = item.changeIcon(item.getIcon());
        item.setIcon(icon);
        item.setCollapse(true);
        pos = adapter.getPosition(item);
        adapter.notifyItemChanged(pos);
    }

    @Override
    public void onSelChanged(TreeViewItem item) {
        TreeViewItem parent;
        ListViewAdapter adapter;
        ListViewItem item1;
        ArrayList<String> array1 =  new ArrayList<>();
        ArrayList<ListViewItem> array2 =  new ArrayList<>();
        String str, str1, str2, str3, str4;
        StringBuilder path1, path2;
        int i, n, n1, n2, YYYY, MM, DD, hh, mm, ss;
        File[] files;
        File file;
        Calendar c;

        // buuin ang path name
        parent = item;

        while (parent != null) {
            array1.add(0, parent.getString());
            parent = parent.getParent();
        }

        str = array1.remove(0);

        for (i=0; i<storage.length; i++)
            if (str.compareTo(storage[i][0]) == 0)
                break;

        path1 = new StringBuilder(storage[i][1]);
        path2 = new StringBuilder(storage[i][0]);

        while (!array1.isEmpty()) {
            str = array1.remove(0);
            path1.append("/").append(str);
            path2.append(" \u25ba ").append(str);
        }

        // path name indicator
        editText1.setText(path2.toString());

        // kunin ang mga file
        file = new File(path1.toString());
        files = file.listFiles();

        n1 = n2 = 0;

        if(files != null) {
            for (File file1 : files)
                if (file1.isDirectory()) {
                    ++n1;
                } else {
                    ++n2;

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

                    array2.add(new ListViewItem(str1, str2, str3));
                }
        }

        // bilang indicator
        textView1.setText(String.format(Locale.US, "%d files %d folders", n2, n1));

        // iclear ang list view
        adapter = (ListViewAdapter) recyclerView2.getAdapter();
        n = adapter.getItemCount();
        adapter.clear();
        adapter.notifyItemRangeRemoved(0, n);

        // kung wala, walang gagawin
        if (array2.isEmpty()) return;

        // isort
        Collections.sort(array2, new Comparator<ListViewItem>() {
            @Override
            public int compare(ListViewItem s, ListViewItem t1) {
                return compareString(s, t1);
            }
        });

        // ilagay sa list view
        n = array2.size();
        adapter = (ListViewAdapter) recyclerView2.getAdapter();
        adapter.addAll(array2);
        adapter.notifyItemRangeInserted(0, n);
    }

    @Override
    public void onItemUnselected() {

    }

    @Override
    public void onItemSelected(ListViewItem item) {

    }

    private void initialize() {
        TreeViewAdapter adapter;
        ArrayList<String> array1 = new ArrayList<>();
        ArrayList<String> array2 = new ArrayList<>();
        File file;
        String str, str1, str2;
        int i, n;

        // internal storage
        file = Environment.getExternalStorageDirectory();
        str = file.toString();
        array1.add("Internal Storage");
        array2.add(str);

        // external storage
        file = new File("/storage");
        File[] files = file.listFiles();
        if (files != null) {
            str = files[0].toString();
            array1.add("SD Card");
            array2.add(str);
        }

        n = array1.size();
        storage = new String[n][2];

        i = 0;

        while (!array1.isEmpty()) {

            str1 = array1.remove(0);
            str2 = array2.remove(0);

            storage[i][0] = str1;
            storage[i][1] = str2;

            ++i;
        }

        adapter =  (TreeViewAdapter) recyclerView1.getAdapter();

        for (i=0; i<storage.length; i++)
            adapter.add(new TreeViewItem(null, storage[i][0], R.raw.drive1, 0, 0));
    }

    private void requestPermissionStorage() {
        if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            access_granted = true;
            initialize();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
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

}
