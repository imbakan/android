package balikbayan.box.bt_client;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Set;

public class ConnectDialog extends DialogFragment {

    private AlertDialog dialog;
    private Context context;
    private BluetoothDevice selected_device;
    private OnClickListener listener;

    public ConnectDialog(Context context, OnClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        DeviceViewAdapter adapter = new DeviceViewAdapter(context, new DeviceViewAdapter.OnEventListener() {
            @Override
            public void onItemSelected(BluetoothDevice device) {
                selected_device = device;
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }

            @Override
            public void onItemUnselected() {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        Set<BluetoothDevice> devices = manager.getAdapter().getBondedDevices();

        if(devices.size() > 0)
            for (BluetoothDevice device : devices)
                adapter.add(device);

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        AlertDialog.Builder builder = getBuilder(recyclerView);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private AlertDialog.Builder getBuilder(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        TextView textView = new TextView(context);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setHeight(128);
        textView.setPadding(0, 32, 0, 0);
        textView.setText(R.string.text_name_1);

        builder.setCustomTitle(textView);
        builder.setView(view);

        builder.setPositiveButton("connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.onClick(selected_device);
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        return builder;
    }

    public interface OnClickListener {
        void onClick(BluetoothDevice device);
    }
}
