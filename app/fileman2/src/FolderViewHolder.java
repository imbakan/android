package balikbayan.box.fileman;

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

        imageView = itemView.findViewById(R.id.imageFView);
        textView = itemView.findViewById(R.id.textFView);
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
