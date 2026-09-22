package balikbayan.box.fileman_a6;

import android.content.Context;
import android.graphics.Color;
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

public class FolderViewAdapter extends RecyclerView.Adapter<FolderViewHolder> {

    private Context context;
    private EventListener listener;
    private ArrayList<FolderViewItem> array;
    private View view1;
    private int index, color;

    public FolderViewAdapter(Context context, EventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        view1 = null;
        index = -1;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_item_fv, parent, false);

        TextView textView = view.findViewById(R.id.textFView);
        color = textView.getCurrentTextColor();

        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        ImageView imageView;
        TextView textView;
        ConstraintLayout layout;
        FolderViewItem item;

        imageView = holder.getImageView();
        textView = holder.getTextView();
        layout = holder.getLayout();

        item = array.get(position);

        imageView.setImageResource(item.getIcon());
        textView.setText(item.getString());

        // alisin ang highlight ng selected item kung meron
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView;
                FolderViewItem item;
                int i;

                if (view1 == null) {

                    // kung walang nakahighlight, click folder 'to
                    i = holder.getAbsoluteAdapterPosition();
                    item = array.get(i);
                    listener.onItemChanged(item);

                } else {

                    // kung may nakahighlight, alisin ang highlight
                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView = view1.findViewById(R.id.textFView);
                    textView.setTextColor(color);

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
                TextView textView;
                FolderViewItem item;

                // kung may nakahighlight, alisin ang highlight
                if (view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView = view1.findViewById(R.id.textFView);
                    textView.setTextColor(color);

                }

                // ihighlight ang selected item
                view.setBackgroundColor(Color.LTGRAY);

                textView = view.findViewById(R.id.textFView);
                textView.setTextColor(Color.BLACK);

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

    public void add(FolderViewItem item) {
        array.add(item);
    }

    public void addAll(ArrayList<FolderViewItem> items) {
        array.addAll(items);
    }

    public void clear() {
        array.clear();
    }

    public FolderViewItem getItem() {
        return array.get(index);
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

                textView = view1.findViewById(R.id.textFView);

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
        void onItemChanged(FolderViewItem item);
        void onItemSelected(FolderViewItem item);
        void onItemUnselected();
    }
}
