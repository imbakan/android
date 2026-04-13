package balikbayan.box.aswitch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class LightColorDialog extends DialogFragment {
    private AlertDialog dialog;
    private Context context;
    private OnChangeListener listener;
    private RadioGroup radioGroup;

    public LightColorDialog(Context context, OnChangeListener listener, int progress) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = getLayoutInflater().inflate(R.layout.light_layout_1, null);

        radioGroup = view.findViewById(R.id.radioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup radioGroup, int i) {
                RadioButton button = view.findViewById(i);
                listener.onChange((int)button.getTag());
            }
        });

        ArrayList<LightAttribute> array = new ArrayList<>();
        RadioButton button;
        int i, n;

        array.add(new LightAttribute("Black Light Fluorescent",  Color.rgb(167, 0, 255)));
        array.add(new LightAttribute("Blue",                     Color.rgb(0, 0, 255)));
        array.add(new LightAttribute("Brown",                    Color.rgb(150, 75, 0)));
        array.add(new LightAttribute("Bulb (100 W)",             Color.rgb(255, 214, 170)));
        array.add(new LightAttribute("Bulb (40 W)",              Color.rgb(255, 197, 143)));
        array.add(new LightAttribute("Bulb",                     Color.rgb(255, 194, 0)));
        array.add(new LightAttribute("Candle",                   Color.rgb(255, 147, 41)));
        array.add(new LightAttribute("Cyan",                     Color.rgb(0, 255, 255)));
        array.add(new LightAttribute("Green",                    Color.rgb(0, 255, 0)));
        array.add(new LightAttribute("Grey",                     Color.rgb(128, 128, 128)));

        array.add(new LightAttribute("Indigo",                       Color.rgb(75, 0, 130)));
        array.add(new LightAttribute("Magenta",                      Color.rgb(255, 0, 255)));
        array.add(new LightAttribute("Orange",                       Color.rgb(255, 165, 0)));
        array.add(new LightAttribute("Red",                          Color.rgb(255, 0, 0)));
        array.add(new LightAttribute("Sodium Lamp",                  Color.rgb(255, 209, 178)));
        array.add(new LightAttribute("Sodium Lamp (High Pressure)",  Color.rgb(255, 183, 76)));
        array.add(new LightAttribute("Violet",                       Color.rgb(127, 0, 255)));
        array.add(new LightAttribute("White",                        Color.rgb(255, 255, 255)));
        array.add(new LightAttribute("Yellow",                       Color.rgb(255, 255, 0)));
        array.add(new LightAttribute("Neon",                         Color.rgb(255, 61, 0)));

        n = radioGroup.getChildCount();

        for (i=0; i<n; i++) {
            button = (RadioButton) radioGroup.getChildAt(i);
            button.setText(array.get(i).getString());
            button.setTag(array.get(i).getValue());
        }

        AlertDialog.Builder builder = getBuilder(view);
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
        textView.setText(R.string.text_name_2);

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
