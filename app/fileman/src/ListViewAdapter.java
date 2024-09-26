package balikbayan.box.fileman06;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public void addAll(ArrayList<ListViewItem> list) {
        array.addAll(list);
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

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // alisin ang pagiging highlight ng dating item
                if(view1 != null)
                    view1.setBackgroundColor(Color.TRANSPARENT);

                listener.onItemUnselected();
            }
        });

        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                // alisin ang pagiging highlight ng dating item
                if(view1 != null)
                    view1.setBackgroundColor(Color.TRANSPARENT);

                // ihighlight ang item
                view.setBackgroundColor(Color.LTGRAY);

                // isave ang mga ito
                index = holder.getAdapterPosition();
                view1 = view;

                ListViewItem item = array.get(index);
                listener.onItemSelected(item);

                // wag na isend sa ibang event listener
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return array.size();
    }
}
