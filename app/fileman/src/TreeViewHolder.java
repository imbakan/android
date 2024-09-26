package balikbayan.box.fileman06;

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

    public TreeViewHolder(@NonNull View view) {
        super(view);

        textView = view.findViewById(R.id.textView2);
        imageView = view.findViewById(R.id.imageView1);
        layout = view.findViewById(R.id.layout1);
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
