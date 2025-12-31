package balikbayan.box.fileman;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Locale;

public class ProgressDialog extends DialogFragment {

    public static final int PROGRESS = 1001;

    private AlertDialog dialog;
    private Context context;
    private OnEventListener listener;

    private ProgressBar progressBar;
    private TextView textView;
    private Handler handler;

    public ProgressDialog(Context context, OnEventListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.progress_dialog, null);

        progressBar = view.findViewById(R.id.progressBar);
        textView = view.findViewById(R.id.textView);

        AlertDialog.Builder builder = getBuilder(view);

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        handler =  new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case PROGRESS:
                        if (textView != null) {
                            int i = (int) msg.obj;
                            String str = String.format(Locale.US, "%d %%", i);

                            textView.setText(str);
                            progressBar.setProgress(i);
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                listener.onShow(ProgressDialog.this);

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onCancel();
                    }
                });
            }
        });

        return dialog;
    }

    private AlertDialog.Builder getBuilder(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        builder.setNegativeButton("cancel", null);

        return builder;
    }

    public Handler getHandler() {
        return handler;
    }

    public interface OnEventListener {
        void onCancel();
        void onShow(ProgressDialog dialog);
    }

}
