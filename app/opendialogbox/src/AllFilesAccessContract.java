package balikbayan.box.opendialogbox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AllFilesAccessContract extends ActivityResultContract<Void, Boolean> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void unused) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.parse("package:" + context.getApplicationContext().getPackageName()));
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    @Override
    public Boolean parseResult(int i, @Nullable Intent intent) {
        return Environment.isExternalStorageManager();
    }
}
