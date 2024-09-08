package balikbayan.box.clientbt;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class DeviceViewHolder extends RecyclerView.ViewHolder {

    private TextView textView1, textView2;
    private ConstraintLayout layout1;

    public DeviceViewHolder(@NonNull View view) {
        super(view);

        textView1 = view.findViewById(R.id.textView6);
        textView2 = view.findViewById(R.id.textView7);
        layout1 = view.findViewById(R.id.layout3);
    }

    public TextView getTextView1() {
        return textView1;
    }

    public TextView getTextView2() {
        return textView2;
    }

    public ConstraintLayout getLayout() {
        return layout1;
    }
}
