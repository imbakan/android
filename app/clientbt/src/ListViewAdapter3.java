package balikbayan.box.client_bt;

import android.bluetooth.BluetoothDevice;
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

public class ListViewAdapter3 extends RecyclerView.Adapter<ListViewHolder3> {

    private Context context;
    private OnEventListener listener;
    private ArrayList<BluetoothDevice> array;
    private View view1;
    private int index, color;

    public ListViewAdapter3(Context context, OnEventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        view1 = null;
        index = -1;
    }

    public void add(BluetoothDevice device) {
        array.add(device);
    }

    public BluetoothDevice getItem() {
        return array.get(index);
    }

    @NonNull
    @Override
    public ListViewHolder3 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_3, parent, false);

        TextView textView = view.findViewById(R.id.textView31);
        color = textView.getCurrentTextColor();

        return new ListViewHolder3(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder3 holder, int position) {
        TextView textView1, textView2;
        ConstraintLayout layout;
        BluetoothDevice item;

        item = array.get(position);

        textView1 = holder.getTextView1();
        textView2 = holder.getTextView2();
        layout = holder.getLayout();

        textView1.setText(item.getName());
        textView2.setText(item.getAddress());

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView1, textView2;

                if(view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textView31);
                    textView2 = view1.findViewById(R.id.textView32);

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

                    textView1 = view1.findViewById(R.id.textView31);
                    textView2 = view1.findViewById(R.id.textView32);

                    textView1.setTextColor(color);
                    textView2.setTextColor(color);
                }

                view.setBackgroundColor(Color.LTGRAY);

                textView1 = view.findViewById(R.id.textView31);
                textView2 = view.findViewById(R.id.textView32);

                textView1.setTextColor(Color.DKGRAY);
                textView2.setTextColor(Color.DKGRAY);

                view1 = view;
                index = holder.getAdapterPosition();

                listener.onItemSelected(array.get(index));

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public interface OnEventListener {
        void onItemSelected(BluetoothDevice device);
        void onItemUnselected();
    }
}
