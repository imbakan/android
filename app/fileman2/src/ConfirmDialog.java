package balikbayan.box.fileman;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ConfirmDialog extends DialogFragment {

    private AlertDialog dialog;
    private Context context;
    private OnClickListener listener;
    private String str;
    private int resid;

    public ConfirmDialog(Context context, OnClickListener listener, int resid, String str) {
        this.context = context;
        this.listener = listener;
        this.resid = resid;
        this.str = str;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = getBuilder(null);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    private AlertDialog.Builder getBuilder(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        TextView textView = new TextView(context);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setHeight(128);
        textView.setPadding(0, 32, 0, 0);
        textView.setText(resid);

        builder.setCustomTitle(textView);
        builder.setMessage(str);

        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.onClick();
            }
        });

        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return builder;
    }

    public interface OnClickListener {
        void onClick();
    }

}
