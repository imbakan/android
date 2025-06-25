package balikbayan.box.client_bt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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
    private BluetoothDevice bluetooth_device;
    private OnClickListener listener;

    public ConnectDialog(Context context, OnClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

       ListViewAdapter3 adapter = new ListViewAdapter3(context, new ListViewAdapter3.OnEventListener() {

           @Override
           public void onItemSelected(BluetoothDevice device) {
               bluetooth_device = device;
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        TextView textView = new TextView(context);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setHeight(128);
        textView.setPadding(32, 32, 32, 32);
        textView.setText(R.string.text_name_1);

        builder.setCustomTitle(textView);
        builder.setView(recyclerView);

        builder.setPositiveButton("connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.onClick(bluetooth_device);
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    public interface OnClickListener {
        void onClick(BluetoothDevice device);
    }
}
