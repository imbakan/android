package balikbayan.box.fileman06;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TreeViewAdapter extends RecyclerView.Adapter<TreeViewHolder> {

    private Context context;
    private TreeViewEventListener listener;
    private ArrayList<TreeViewItem> array;
    private int index;

    public TreeViewAdapter(Context context, TreeViewEventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        index = 0;
    }

    public void add(TreeViewItem treeItem) {
        array.add(treeItem);
    }

    public void insert(TreeViewItem treeItem, int index) {
        array.add(index, treeItem);
    }

    public int getPosition(TreeViewItem treeItem) {
        return array.indexOf(treeItem);
    }

    public TreeViewItem getItem(int position) {
        return array.get(position);
    }

    public void remove(TreeViewItem treeItem) {
        array.remove(treeItem);
    }

    public void clear() {
        array.clear();
    }

    public TreeViewItem getSelectedItem() {
        return array.get(index);
    }

    @NonNull
    @Override
    public TreeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tree_layout, parent, false);
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

        imageView.setImageResource (item.getIcon());
        imageView.setPadding(item.getPadding(), 0, 0,0);

        textView.setText(item.getString());

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                index = holder.getAdapterPosition();
                TreeViewItem item = array.get(index);

                if (item.isCollapse())
                    listener.onItemExpanding(item, TreeViewAdapter.this);
                else
                    listener.onItemExpanded(item, TreeViewAdapter.this);
            }
        });

        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                index = holder.getAdapterPosition();
                TreeViewItem item = array.get(index);
                listener.onSelChanged(item);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return array.size();
    }
}
