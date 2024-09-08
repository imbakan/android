package balikbayan.box.clientbt;

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

public class DeviceAdapter extends RecyclerView.Adapter<DeviceViewHolder> {

    private Context context;
    private DeviceEventListener listener;
    private ArrayList<BluetoothDevice> array;
    private View view1;
    private int index;

    public DeviceAdapter(Context context, DeviceEventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        view1 = null;
        index = 0;
    }

    public void add(BluetoothDevice device) {
        array.add(device);
    }

    public BluetoothDevice getItem(int position) {
        return array.get(position);
    }

    public BluetoothDevice getSelectedItem() {
        return array.get(index);
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.device_layout, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        TextView textView1, textView2;
        ConstraintLayout layout1;
        String str1, str2;

        textView1 = holder.getTextView1();
        textView2 = holder.getTextView2();
        layout1 = holder.getLayout();

        str1 = array.get(position).getName();
        str2 = array.get(position).getAddress();

        textView1.setText(str1);
        textView2.setText(str2);

        layout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view1 != null)
                    view1.setBackgroundColor(Color.TRANSPARENT);

                listener.onItemUnselected();
            }
        });

        layout1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if(view1 != null)
                    view1.setBackgroundColor(Color.TRANSPARENT);

                view.setBackgroundColor(Color.LTGRAY);
                view1 = view;
                index = holder.getAdapterPosition();

                listener.onItemSelected(index);

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return array.size();
    }
}

