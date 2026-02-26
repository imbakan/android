package balikbayan.box.recycleviewsample1;

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

public class MessageDialog  extends DialogFragment {

    private Context context;
    private OnClickListener listener;
    private String message;

    public MessageDialog(Context context, OnClickListener listener, String message) {
        this.context = context;
        this.listener = listener;
        this.message = message;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = getBuilder(null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    private AlertDialog.Builder getBuilder(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        TextView textView = new TextView(context);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setHeight(128);
        textView.setPadding(0, 32, 0, 0);
        textView.setText(R.string.text_name_3);

        builder.setCustomTitle(textView);
        builder.setMessage(message);

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
