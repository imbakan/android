package balikbayan.box.client_bt;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ListViewHolder3 extends RecyclerView.ViewHolder {

    private TextView textView1, textView2;
    private ConstraintLayout layout;

    public ListViewHolder3(@NonNull View itemView) {
        super(itemView);

        textView1 = itemView.findViewById(R.id.textView31);
        textView2 = itemView.findViewById(R.id.textView32);
        layout = itemView.findViewById(R.id.layout3);
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
