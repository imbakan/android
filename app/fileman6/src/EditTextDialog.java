package balikbayan.box.fileman_a6;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

public class EditTextDialog extends DialogFragment {

    private AlertDialog dialog;
    private EditText editText;
    private Context context;
    private OnClickListener listener;
    private String name;
    private int resid;

    public EditTextDialog(Context context, OnClickListener listener, int resid, String name) {
        this.context = context;
        this.listener = listener;
        this.resid = resid;
        this.name = name;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // ang box_outline_2 ay para maglagay ng space sa pagitan ng left/right border ng dialog at left/right ng EditText
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.box_outline_2, null);

        editText = new EditText(context);
        editText.setBackground(drawable);
        editText.setPadding(16, 8, 16, 8); // sa left/right tingnan ang file box_outline_2.xml
        editText.setSingleLine();
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
        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        textView.setHeight(64);
        textView.setPadding(0, 16, 0, 0);
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
