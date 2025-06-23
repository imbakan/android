package balikbayan.box.client_lan;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ListViewHolder1 extends RecyclerView.ViewHolder {

    private ImageView imageView;
    private TextView textView;
    private ConstraintLayout layout;

    public ListViewHolder1(@NonNull View itemView) {
        super(itemView);

        textView = itemView.findViewById(R.id.textView11);
        imageView = itemView.findViewById(R.id.imageView11);
        layout = itemView.findViewById(R.id.layout1);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public TextView getTextView() {
        return textView;
    }

    public ConstraintLayout getLayout() {
        return layout;
    }
}
