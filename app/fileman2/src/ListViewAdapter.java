package balikbayan.box.fileman;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewHolder> {

    private Context context;
    private OnEventListener listener;
    private ArrayList<ListViewItem> array;
    private View view1;
    private int index, color;
    private boolean ounce1, ounce2;

    public ListViewAdapter(Context context, OnEventListener listener) {
        this.context = context;
        this.listener = listener;
        array = new ArrayList<>();
        view1 = null;
        index = -1;
        ounce1 = ounce2 = false;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

        TextView textView = view.findViewById(R.id.textViewLV1);
        color = textView.getCurrentTextColor();

        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        TextView textView1, textView2, textView3;
        ConstraintLayout layout;
        ListViewItem item;

        textView1 = holder.getTextView1();
        textView2 = holder.getTextView2();
        textView3 = holder.getTextView3();
        layout = holder.getLayout();

        item = array.get(position);

        textView1.setText(item.getString1());
        textView2.setText(item.getString2());
        textView3.setText(item.getString3());

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView textView1, textView2, textView3;

                // kung may nakahighlight, alisin ang highlight
                if (view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textViewLV1);
                    textView2 = view1.findViewById(R.id.textViewLV2);
                    textView3 = view1.findViewById(R.id.textViewLV3);

                    textView1.setTextColor(color);
                    textView2.setTextColor(color);
                    textView3.setTextColor(color);

                    view1 = null;
                    index = -1;

                    listener.onItemUnselected();
                }
            }
        });

        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                TextView textView1, textView2, textView3;
                ListViewItem item;

                // kung may nakahighlight, alisin ang highlight
                if (view1 != null) {

                    view1.setBackgroundColor(Color.TRANSPARENT);

                    textView1 = view1.findViewById(R.id.textViewLV1);
                    textView2 = view1.findViewById(R.id.textViewLV2);
                    textView3 = view1.findViewById(R.id.textViewLV3);

                    textView1.setTextColor(color);
                    textView2.setTextColor(color);
                    textView3.setTextColor(color);
                }

                // ihighlight ang selected item
                view.setBackgroundColor(Color.LTGRAY);

                textView1 = view.findViewById(R.id.textViewLV1);
                textView2 = view.findViewById(R.id.textViewLV2);
                textView3 = view.findViewById(R.id.textViewLV3);

                textView1.setTextColor(Color.DKGRAY);
                textView2.setTextColor(Color.DKGRAY);
                textView3.setTextColor(Color.DKGRAY);

                view1 = view;
                index = holder.getAbsoluteAdapterPosition();

                ounce1 = true;
                ounce2 = false;

                item = array.get(index);
                listener.onItemSelected(item);

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                TextView textView1, textView2, textView3;
                int i1, i2;

                // kung ang index ay negative walang nakaselect
                if (index < 0) return;

                textView1 = view1.findViewById(R.id.textViewLV1);
                textView2 = view1.findViewById(R.id.textViewLV2);
                textView3 = view1.findViewById(R.id.textViewLV3);

                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();

                // ito ay para sa partially visible
                i1 = manager.findFirstVisibleItemPosition();
                i2 = manager.findLastVisibleItemPosition();

                // ito ay para sa completely visible
                //i1 = manager.findFirstCompletelyVisibleItemPosition();
                //i2 = manager.findLastCompletelyVisibleItemPosition();

                // alamin kung visible o hindi ang selected item
                if (index < i1 || i2 < index) {

                    // alisin ang highlight kapag ang selected item ay di visible
                    // ang variable ounce1 ay para isang beses lang ito iexecute
                    if (ounce1) {

                        ounce1 = false;
                        ounce2 = true;

                        view1.setBackgroundColor(Color.TRANSPARENT);

                        textView1.setTextColor(color);
                        textView2.setTextColor(color);
                        textView3.setTextColor(color);
                    }
                } else {

                    // ihighlight kapag ang selected item ay visible
                    // ang variable ounce2 ay para isang beses lang ito iexecute
                    if (ounce2) {

                        ounce1 = true;
                        ounce2 = false;

                        view1 = manager.findViewByPosition(index);

                        view1.setBackgroundColor(Color.LTGRAY);

                        textView1.setTextColor(Color.DKGRAY);
                        textView2.setTextColor(Color.DKGRAY);
                        textView3.setTextColor(Color.DKGRAY);
                    }
                }
            }
        });
    }

    public void addAll(ArrayList<ListViewItem> items) {
        array.addAll(items);
    }

    public void remove(ListViewItem item) {
        array.remove(item);
    }

    public void clear() {
        array.clear();
    }

    public ListViewItem getItem() {
        return array.get(index);
    }

    public int getPosition(ListViewItem item) {
        return array.indexOf(item);
    }

    public interface OnEventListener {
        void onItemUnselected();
        void onItemSelected(ListViewItem item);
    }
}
