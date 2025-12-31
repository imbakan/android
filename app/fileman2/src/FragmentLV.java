package balikbayan.box.fileman;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

public class FragmentLV extends Fragment implements ListViewAdapter.OnEventListener {

    private Context context;
    private RecyclerView recyclerView;
    private OnEventListener listener;

    public FragmentLV(Context context, OnEventListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ListViewAdapter adapter;
        adapter = new ListViewAdapter(context, this);

        View view = inflater.inflate(R.layout.fragment_lv, container, false);
        recyclerView = view.findViewById(R.id.recyclerLView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

            }
        });

        return view;
    }

    @Override
    public void onItemUnselected() {
        listener.onFileUnselected();
    }

    @Override
    public void onItemSelected(ListViewItem item) {
        listener.onFileSelected(item.getString1());
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

    // kunin ang mga file ng directory pathname
    // ilagay ang mga nakuha sa ArrayList array
    private void getFiles(String pathname, ArrayList<ListViewItem> array, String[] str) {
        File[] files;
        File file;
        Calendar c;
        SimpleDateFormat sdf;
        int n1, n2;
        long date;
        String str1, str2, str3;

        file = new File(pathname);
        files = file.listFiles();

        // error checking
        if(files == null) {
            Toast.makeText(getActivity(), "Get files failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        n1 = n2 = 0;

        for (File file1 : files)
            if (file1.isFile()) {

                ++n2;

                str1 = file1.getName();

                c = Calendar.getInstance();
                c.setTimeInMillis(file1.lastModified());
                date = c.getTimeInMillis();
                sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
                str2 = sdf.format(date);

                str3 = String.format(Locale.US, "%,d", file1.length());

                array.add(new ListViewItem(str1, str2, str3));
            } else {
                ++n1;
            }

        array.sort(new Comparator<ListViewItem>() {
            @Override
            public int compare(ListViewItem listViewItem, ListViewItem t1) {
                return compareObject(listViewItem, t1);
            }
        });

        str[0] = String.format(Locale.US, "%d Files    %d Folders", n2, n1);
    }

    public String getFileName() {
        ListViewAdapter adapter = (ListViewAdapter) recyclerView.getAdapter();
        return adapter.getItem().getString1();
    }

    public void populate(String pathname, String[] str) {

        ArrayList<ListViewItem> array = new ArrayList<>();
        ListViewAdapter adapter;
        int n;

        // alisin muna lahat ng laman ng RecyclerView
        adapter = (ListViewAdapter) recyclerView.getAdapter();
        n = adapter.getItemCount();
        adapter.clear();
        adapter.notifyItemRangeRemoved(0, n);

        // kunin ang mga file under directory pathname
        // ilagay ang mga nakuha sa ArrayList array
        getFiles(pathname, array, str);

        // kung walang nakuha, alis na dito
        if (array.isEmpty()) return;

        // ilagay ang ArrayList array sa RecyclerView
        n = array.size();
        adapter.addAll(array);
        adapter.notifyItemRangeInserted(0, n);
    }

    public void clear() {
        ListViewAdapter adapter;
        int n;

        adapter = (ListViewAdapter) recyclerView.getAdapter();
        n = adapter.getItemCount();
        adapter.clear();
        adapter.notifyItemRangeRemoved(0, n);
    }

    public void rename(String str) {
        ListViewAdapter adapter;
        ListViewItem item;
        ListViewHolder holder;
        ConstraintLayout layout;
        int pos;

        adapter = (ListViewAdapter) recyclerView.getAdapter();
        item = adapter.getItem();
        pos = adapter.getPosition(item);

        item.setString1(str);
        adapter.notifyItemChanged(pos);

        holder = (ListViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
        layout = holder.getLayout();
        layout.performClick();
    }

    public void delete() {
        ListViewAdapter adapter;
        ListViewItem item;
        ListViewHolder holder;
        ConstraintLayout layout;
        int pos;

        adapter = (ListViewAdapter) recyclerView.getAdapter();
        item = adapter.getItem();
        pos = adapter.getPosition(item);

        holder = (ListViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
        layout = holder.getLayout();
        layout.performClick();

        adapter.remove(item);
        adapter.notifyItemRemoved(pos);
    }

    public interface OnEventListener {
        void onFileSelected(String str);
        void onFileUnselected();
    }
}
