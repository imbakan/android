package balikbayan.box.recycleviewsample1;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
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
    private ArrayList<String> array;
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
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_1, parent, false);

        TextView textView = view.findViewById(R.id.textView1);
        color = textView.getCurrentTextColor();

        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        TextView textView1;
        ConstraintLayout layout1;
        String str;

        str = array.get(position);

        textView1 = holder.getTextView1();
        textView1.setText(str);

        layout1 = holder.getLayout1();

        // alisin ang highlight ng selected item kung meron
        layout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView1;

                if (view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textView1);
                    textView1.setTextColor(color);
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
                TextView textView1;
                String str;

                if (view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textView1);
                    textView1.setTextColor(color);
                }

                view.setBackgroundColor(Color.LTGRAY);

                textView1 = view.findViewById(R.id.textView1);
                textView1.setTextColor(Color.DKGRAY);

                view1 = view;

                index = holder.getAbsoluteAdapterPosition();
                str = array.get(index);
                listener.onItemSelected(str);

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
                TextView textView;
                int i1, i2;

                // kung ang index ay negative walang nakaselect
                if (index < 0) return;

                textView = view1.findViewById(R.id.textView1);

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
                    textView.setTextColor(color);

                } else {

                    // kung visible, ihighlight ng selected item
                    view1 = manager.findViewByPosition(index);
                    view1.setBackgroundColor(Color.LTGRAY);
                    textView.setTextColor(Color.DKGRAY);
                }
            }
        });
    }

    public void add(String str) {
        array.add(str);
    }

    public void addAll(Collection<String> c) {
        array.addAll(c);
    }

    public void insert(String str, int i) {
        array.add(i, str);
    }

    public String set(String str, int i) {
        return array.set(i, str);
    }

    public void remove(String str) {
        array.remove(str);
    }

    public void remove(int i) {
        array.remove(i);
    }

    public void clear() {
        array.clear();
    }

    public String getItem(int i) {
        return array.get(i);
    }

    public String getItem() {
        return array.get(index);
    }

    public int getPosition(String item) {
        return array.indexOf(item);
    }

    public void sort(Comparator<? super String> c) {
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
        TextView textView;

        manager = (LinearLayoutManager) recyclerView.getLayoutManager();

        view = manager.findViewByPosition(oldIndex);
        view.setBackgroundColor(Color.LTGRAY);

        textView = view.findViewById(R.id.textView1);
        textView.setTextColor(Color.DKGRAY);

        index = oldIndex;
        view1 = view;
    }

    public void unhighlighItem() {
        LinearLayoutManager manager;
        View view;
        TextView textView;

        manager = (LinearLayoutManager) recyclerView.getLayoutManager();

        view = manager.findViewByPosition(index);
        view.setBackgroundColor(Color.TRANSPARENT);

        textView = view.findViewById(R.id.textView1);
        textView.setTextColor(color);

        oldIndex = index;
        index = -1;
        view1 = null;
    }

    public interface EventListener {
        void onItemSelected(String str);
        void onItemUnselected();
    }

}
