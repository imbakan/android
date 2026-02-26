package balikbayan.box.recycleviewsample1;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ListViewHolder extends RecyclerView.ViewHolder {
    private TextView textView1;
    private final ConstraintLayout layout1;

    public ListViewHolder(@NonNull View itemView) {
        super(itemView);
        textView1 = itemView.findViewById(R.id.textView1);
        layout1 = itemView.findViewById(R.id.layout1);
    }

    public TextView getTextView1() {
        return textView1;
    }

    public ConstraintLayout getLayout1() {
        return layout1;
    }
}
