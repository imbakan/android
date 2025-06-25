package balikbayan.box.client_bt;

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

public class ListViewAdapter1 extends RecyclerView.Adapter<ListViewHolder1> {

    public static final int INDENT = 32;

    private Context context;
    private OnEventListener listener;
    private ArrayList<ListViewItem1> array;
    private int index;

    public ListViewAdapter1(Context context, OnEventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        index = -1;
    }

    public void add(ListViewItem1 item) {
        array.add(item);
    }

    public void insert(ListViewItem1 item, int i) {
        array.add(i, item);
    }

    public void remove(ListViewItem1 item) {
        array.remove(item);
    }

    public void clear() {
        array.clear();
    }

    public ListViewItem1 getItem() {
        return array.get(index);
    }

    public ListViewItem1 getItem(int i) {
        return array.get(i);
    }

    public int getPosition(ListViewItem1 item) {
        return array.indexOf(item);
    }

    @NonNull
    @Override
    public ListViewHolder1 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_1, parent, false);
        return new ListViewHolder1(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder1 holder, int position) {
        ImageView imageView;
        TextView textView;
        ConstraintLayout layout;
        ListViewItem1 item;

        item = array.get(position);

        imageView = holder.getImageView();
        textView = holder.getTextView();
        layout = holder.getLayout();

        imageView.setImageResource(item.getIcon());
        imageView.setPadding(item.getIndent(), 0, 0,0);

        textView.setText(item.getString());

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                index = holder.getAdapterPosition();
                listener.onItemClick(array.get(index));
            }
        });

        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                index = holder.getAdapterPosition();
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
            }
        });
    }

    public interface OnEventListener {
        void onItemClick(ListViewItem1 item);
        void onItemSelected();
        void onItemUnselected();
    }
}
