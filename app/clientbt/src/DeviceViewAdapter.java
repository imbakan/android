package balikbayan.box.bt_client;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeviceViewAdapter extends RecyclerView.Adapter<DeviceViewHolder> {

    private Context context;
    private OnEventListener listener;
    private ArrayList<BluetoothDevice> array;
    private View view1;
    private int index, color;

    public DeviceViewAdapter(Context context, OnEventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        view1 = null;
        index = -1;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.device_item, parent, false);

        TextView textView = view.findViewById(R.id.textViewDV1);
        color = textView.getCurrentTextColor();

        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        TextView textView1, textView2;
        ConstraintLayout layout;
        BluetoothDevice item;

        item = array.get(position);

        textView1 = holder.getTextView1();
        textView2 = holder.getTextView2();
        layout = holder.getLayout();

        textView1.setText(item.getName());
        textView2.setText(item.getAddress());

        // alisin ang highlight ng selected item kung meron
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView1, textView2;

                if (view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textViewDV1);
                    textView2 = view1.findViewById(R.id.textViewDV2);

                    textView1.setTextColor(color);
                    textView2.setTextColor(color);
                }

                view1 = null;
                index = -1;

                listener.onItemUnselected();
            }
        });

        // ihighlight ng selected item
        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                TextView textView1, textView2;
                BluetoothDevice item;

                if (view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textViewDV1);
                    textView2 = view1.findViewById(R.id.textViewDV2);

                    textView1.setTextColor(color);
                    textView2.setTextColor(color);
                }

                view.setBackgroundColor(Color.LTGRAY);

                textView1 = view.findViewById(R.id.textViewDV1);
                textView2 = view.findViewById(R.id.textViewDV2);

                textView1.setTextColor(Color.DKGRAY);
                textView2.setTextColor(Color.DKGRAY);

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

    public void add(BluetoothDevice device) {
        array.add(device);
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
                TextView textView1, textView2;
                int i1, i2;

                // kung ang index ay negative walang nakaselect
                if (index < 0) return;

                textView1 = view1.findViewById(R.id.textViewDV1);
                textView2 = view1.findViewById(R.id.textViewDV2);

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
                    textView1.setTextColor(color);
                    textView2.setTextColor(color);

                } else {

                    // kung visible, ihighlight ng selected item
                    view1 = manager.findViewByPosition(index);
                    view1.setBackgroundColor(Color.LTGRAY);
                    textView1.setTextColor(Color.DKGRAY);
                    textView2.setTextColor(Color.DKGRAY);
                }
            }
        });
    }

    public interface OnEventListener {
        void onItemSelected(BluetoothDevice device);
        void onItemUnselected();
    }
}
