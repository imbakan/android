package balikbayan.box.light;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyDeviceAdminContract extends ActivityResultContract<Void, Boolean> {

    private Context context;

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void unused) {

        this.context = context;

        ComponentName componentName = new ComponentName(context, MyDeviceAdminReceiver.class);

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Testing lang to para sa lock.");

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        return intent;
    }

    @Override
    public Boolean parseResult(int i, @Nullable Intent intent) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(context, MyDeviceAdminReceiver.class);
        return devicePolicyManager.isAdminActive(componentName);
    }
}
