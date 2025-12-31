package balikbayan.box.fileman;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class TreeViewHolder extends RecyclerView.ViewHolder {

    private ImageView imageView;
    private TextView textView;
    private ConstraintLayout layout;

    public TreeViewHolder(@NonNull View itemView) {
        super(itemView);

        textView = itemView.findViewById(R.id.textViewTV);
        imageView = itemView.findViewById(R.id.imageViewTV);
        layout = itemView.findViewById(R.id.layoutTV);
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
