package balikbayan.box.client_bt;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class Fragment1 extends Fragment{

    private Context context;
    private OnEventListener listener;
    private RecyclerView recyclerView1;

    public Fragment1(Context context, OnEventListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_1, container, false);

        ListViewAdapter1 adapter = new ListViewAdapter1(context, new ListViewAdapter1.OnEventListener() {
            @Override
            public void onItemClick(ListViewItem1 item) {
                if (item.isCollapse()) {
                    listener.onItemClick();
                } else {
                    collapse(item, false);
                }
            }

            @Override
            public void onItemSelected() {

            }

            @Override
            public void onItemUnselected() {

            }
        });

        recyclerView1 = view.findViewById(R.id.recyclerView1);
        recyclerView1.setLayoutManager(new LinearLayoutManager(context));
        recyclerView1.setAdapter(adapter);

        return view;
    }

    private int toggleIcon(int icon) {
        int icon2;

        if (icon == R.raw.device1)
            icon2 = R.raw.device2;
        else if (icon == R.raw.device2)
            icon2 = R.raw.device1;
        else if (icon == R.raw.drive1)
            icon2 = R.raw.drive2;
        else if (icon == R.raw.drive2)
            icon2 = R.raw.drive1;
        else if (icon == R.raw.folder1)
            icon2 = R.raw.folder2;
        else if (icon == R.raw.folder2)
            icon2 = R.raw.folder1;
        else
            icon2 = R.raw.file1;

        return icon2;
    }

    public void addRoot(ArrayList<Attribute> array) {
        ListViewAdapter1 adapter;
        Attribute attribute;
        int k, n;

        adapter = (ListViewAdapter1) recyclerView1.getAdapter();
        k = adapter.getItemCount();

        while (!array.isEmpty()) {

            attribute = array.remove(0);
            adapter.add(new ListViewItem1(attribute.getString(), attribute.getValue(), R.raw.device2, 0, 0));
        }

        n = adapter.getItemCount();
        adapter.notifyItemRangeInserted(k, n);
    }

    public void removeRoot(long id) {
        ListViewAdapter1 adapter;
        ListViewItem1 item = null;
        int i, n;

        adapter = (ListViewAdapter1) recyclerView1.getAdapter();

        // hanapin ang root node na may id na id
        n = adapter.getItemCount();

        for (i=0; i<n; i++) {

            item = adapter.getItem(i);

            if (item.getDepth() > 0) continue;

            if (item.getId() == id) break;
        }

        // iremove ito kasama ang mga child nito
        collapse(item, true);
    }

    public void expand(ListViewItem1 item, ArrayList<String> array, int icon) {
        ListViewAdapter1 adapter = (ListViewAdapter1) recyclerView1.getAdapter();
        int k, n, pos, depth, indent;
        long id;
        String str;

        item.setCollapse(false);

        id = item.getId();
        pos = adapter.getPosition(item) + 1;
        depth = item.getDepth() + 1;
        indent = item.getIndent() + ListViewAdapter1.INDENT;

        n = array.size();
        k = pos;

        while (!array.isEmpty()) {

            str = array.remove(0);
            adapter.insert(new ListViewItem1(str, id, icon, depth, indent), k++);
        }

        adapter.notifyItemRangeInserted(pos, n);
    }

    public void collapse(ListViewItem1 item, boolean flag) {
        ListViewAdapter1 adapter = (ListViewAdapter1) recyclerView1.getAdapter();
        ListViewItem1 dummy;
        ArrayList<ListViewItem1> array = new ArrayList<>();
        int i, k, n, p, depth;

        item.setCollapse(true);

        depth = item.getDepth();
        n = adapter.getItemCount();
        k = adapter.getPosition(item) + 1;

        // kung true kasama ang parent sa ireremove
        if (flag) {
            array.add(item);
            p = k - 1;
        } else {
            p = k;
        }

        // iremove ang mga child
        for (i=k; i<n; i++) {

            dummy = adapter.getItem(i);

            if (dummy.getDepth() == depth) break;

            array.add(dummy);
        }

        n = array.size();

        while (!array.isEmpty()) {
            dummy = array.remove(0);
            adapter.remove(dummy);
        }

        adapter.notifyItemRangeRemoved(p, n);
    }

    public void clear() {
        ListViewAdapter1 adapter;
        int n;

        adapter = (ListViewAdapter1) recyclerView1.getAdapter();
        n = adapter.getItemCount();
        adapter.clear();
        adapter.notifyItemRangeRemoved(0, n);
    }

    public ListViewItem1 getItem() {
        ListViewAdapter1 adapter;

        adapter = (ListViewAdapter1) recyclerView1.getAdapter();

        return adapter.getItem();
    }

    public interface OnEventListener {
        void onItemClick();
        void onItemSelected();
        void onItemUnselected();
    }
}
