package balikbayan.box.fileman;

import android.content.Context;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

public class FragmentTV extends Fragment implements TreeViewAdapter.OnEventListener {

    private Context context;
    private RecyclerView recyclerView;
    private OnEventListener listener;
    private int action, position;

    private ArrayList<String> storage_paths = new ArrayList<>();
    private ArrayList<String> storage_names = new ArrayList<>();

    public FragmentTV(Context context, OnEventListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDeviceStorages(storage_paths, storage_names);

        action = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        TreeViewAdapter adapter;
        adapter = new TreeViewAdapter(context, this);

        for (String str : storage_names)
            adapter.add(new TreeViewItem(null, str, R.raw.drive1, 0, 0));

        View view = inflater.inflate(R.layout.fragment_tv, container, false);
        recyclerView = view.findViewById(R.id.recyclerTView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                switch (action) {
                    case 1 :

                        action = 0;

                        TreeViewHolder holder;
                        ConstraintLayout layout;

                        holder = (TreeViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                        layout = holder.getLayout();
                        layout.performLongClick();

                        break;
                }
            }
        });

        return view;
    }

    @Override
    public void onItemUnselected() {
        listener.onFolderUnselected();
    }

    @Override
    public void onItemSelected(TreeViewItem item) {
        ArrayList<String> array = new ArrayList<>();
        String[] str = new String[2];

        getPathName(item, array);
        str[0] = getPathName(storage_names, storage_paths, array);
        str[1] = getPathName(array);
        listener.onFolderSelected(str);
    }

    @Override
    public void onItemExpanding(TreeViewItem item, TreeViewAdapter adapter) {
        ArrayList<String> array1 = new ArrayList<>();
        ArrayList<String> array2 = new ArrayList<>();
        String pathname, str;
        int padding, depth, pos, count, icon;

        // kunin ang mga directory at ilagay sa ArrayList array2
        getPathName(item, array1);
        pathname = getPathName(storage_names, storage_paths, array1);

        getDirectories(pathname, array2);

        // kung walang laman wag ng tumuloy
        if (array2.isEmpty()) return;

        // ilagay ang array2 sa tree view under TreeViewItem item
        padding = item.getPadding() + TreeViewAdapter.INDENT;
        depth = item.getDepth() + 1;
        pos = adapter.getPosition(item);
        count = array2.size();

        while (!array2.isEmpty()) {
            str = array2.remove(0);
            adapter.insert(new TreeViewItem(item, str, R.raw.folder1, padding, depth), ++pos);
        }

        pos = adapter.getPosition(item) + 1;
        adapter.notifyItemRangeInserted(pos, count);

        // baguhin ang icon ng TreeViewItem item
        icon = item.getIcon(item.getIcon());
        item.setIcon(icon);
        item.setCollapse(false);
        pos = adapter.getPosition(item);

        adapter.notifyItemChanged(pos);
    }

    @Override
    public void onItemCollapsing(TreeViewItem item, TreeViewAdapter adapter) {
        ArrayList<TreeViewItem> array = new ArrayList<>();
        TreeViewItem item1;
        int i, pos, n, depth, icon;

        // kunin ang mga item sa tree view under TreeViewItem item
        // ilagay ang mga nakuha sa ArrayList array
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

        // gamit ang ArrayList array na nakuha sa itaas
        // iremove ang mga item sa tree view under TreeViewItem item
        n = array.size();

        while (!array.isEmpty()) {
            item1 = array.remove(0);
            adapter.remove(item1);
        }

        adapter.notifyItemRangeRemoved(pos, n);

        // baguhin ang icon ng TreeViewItem item
        icon = item.getIcon(item.getIcon());
        item.setIcon(icon);
        item.setCollapse(true);
        pos = adapter.getPosition(item);
        adapter.notifyItemChanged(pos);
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

    // kunin ang mga storage
    // ilagay ang mga nakuha sa ArrayList paths at names
    private void getDeviceStorages(ArrayList<String> paths, ArrayList<String> names) {
        File file;
        String[] str = new String[2];
        String str3;
        int i;

        paths.clear();
        names.clear();

        // internal storage
        file = Environment.getExternalStorageDirectory();

        str[0] = file.toString();
        str[1] = "Internal Storage";

        paths.add(str[0]);
        names.add(str[1]);

        // external storage
        Set<String> volumes = MediaStore.getExternalVolumeNames(context);
        int n = volumes.size();

        if (n > 1) {

            String[] array = new String[n];
            volumes.toArray(array);

            str[0] = "/storage/" + array[1].toUpperCase();
            str[1] = "External Storage";

            paths.add(str[0]);
            names.add(str[1]);
        }
    }

    // kunin ang mga sub directory ng directory pathname
    // ilagay ang mga nakuha sa ArrayList array
    private void getDirectories(String pathname, ArrayList<String> array) {
        File[] files;
        File file;

        file = new File(pathname);
        files = file.listFiles();

        // error checking
        if(files == null) {
            Toast.makeText(getActivity(), "Get directories failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (File file1 : files)
            if (file1.isDirectory())
                array.add(file1.getName());

        array.sort(new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return compareString(s, t1);
            }
        });
    }

    // kunin ang pangalan mula TreeViewItem item papuntang storage device
    // ilagay ang mga nakuha sa ArrayList array
    private void getPathName(TreeViewItem item, ArrayList<String> array) {
        TreeViewItem parent;

        parent = item;

        while (parent != null) {
            array.add(0, parent.getString());
            parent = parent.getParent();
        }
    }

    // buuin ang pathname na nasa ArrayList array
    // ito ay separated ng arrow character
    private String getPathName(ArrayList<String> array) {
        StringBuilder sb;
        String str;
        int i, n;

        n = array.size();
        str = array.get(0);

        sb = new StringBuilder(str);

        for (i=1; i<n; i++)
            sb.append(" ► ").append(array.get(i));

        return sb.toString();
    }

    // buuin ang pathname na nasa ArrayList array
    // ito ay separated ng slash character
    private String getPathName(ArrayList<String> names, ArrayList<String> paths, ArrayList<String> array) {
        StringBuilder sb;
        String str1, str2;
        int i, n;

        n = array.size();
        str1 = array.get(0);

        str2 = "";
        for (i=0; i<names.size(); i++)
            if (str1.compareTo(names.get(i)) == 0) {
                str2 = paths.get(i);
                break;
            }

        sb = new StringBuilder(str2);

        for (i=1; i<n; i++)
            sb.append("/").append(array.get(i));

        return sb.toString();
    }

    public String getPathName() {
        ArrayList<String> array = new ArrayList<>();
        TreeViewAdapter adapter = (TreeViewAdapter) recyclerView.getAdapter();
        TreeViewItem item = adapter.getItem();
        getPathName(item, array);
        String pathname = getPathName(storage_names, storage_paths, array);
        return pathname;
    }

    public void getPathName(String[] str) {
        ArrayList<String> array = new ArrayList<>();
        TreeViewAdapter adapter = (TreeViewAdapter) recyclerView.getAdapter();
        TreeViewItem item = adapter.getItem();
        getPathName(item, array);

        int n = array.size() - 1;
        str[1] = array.remove(n);

        str[0] = getPathName(storage_names, storage_paths, array);
    }

    public void create(String str) {
        TreeViewAdapter adapter;
        TreeViewItem item;
        TreeViewHolder holder;
        ConstraintLayout layout;
        int padding, depth, pos;
        boolean collapse;

        action = 1;

        adapter = (TreeViewAdapter) recyclerView.getAdapter();
        item = adapter.getItem();
        collapse = item.isCollapse();
        position = adapter.getPosition(item);

        padding = item.getPadding() + TreeViewAdapter.INDENT;
        depth = item.getDepth() + 1;
        pos = position + 1;

        if (collapse) {

            onItemExpanding(item, adapter);

        } else {

            adapter.insert(new TreeViewItem(item, str, R.raw.folder1, padding, depth), pos);
            adapter.notifyItemInserted(pos);
        }

        holder = (TreeViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        layout = holder.getLayout();
        layout.performClick();
    }

    public void rename(String str) {
        TreeViewAdapter adapter;
        TreeViewItem item;
        TreeViewHolder holder;
        ConstraintLayout layout;

        action = 1;

        adapter = (TreeViewAdapter) recyclerView.getAdapter();
        item = adapter.getItem();
        position = adapter.getPosition(item);

        item.setString(str);
        adapter.notifyItemChanged(position);

        holder = (TreeViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        layout = holder.getLayout();
        layout.performClick();
    }

    public void remove() {
        TreeViewAdapter adapter;
        TreeViewItem item;
        TreeViewHolder holder;
        ConstraintLayout layout;

        adapter = (TreeViewAdapter) recyclerView.getAdapter();
        item = adapter.getItem();
        position = adapter.getPosition(item);

        holder = (TreeViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        layout = holder.getLayout();
        layout.performClick();

        adapter.remove(item);
        adapter.notifyItemRemoved(position);
    }

    public interface OnEventListener {
        void onFolderSelected(String[] str);
        void onFolderUnselected();
    }
}
