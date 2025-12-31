package balikbayan.box.fileman;

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
    private OnEventListener listener;
    private ArrayList<TreeViewItem> array;
    private View view1;
    private int index, color;
    private boolean ounce1, ounce2;

    public TreeViewAdapter(Context context, OnEventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        view1 = null;
        index = -1;
        ounce1 = ounce2 = false;
    }

    @NonNull
    @Override
    public TreeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tree_item, parent, false);

        TextView textView = view.findViewById(R.id.textViewTV);
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

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Log.d("KLGYN", "Tree View Adapter onClick");

                TextView textView1;
                TreeViewItem item;
                int i;

                if (view1 == null) {

                    // kung walang nakahighlight, click folder 'to
                    i = holder.getAbsoluteAdapterPosition();
                    item = array.get(i);

                    if (item.isCollapse())
                        listener.onItemExpanding(item, TreeViewAdapter.this);
                    else
                        listener.onItemCollapsing(item, TreeViewAdapter.this);

                } else {

                    // kung may nakahighlight, alisin ang highlight
                    view1.setBackgroundColor(Color.TRANSPARENT);
                    textView1 = view1.findViewById(R.id.textViewTV);
                    textView1.setTextColor(color);

                    listener.onItemUnselected();
                }

                view1 = null;
                index = -1;
            }
        });

        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                //Log.d("KLGYN", "Tree View Adapter onLongClick");

                TextView textView1;
                TreeViewItem item;
                int i;

                // folder lang ang tatanggapin nito
                i = holder.getAbsoluteAdapterPosition();
                item = array.get(i);

                if (item.getIcon() != R.raw.folder1 && item.getIcon() != R.raw.folder2) return true;

                // kung may nakahighlight, alisin ang highlight
                if (view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);
                    textView1 = view1.findViewById(R.id.textViewTV);
                    textView1.setTextColor(color);
                }

                // ihighlight ang selected item
                view.setBackgroundColor(Color.LTGRAY);
                textView1 = view.findViewById(R.id.textViewTV);
                textView1.setTextColor(Color.DKGRAY);

                view1 = view;

                ounce1 = true;
                ounce2 = false;

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

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                TextView textView1;
                int i1, i2;

                // kung ang index ay negative walang nakaselect
                if (index < 0) return;

                textView1 = view1.findViewById(R.id.textViewTV);

                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();

                // ito ay para sa partially visible
                i1 = manager.findFirstVisibleItemPosition();
                i2 = manager.findLastVisibleItemPosition();

                // ito ay para sa completely visible
                //i1 = manager.findFirstCompletelyVisibleItemPosition();
                //i2 = manager.findLastCompletelyVisibleItemPosition();

                // alamin kung visible o hindi ang selected item
                if (index < i1 || i2 < index) {

                    // alisin ang highlight kapag ang selected item ay di visible
                    // ang variable ounce1 ay para isang beses lang ito iexecute
                    if (ounce1) {

                        ounce1 = false;
                        ounce2 = true;

                        view1.setBackgroundColor(Color.TRANSPARENT);
                        textView1.setTextColor(color);
                    }
                } else {

                    // ihighlight kapag ang selected item ay visible
                    // ang variable ounce2 ay para isang beses lang ito iexecute
                    if (ounce2) {

                        ounce1 = true;
                        ounce2 = false;

                        view1 = manager.findViewByPosition(index);
                        view1.setBackgroundColor(Color.LTGRAY);
                        textView1.setTextColor(Color.DKGRAY);
                    }
                }
            }
        });
    }

    public void add(TreeViewItem item) {
        array.add(item);
    }

    public void insert(TreeViewItem item, int i) {
        array.add(i, item);
    }

    public void remove(TreeViewItem item) {
        array.remove(item);
    }

    public TreeViewItem getItem(int i) {
        return array.get(i);
    }

    public TreeViewItem getItem() {
        return array.get(index);
    }

    public int getPosition(TreeViewItem item) {
        return array.indexOf(item);
    }

    public interface OnEventListener {
        void onItemUnselected();
        void onItemSelected(TreeViewItem item);
        void onItemExpanding(TreeViewItem item, TreeViewAdapter adapter);
        void onItemCollapsing(TreeViewItem item, TreeViewAdapter adapter);
    }
}
