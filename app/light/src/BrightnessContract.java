package balikbayan.box.light;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BrightnessContract extends ActivityResultContract<Void, Boolean> {

    private Context context;

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void unused) {

        this.context = context;

        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        return intent;
    }

    @Override
    public Boolean parseResult(int i, @Nullable Intent intent) {
        return Settings.System.canWrite(context);
    }
}
