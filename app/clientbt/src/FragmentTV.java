package balikbayan.box.bt_client;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentTV extends Fragment {

    private Context context;
    private EventListener listener;
    private RecyclerView recyclerView;

    public FragmentTV(Context context, EventListener listener) {

        Log.d("KLGYN", "constructor");

        this.listener = listener;
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("KLGYN", "on create");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d("KLGYN", "on create view");

        View view = inflater.inflate(R.layout.fragment_t_v, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewTV);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        TreeViewAdapter adapter = new TreeViewAdapter(context, new TreeViewAdapter.EventListener() {

            @Override
            public void onItemClick(TreeViewItem item) {
                listener.onItemClick(item);
            }

            @Override
            public void onItemSelected(TreeViewItem item) {
                listener.onItemSelected(item);
            }

            @Override
            public void onItemUnselected() {
                listener.onItemUnselected();
            }
        });

        recyclerView.setAdapter(adapter);

        return view;
    }

    public TreeViewAdapter getAdapter() {
        return (TreeViewAdapter) recyclerView.getAdapter();
    }

    public interface EventListener {
        void onItemClick(TreeViewItem item);
        void onItemSelected(TreeViewItem item);
        void onItemUnselected();
    }

}
