package balikbayan.box.server_lan;

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
    private OnEventListener listener;
    private ArrayList<Client> array;
    private View view1;
    private int index;
    boolean ounce1, ounce2;

    public ListViewAdapter(Context context, OnEventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        view1 = null;
        index = -1;
        ounce1 = ounce2 = false;
    }

    public void add(Client client) {
        array.add(client);
    }

    public void remove(Client client) {
        array.remove(client);
    }

    public void remove(int i) {
        array.remove(i);
    }

    public void clear() {
        array.clear();
    }

    public Client getItem(int i) {
        return array.get(i);
    }

    public Client getItem() {
        return array.get(index);
    }

    public int getPosition(Client client) {
        return array.indexOf(client);
    }

    public int getPosition() {
        return index;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_layout_1, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        TextView textView1, textView2;
        ConstraintLayout layout;
        String str1, str2;

        textView1 = holder.getTextView1();
        textView2 = holder.getTextView2();
        layout = holder.getLayout();

        str1 = array.get(position).getName();
        str2 = array.get(position).getDate();

        textView1.setText(str1);
        textView2.setText(str2);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (view1 != null) {
                    view1.setBackgroundColor(Color.TRANSPARENT);

                    index = -1;
                    view1 = null;
                }

                listener.onItemUnselected();
            }
        });

        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (view1 != null)
                    view1.setBackgroundColor(Color.TRANSPARENT);

                view.setBackgroundColor(Color.LTGRAY);

                index = holder.getAdapterPosition();
                view1 = view;

                ounce1 = false;
                ounce2 = true;

                listener.onItemSelected();

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
        void onItemSelected();
        void onItemUnselected();
    }
}
