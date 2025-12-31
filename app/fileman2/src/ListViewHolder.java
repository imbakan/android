package balikbayan.box.fileman;

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

        textView1 = itemView.findViewById(R.id.textViewLV1);
        textView2 = itemView.findViewById(R.id.textViewLV2);
        textView3 = itemView.findViewById(R.id.textViewLV3);
        layout = itemView.findViewById(R.id.layoutLV);

        ImageView imageView = itemView.findViewById(R.id.imageViewLV);
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
