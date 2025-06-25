package balikbayan.box.server_bt;

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
    private int index, color;

    public ListViewAdapter(Context context, OnEventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        view1 = null;
        index = -1;
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
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_1, parent, false);

        TextView textView = view.findViewById(R.id.textView1);
        color = textView.getCurrentTextColor();

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
                TextView textView1, textView2;

                if(view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textView1);
                    textView2 = view1.findViewById(R.id.textView2);

                    textView1.setTextColor(color);
                    textView2.setTextColor(color);
                }

                view1 = null;
                index = -1;

                listener.onItemUnselected();
            }
        });

        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                TextView textView1, textView2;

                if(view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textView1);
                    textView2 = view1.findViewById(R.id.textView2);

                    textView1.setTextColor(color);
                    textView2.setTextColor(color);
                }

                view.setBackgroundColor(Color.LTGRAY);

                textView1 = view.findViewById(R.id.textView1);
                textView2 = view.findViewById(R.id.textView2);

                textView1.setTextColor(Color.DKGRAY);
                textView2.setTextColor(Color.DKGRAY);

                view1 = view;
                index = holder.getAdapterPosition();

                listener.onItemSelected();

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public interface OnEventListener {
        void onItemSelected();
        void onItemUnselected();
    }
}
