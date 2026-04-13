package balikbayan.box.aswitch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class BrightnessDialog extends DialogFragment {
    private AlertDialog dialog;
    private Context context;
    private OnChangeListener listener;
    private int progress;

    public BrightnessDialog(Context context, OnChangeListener listener, int progress) {
        this.context = context;
        this.listener = listener;
        this.progress = progress;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        SeekBar seekBar = new SeekBar(context);

        //seekBar.setMin(1);
        seekBar.setMax(255);
        seekBar.setProgress(progress);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                listener.onChange(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        AlertDialog.Builder builder = getBuilder(seekBar);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window window = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(window.getAttributes()); // Copy existing attributes
        layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        layoutParams.x = 0;
        layoutParams.y = 0;
        window.setAttributes(layoutParams);
    }

    private AlertDialog.Builder getBuilder(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        TextView textView = new TextView(context);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setHeight(128);
        textView.setPadding(32, 32, 32, 32);
        textView.setText(R.string.text_name_1);

        builder.setCustomTitle(textView);
        builder.setView(view);

        builder.setPositiveButton("close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return builder;
    }

    public interface OnChangeListener {
        void onChange(int i);
    }

}
