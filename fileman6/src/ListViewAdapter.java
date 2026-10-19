package balikbayan.box.fileman_a6;

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

public class ListViewAdapter extends RecyclerView.Adapter<ListViewHolder> {

    private Context context;
    private RecyclerView recyclerView;
    private EventListener listener;
    private ArrayList<ListViewItem> array;
    private View view1;
    private int index, color, old_index;
    private boolean restore_highlight;

    public ListViewAdapter(Context context, EventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        view1 = null;
        index = -1;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_item_lv, parent, false);

        TextView textView = view.findViewById(R.id.textLView1);
        color = textView.getCurrentTextColor();

        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        TextView textView1,  textView2,  textView3;
        ConstraintLayout layout;
        ListViewItem item;

        textView1 = holder.getTextView1();
        textView2 = holder.getTextView2();
        textView3 = holder.getTextView3();
        layout = holder.getLayout();

        item = array.get(position);

        textView1.setText(item.getString1());
        textView2.setText(item.getString2());
        textView3.setText(item.getString3());

        // alisin ang highlight ng selected item kung meron
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView1, textView2, textView3;

                if (view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textLView1);
                    textView2 = view1.findViewById(R.id.textLView2);
                    textView3 = view1.findViewById(R.id.textLView3);

                    textView1.setTextColor(color);
                    textView2.setTextColor(color);
                    textView3.setTextColor(color);

                    view1 = null;
                    index = -1;

                    listener.onItemUnselected();
                }
            }
        });

        // ihighlight ng selected item
        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                TextView textView1, textView2, textView3;
                ListViewItem item;

                if (view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textLView1);
                    textView2 = view1.findViewById(R.id.textLView2);
                    textView3 = view1.findViewById(R.id.textLView3);

                    textView1.setTextColor(color);
                    textView2.setTextColor(color);
                    textView3.setTextColor(color);
                }

                view.setBackgroundColor(Color.LTGRAY);

                textView1 = view.findViewById(R.id.textLView1);
                textView2 = view.findViewById(R.id.textLView2);
                textView3 = view.findViewById(R.id.textLView3);

                textView1.setTextColor(Color.BLACK);
                textView2.setTextColor(Color.BLACK);
                textView3.setTextColor(Color.BLACK);

                view1 = view;

                index = holder.getAbsoluteAdapterPosition();
                item = array.get(index);
                listener.onItemSelected(item);

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public void add(ListViewItem item) {
        array.add(item);
    }

    public void addAll(ArrayList<ListViewItem> items) {
        array.addAll(items);
    }

    public void insert(ListViewItem item, int i) {
        array.add(i, item);
    }

    public void remove(ListViewItem item) {
        array.remove(item);
    }

    public void remove(int i) {
        array.remove(i);
    }

    public void clear() {
        array.clear();
    }

    public ListViewItem getItem() {
        return array.get(index);
    }

    public ListViewItem getItem(int i) {
        return array.get(i);
    }

    public int getPosition(ListViewItem item) {
        return array.indexOf(item);
    }

    public void unhighlight() {
        LinearLayoutManager manager;
        TextView textView1, textView2, textView3;
        View view;

        if (index < 0) return;

        manager = (LinearLayoutManager) recyclerView.getLayoutManager();

        view = manager.findViewByPosition(index);
        view.setBackgroundColor(Color.TRANSPARENT);

        textView1 = view.findViewById(R.id.textLView1);
        textView2 = view.findViewById(R.id.textLView2);
        textView3 = view.findViewById(R.id.textLView3);

        textView1.setTextColor(color);
        textView2.setTextColor(color);
        textView3.setTextColor(color);

        index = -1;
        view1 = null;
    }

    public void save() {
        restore_highlight = true;
        old_index = index;
    }

    public void restore() {
        LinearLayoutManager manager;
        TextView textView1, textView2, textView3;
        View view;

        if (restore_highlight) {

            restore_highlight = false;

            manager = (LinearLayoutManager) recyclerView.getLayoutManager();

            view = manager.findViewByPosition(old_index);
            view.setBackgroundColor(Color.LTGRAY);

            textView1 = view.findViewById(R.id.textLView1);
            textView2 = view.findViewById(R.id.textLView2);
            textView3 = view.findViewById(R.id.textLView3);

            textView1.setTextColor(color);
            textView2.setTextColor(color);
            textView3.setTextColor(color);

            index = old_index;
            view1 = view;
        }
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
                TextView textView1, textView2, textView3;
                int i1, i2;

                // kung ang index ay negative walang nakaselect
                if (index < 0) return;

                textView1 = view1.findViewById(R.id.textLView1);
                textView2 = view1.findViewById(R.id.textLView2);
                textView3 = view1.findViewById(R.id.textLView3);

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
                    textView3.setTextColor(color);

                } else {

                    // kung visible, ihighlight ng selected item
                    view1 = manager.findViewByPosition(index);
                    view1.setBackgroundColor(Color.LTGRAY);

                    textView1.setTextColor(Color.BLACK);
                    textView2.setTextColor(Color.BLACK);
                    textView3.setTextColor(Color.BLACK);
                }
            }
        });
    }

    public interface EventListener {
        void onItemSelected(ListViewItem item);
        void onItemUnselected();
    }
}
