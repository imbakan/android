package balikbayan.box.clientbt;

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

    public TreeViewAdapter(Context context, TreeViewEventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
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
                int index = holder.getAdapterPosition();
                TreeViewItem item = array.get(index);
                listener.onItemClick(item);
            }
        });

        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onItemLongClick();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return array.size();
    }
}
