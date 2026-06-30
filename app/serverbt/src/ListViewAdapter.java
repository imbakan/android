package balikbayan.box.bt_server;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewHolder> {

    public static final int NONE = 1001;
    public static final int EDIT = 1002;

    private Context context;
    private EventListener listener;
    private ArrayList<Client> array;
    private View view1;
    private RecyclerView recyclerView;
    private int index, color;
    private int mode, oldIndex;

    public ListViewAdapter(Context context, EventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        view1 = null;
        index = -1;
        mode = NONE;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_item_1, parent, false);

        TextView textView = view.findViewById(R.id.textView1);
        color = textView.getCurrentTextColor();

        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        TextView textView1, textView2;
        ConstraintLayout layout1;
        Client client;

        client = array.get(position);

        textView1 = holder.getTextView1();
        textView1.setText(client.getName());

        textView2 = holder.getTextView2();
        textView2.setText(client.getDate());

        layout1 = holder.getLayout1();

        // alisin ang highlight ng selected item kung meron
        layout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView1, textView2;

                if (view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textView1);
                    textView1.setTextColor(color);

                    textView2 = view1.findViewById(R.id.textView2);
                    textView2.setTextColor(color);
                }

                view1 = null;
                index = -1;

                listener.onItemUnselected();
            }
        });

        // ihighlight ng selected item
        layout1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                TextView textView1, textView2;
                Client client;

                if (view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textView1);
                    textView1.setTextColor(color);

                    textView2 = view1.findViewById(R.id.textView2);
                    textView2.setTextColor(color);
                }

                view.setBackgroundColor(Color.LTGRAY);

                textView1 = view.findViewById(R.id.textView1);
                textView1.setTextColor(Color.DKGRAY);

                textView2 = view.findViewById(R.id.textView2);
                textView2.setTextColor(Color.DKGRAY);

                view1 = view;

                index = holder.getAbsoluteAdapterPosition();
                client = array.get(index);
                listener.onItemSelected(client);

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    // Kapag hinightlight ang cell, ang ibang cell na offscreen ay nahihilight din.
    // Makikita lang 'to kapag iniscroll. Ang function onScroll ay para ayusin to.
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        this.recyclerView = recyclerView;

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                TextView textView1, textView2;
                int i1, i2;

                // kung ang index ay negative walang nakaselect
                if (index < 0) return;

                textView1 = view1.findViewById(R.id.textView1);
                textView2 = view1.findViewById(R.id.textView2);

                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();

                // ito ay para sa partially visible
                i1 = manager.findFirstVisibleItemPosition();
                i2 = manager.findLastVisibleItemPosition();

                // ito ay para sa completely visible
                //i1 = manager.findFirstCompletelyVisibleItemPosition();
                //i2 = manager.findLastCompletelyVisibleItemPosition();

                // alamin kung visible o hindi ang selected item
                if (index < i1 || i2 < index) {

                    // kung hindi visible, alisin ang highlight ng selected item
                    view1.setBackgroundColor(Color.TRANSPARENT);
                    textView1.setTextColor(color);
                    textView2.setTextColor(color);

                } else {

                    // kung visible, ihighlight ng selected item
                    view1 = manager.findViewByPosition(index);
                    view1.setBackgroundColor(Color.LTGRAY);
                    textView1.setTextColor(Color.DKGRAY);
                    textView2.setTextColor(Color.DKGRAY);
                }
            }
        });
    }

    public void add(Client client) {
        array.add(client);
    }

    public void addAll(Collection<Client> c) {
        array.addAll(c);
    }

    public void insert(Client client, int i) {
        array.add(i, client);
    }

    public Client set(Client client, int i) {
        return array.set(i, client);
    }

    public void remove(Client client) {
        array.remove(client);
    }

    public void remove(int i) {
        array.remove(i);
    }

    public void clear() {
        array.clear();
    }

    public Client getItem(int i) {
        return array.get(i);
    }

    public Client getItem() {
        return array.get(index);
    }

    public int getPosition(Client client) {
        return array.indexOf(client);
    }

    public void sort(Comparator<? super Client> c) {
        array.sort(c);
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void highlighItem() {
        LinearLayoutManager manager;
        View view;
        TextView textView1, textView2;

        manager = (LinearLayoutManager) recyclerView.getLayoutManager();

        view = manager.findViewByPosition(oldIndex);
        view.setBackgroundColor(Color.LTGRAY);

        textView1 = view.findViewById(R.id.textView1);
        textView1.setTextColor(Color.DKGRAY);

        textView2 = view.findViewById(R.id.textView2);
        textView2.setTextColor(Color.DKGRAY);

        index = oldIndex;
        view1 = view;
    }

    public void unhighlighItem() {

        if (index < 0) return;

        LinearLayoutManager manager;
        View view;
        TextView textView1, textView2;

        manager = (LinearLayoutManager) recyclerView.getLayoutManager();

        view = manager.findViewByPosition(index);
        view.setBackgroundColor(Color.TRANSPARENT);

        textView1 = view.findViewById(R.id.textView1);
        textView1.setTextColor(color);

        textView2 = view.findViewById(R.id.textView2);
        textView2.setTextColor(color);

        oldIndex = index;
        index = -1;
        view1 = null;
    }

    public interface EventListener {
        void onItemSelected(Client client);
        void onItemUnselected();
    }

}
