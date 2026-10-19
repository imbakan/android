package balikbayan.box.fileman_a6;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class MessageDialog extends DialogFragment {

    private AlertDialog dialog;
    private Context context;
    private OnClickListener listener;
    private String msg;
    private int resid;

    public MessageDialog(Context context, OnClickListener listener, int resid, String msg) {
        this.context = context;
        this.listener = listener;
        this.resid = resid;
        this.msg = msg;
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
        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        textView.setHeight(64);
        textView.setPadding(0, 16, 0, 0);
        textView.setText(resid);

        builder.setCustomTitle(textView);
        builder.setMessage(msg);

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
