package balikbayan.box.bt_client;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class DeviceViewHolder extends RecyclerView.ViewHolder {

    private TextView textView1, textView2;
    private ConstraintLayout layout;

    public DeviceViewHolder(@NonNull View itemView) {
        super(itemView);

        textView1 = itemView.findViewById(R.id.textViewDV1);
        textView2 = itemView.findViewById(R.id.textViewDV2);
        layout = itemView.findViewById(R.id.layoutDV);
    }

    public TextView getTextView1() {
        return textView1;
    }

    public TextView getTextView2() {
        return textView2;
    }

    public ConstraintLayout getLayout() {
        return layout;
    }
}
