package balikbayan.box.light;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BluetoothContract extends ActivityResultContract<Void, Boolean> {

    private Context context;

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void unused) {

        this.context = context;

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        return intent;
    }

    @Override
    public Boolean parseResult(int i, @Nullable Intent intent) {

        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();

        return (adapter != null && adapter.isEnabled());
    }
}
