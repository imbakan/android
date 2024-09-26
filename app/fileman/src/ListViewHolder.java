package balikbayan.box.fileman06;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ListViewHolder extends RecyclerView.ViewHolder {

    private TextView textView1, textView2, textView3;
    private ConstraintLayout layout;

    public ListViewHolder(@NonNull View itemView) {
        super(itemView);

        textView1 = itemView.findViewById(R.id.textView3);
        textView2 = itemView.findViewById(R.id.textView4);
        textView3 = itemView.findViewById(R.id.textView5);
        layout = itemView.findViewById(R.id.layout2);

        ImageView imageView = itemView.findViewById(R.id.imageView2);
        imageView.setImageResource(R.raw.file1);
    }

    public TextView getTextView1() {
        return textView1;
    }

    public TextView getTextView2() {
        return textView2;
    }

    public TextView getTextView3() {
        return textView3;
    }

    public ConstraintLayout getLayout() {
        return layout;
    }
}
