package balikbayan.box.fileman_a6;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class FolderViewHolder extends RecyclerView.ViewHolder {

    private ImageView imageView;
    private TextView textView;
    private ConstraintLayout layout;

    public FolderViewHolder(@NonNull View itemView) {
        super(itemView);

        textView = itemView.findViewById(R.id.textFView);
        imageView = itemView.findViewById(R.id.imageFView);
        layout = itemView.findViewById(R.id.layoutFV);
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
