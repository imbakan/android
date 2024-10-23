package balikbayan.box.opendialogbox;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewHolder> {
    private Context context;
    private ListViewEventListener listener;
    private ArrayList<ListViewItem> array;
    private View view1;
    private int index;

    public ListViewAdapter(Context context, ListViewEventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        view1 = null;
        index = 0;
    }

    public void add(ListViewItem listItem) {
        array.add(listItem);
    }

    public void clear() {
        array.clear();
    }

    public ListViewItem getSelectedItem() {
        return array.get(index);
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_layout, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        TextView textView1, textView2, textView3;
        ImageView imageView;
        ConstraintLayout layout;
        ListViewItem item;

        imageView = holder.getImageView();
        textView1 = holder.getTextView1();
        textView2 = holder.getTextView2();
        textView3 = holder.getTextView3();
        layout = holder.getLayout();

        item = array.get(position);

        imageView.setImageResource(item.getIcon());
        textView1.setText(item.getString1());
        textView2.setText(item.getString2());
        textView3.setText(item.getString3());

        // ang click ay para baguhin ang directory
        // pero kung nakaselect ang file ay madedeselect ito
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(view1 != null) {
                    view1.setBackgroundColor(Color.TRANSPARENT);
                    view1 = null;
                    listener.onItemUnselected();
                    return;
                }

                index = holder.getAdapterPosition();
                ListViewItem item = array.get(index);

                if (item.getIcon() == R.raw.file1) return;

                listener.onItemChanged(item);
            }
        });

        // ang long click ay para iselect ang file
        // kung hindi file ang sinelect, hindi 'to mahihighlight
        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (item.getIcon() != R.raw.file1) return true;

                if(view1 != null)
                    view1.setBackgroundColor(Color.TRANSPARENT);

                view.setBackgroundColor(Color.LTGRAY);
                view1 = view;

                index = holder.getAdapterPosition();
                ListViewItem item = array.get(index);
                listener.onItemSelected(item);

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return array.size();
    }
}
