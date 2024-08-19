package balikbayan.box.serverlan06;

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
    private ArrayList<Client> array;
    private ListViewEventListener listener;
    private View view1;
    private int index;

    public ListViewAdapter(Context context, ListViewEventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        view1 = null;
        index = 0;
    }

    public void add(Client client) {
        array.add(client);
    }

    public int remove(Client client) {
        int i = array.indexOf(client);
        array.remove(client);
        return i;
    }

    public Client getItem(int position) {
        return array.get(position);
    }

    public Client getSelectedItem() {
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
        TextView textView1, textView2;
        ConstraintLayout layout;
        String str1, str2;

        textView1 = holder.getTextView1();
        textView2 = holder.getTextView2();
        layout = holder.getLayout();

        str1 = array.get(position).getName();
        str2 = array.get(position).getLogOnTime();

        textView1.setText(str1);
        textView2.setText(str2);

        // ang click ay para ideselect ang item
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // alisin ang pagiging highlight ng dating item
                if(view1 != null)
                    view1.setBackgroundColor(Color.TRANSPARENT);

                listener.onItemUnselected();

            }
        });

        // ang long click ay para iselect ang item
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

                listener.onItemSelected();

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
