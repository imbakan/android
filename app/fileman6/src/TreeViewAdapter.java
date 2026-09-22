package balikbayan.box.fileman_a6;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TreeViewAdapter extends RecyclerView.Adapter<TreeViewHolder> {

    public static final int INDENT = 32;

    private Context context;
    private EventListener listener;
    private ArrayList<TreeViewItem> array;
    private View view1;
    private int index, color, old_index;
    private boolean restore_highlight;

    public TreeViewAdapter(Context context, EventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        view1 = null;
        index = -1;
    }

    @NonNull
    @Override
    public TreeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_item_tv, parent, false);

        TextView textView = view.findViewById(R.id.textTView);
        color = textView.getCurrentTextColor();

        return new TreeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TreeViewHolder holder, int position) {
        ImageView imageView;
        TextView textView;
        ConstraintLayout layout;
        TreeViewItem item;

        imageView = holder.getImageView();
        textView = holder.getTextView();
        layout = holder.getLayout();

        item = array.get(position);

        imageView.setImageResource(item.getIcon());
        imageView.setPadding(item.getPadding(), 0, 0,0);

        textView.setText(item.getString());

        // alisin ang highlight ng selected item kung meron
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView1;
                TreeViewItem item;
                int i;

                if (view1 == null) {

                    i = holder.getAbsoluteAdapterPosition();
                    item = array.get(i);

                    if (item.isCollapse())
                        listener.onItemChanged(item, TreeViewAdapter.this);
                    else
                        collapseItem(item);

                } else {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textTView);
                    textView1.setTextColor(color);

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
                TextView textView1;
                TreeViewItem item;

                if (view1 != null) {
                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textTView);
                    textView1.setTextColor(color);

                }

                view.setBackgroundColor(Color.LTGRAY);

                textView1 = view.findViewById(R.id.textTView);
                textView1.setTextColor(Color.BLACK);

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

    public void add(TreeViewItem item) {
        array.add(item);
    }

    public void insert(TreeViewItem item, int i) {
        array.add(i, item);
    }

    public TreeViewItem set(TreeViewItem item, int i) {
        return array.set(i, item);
    }

    public void remove(TreeViewItem item) {
        array.remove(item);
    }

    public void remove(int i) {
        array.remove(i);
    }

    public void clear() {
        array.clear();
    }

    public TreeViewItem getItem() {
        return array.get(index);
    }

    public TreeViewItem getItem(int i) {
        return array.get(i);
    }

    public int getPosition(TreeViewItem item) {
        return array.indexOf(item);
    }

    public void expandItem(TreeViewItem item, ArrayList<String> array, int icon1) {
        String str;
        int padding, depth, pos, count, icon;

        // ilagay ang mga child item under parent item
        padding = item.getPadding() + INDENT;
        depth = item.getDepth() + 1;
        pos = getPosition(item);
        count = array.size();

        while (!array.isEmpty()) {
            str = array.remove(0);
            insert(new TreeViewItem(item, str, icon1, padding, depth), ++pos);
        }

        pos = getPosition(item) + 1;
        notifyItemRangeInserted(pos, count);

        // baguhin ang icon ng parent item
        icon = item.getIcon(item.getIcon());
        item.setIcon(icon);
        item.setCollapse(false);

        pos = getPosition(item);
        notifyItemChanged(pos);
    }

    public void collapseItem(TreeViewItem item) {
        ArrayList<TreeViewItem> array = new ArrayList<>();
        TreeViewItem item1;
        int i, pos, n, depth, icon;

        // ilagay sa array list ang mga child item under parent item
        n = getItemCount();
        pos = getPosition(item) + 1;
        depth = item.getDepth();

        for(i=pos; i<n; i++) {
            item1 = getItem(i);
            if(depth >= item1.getDepth()) break;
            array.add(item1);
        }

        // kung wala, walang gagawin
        if (array.isEmpty()) return;

        // gamit ang ArrayList array na nakuha sa itaas
        // iremove ang mga child item under parent item
        n = array.size();

        while (!array.isEmpty()) {
            item1 = array.remove(0);
            remove(item1);
        }

        notifyItemRangeRemoved(pos, n);

        // baguhin ang icon ng parent item
        icon = item.getIcon(item.getIcon());
        item.setIcon(icon);
        item.setCollapse(true);
        pos = getPosition(item);
        notifyItemChanged(pos);
    }

    public void unhighlight() {
        TextView textView;

        view1.setBackgroundColor(Color.TRANSPARENT);
        textView = view1.findViewById(R.id.textTView);
        textView.setTextColor(color);

        index = -1;
        view1 = null;
    }

    public void save() {
        restore_highlight = true;
        old_index = index;
    }

    // Maihihighlight lang uli ang item kung ito ay visible.
    // Kapag ito ay natatakpan ng soft keyboard ito ay di visible.
    // Ang manager.findViewByPosition(old_index); ay null;
    // Kaya nilagyan ko ng throw exception.
    public void restore(RecyclerView recyclerView) {
        LinearLayoutManager manager;
        TextView textView;
        View view;

        if (restore_highlight) {

            restore_highlight = false;

            manager = (LinearLayoutManager) recyclerView.getLayoutManager();
            view = manager.findViewByPosition(old_index);

            if (view == null)
                throw new RuntimeException("Can't rehighlight item.");

            view.setBackgroundColor(Color.LTGRAY);

            textView = view.findViewById(R.id.textTView);
            textView.setTextColor(Color.BLACK);

            index = old_index;
            view1 = view;
        }
    }

    // Kapag hinightlight ang cell, ang ibang cell na offscreen ay nahihilight din.
    // Makikita lang 'to kapag iniscroll. Ang function onScroll ay para ayusin to.
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                TextView textView;
                int i1, i2;

                // kung ang index ay negative walang nakaselect
                if (index < 0) return;

                textView = view1.findViewById(R.id.textTView);

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
                    textView.setTextColor(Color.BLACK);
                }
            }
        });
    }

    public interface EventListener {
        void onItemChanged(TreeViewItem item, TreeViewAdapter adapter);
        void onItemSelected(TreeViewItem item);
        void onItemUnselected();
    }
}
