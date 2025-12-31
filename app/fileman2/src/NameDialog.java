
package balikbayan.box.fileman;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

public class NameDialog extends DialogFragment {

    private AlertDialog dialog;
    private EditText editText;
    private Context context;
    private OnClickListener listener;
    private String name;
    private int resid;

    public NameDialog(Context context, OnClickListener listener, int resid, String name) {
        this.context = context;
        this.listener = listener;
        this.resid = resid;
        this.name = name;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.box_outline_2, null);
        drawable.setTint(Color.BLACK);

        editText = new EditText(context);
        editText.setBackground(drawable);
        editText.setPadding(32, 32, 32, 32);
        editText.setText(name);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(charSequence.length() > 0);
            }
        });

        AlertDialog.Builder builder = getBuilder(editText);
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
        builder.setView(view);

        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String str = editText.getText().toString();
                listener.onClick(str);
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return builder;
    }

    public interface OnClickListener {
        void onClick(String str);
    }

}
