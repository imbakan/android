package balikbayan.box.recyclerviewsample2;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ListViewHolder extends RecyclerView.ViewHolder {
    private TextView textView1;

    public ListViewHolder(@NonNull View itemView) {
        super(itemView);
        textView1 = itemView.findViewById(android.R.id.text1);
    }

    public TextView getTextView() {
        return textView1;
    }

}
