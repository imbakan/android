
package balikbayan.box.clientbt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Set;

public class ConnectDialog extends DialogFragment implements DeviceEventListener{

    public static final int CONNECT = 9301;

    private AlertDialog dialog;
    private Context context;
    private Handler handler;

    public ConnectDialog(Handler handler, Context context) {
        this.handler = handler;
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        DeviceAdapter adapter = new DeviceAdapter(context, (DeviceEventListener) this);

        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        Set<BluetoothDevice> devices = manager.getAdapter().getBondedDevices();

        if(devices.size() > 0)
            for (BluetoothDevice device : devices)
                adapter.add(device);

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(recyclerView);

        // positive
        builder.setPositiveButton("connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Message msg;
                DeviceAdapter adapter;
                BluetoothDevice device;

                adapter = (DeviceAdapter) recyclerView.getAdapter();
                device = adapter.getSelectedItem();

                msg = handler.obtainMessage(CONNECT, device);
                msg.sendToTarget();
            }
        });

        // negative
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        // ilagay ito pagkatapos ng builder set positive/negative button
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    @Override
    public void onItemSelected(int position) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
    }

    @Override
    public void onItemUnselected() {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }
}
