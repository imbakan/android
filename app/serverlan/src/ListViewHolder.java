package balikbayan.box.serverlan06;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ListViewHolder extends RecyclerView.ViewHolder {

    private TextView textView1, textView2;
    private ConstraintLayout layout1;

    public ListViewHolder(@NonNull View itemView) {
        super(itemView);
        textView1 = itemView.findViewById(R.id.textView1);
        textView2 = itemView.findViewById(R.id.textView2);
        layout1 = itemView.findViewById(R.id.layout1);
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
