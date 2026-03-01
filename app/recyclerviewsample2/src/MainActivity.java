
// recyclerView sample without layout resource

package balikbayan.box.recyclerviewsample2;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ListViewAdapter.EventListener {

    private Toolbar toolbar;
    private RecyclerView recyclerView1;
    private boolean access_granted, item_selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        recyclerView1 = findViewById(R.id.recyclerView1);

        ListViewAdapter adapter = new ListViewAdapter(this, this);

        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        recyclerView1.setAdapter(adapter);

        recyclerView1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            //  invoked when the global layout state or the visibility of views within the view tree changes
            @Override
            public void onGlobalLayout() {

                ListViewAdapter adapter = (ListViewAdapter) recyclerView1.getAdapter();

                if (adapter.getMode() == ListViewAdapter.EDIT) {
                    adapter.setMode(ListViewAdapter.NONE);
                    adapter.highlighItem();
                }
            }

        });

        access_granted = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        ListViewAdapter adapter = (ListViewAdapter) recyclerView1.getAdapter();
        boolean filled = adapter.getItemCount() > 0;

        menu.findItem(R.id.mnuAdd).setEnabled(access_granted);
        menu.findItem(R.id.mnuEdit).setEnabled(access_granted && item_selected);
        menu.findItem(R.id.mnuDelete).setEnabled(access_granted && item_selected);
        menu.findItem(R.id.mnuClear).setEnabled(access_granted && filled);

        menu.findItem(R.id.mnuContent).setEnabled(access_granted && filled);

        menu.findItem(R.id.mnuFill).setEnabled(access_granted);
        menu.findItem(R.id.mnuSort).setEnabled(access_granted && filled);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.mnuAdd)
            onEditAdd();
        else if (id == R.id.mnuEdit)
            onEditEdit();
        else if (id == R.id.mnuDelete)
            onEditDelete();
        else if (id == R.id.mnuClear)
            onEditClear();
        else if (id == R.id.mnuContent)
            onViewConent();
        else if (id == R.id.mnuFill)
            onToolsFill();
        else if (id == R.id.mnuSort)
            onToolsSort();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public void onItemSelected(String str) {
        item_selected = true;
    }

    @Override
    public void onItemUnselected() {
        item_selected = false;
    }

    private void onEditAdd() {
        InputDialog dlg = new InputDialog(this, new InputDialog.OnClickListener() {
            @Override
            public void onClick(String str) {
                ListViewAdapter adapter;
                int k;

                adapter = (ListViewAdapter) recyclerView1.getAdapter();
                adapter.add(str);

                k = adapter.getItemCount() - 1;
                adapter.notifyItemInserted(k);
            }
        }, R.string.text_name_1, null);

        dlg.show(getSupportFragmentManager(), "add dialog");
    }

    private void onEditEdit() {
        ListViewAdapter adapter1 = (ListViewAdapter) recyclerView1.getAdapter();
        String str1 = adapter1.getItem();

        InputDialog dlg = new InputDialog(this, new InputDialog.OnClickListener() {
            @Override
            public void onClick(String str) {
                ListViewAdapter adapter2;
                String str2;
                int k;

                adapter2 = (ListViewAdapter) recyclerView1.getAdapter();
                str2 = adapter2.getItem();
                k = adapter2.getPosition(str2);

                adapter2.setMode(ListViewAdapter.EDIT);
                adapter2.unhighlighItem();

                adapter2.set(str , k);
                adapter2.notifyItemChanged(k);
            }
        }, R.string.text_name_2, str1);

        dlg.show(getSupportFragmentManager(), "edit dialog");
    }

    private void onEditClear() {
        ListViewAdapter adapter;
        int n;

        adapter = (ListViewAdapter) recyclerView1.getAdapter();
        n = adapter.getItemCount();

        adapter.clear();
        adapter.notifyItemRangeRemoved(0, n);
    }

    private void onEditDelete() {
        ListViewAdapter adapter = (ListViewAdapter) recyclerView1.getAdapter();
        String str = adapter.getItem();

        String msg = String.format("Are you sure you want to permanently delete '%s' ?", str);

        MessageDialog dlg = new MessageDialog(this, new MessageDialog.OnClickListener() {
            @Override
            public void onClick() {
                int k = adapter.getPosition(str);
                adapter.unhighlighItem();
                adapter.remove(k);
                adapter.notifyItemRemoved(k);

                onItemUnselected();
            }
        }, msg);

        dlg.show(getSupportFragmentManager(), "message dialog");
    }

    private void onViewConent() {
        ListViewAdapter adapter = (ListViewAdapter) recyclerView1.getAdapter();
        int i, n = adapter.getItemCount();

        for (i=0; i<n; i++)
            Log.d("KLGYN", String.format("%10d %s", i, adapter.getItem(i)));
    }

    private void onToolsFill() {

        String[] names = {"Nova", "Stella", "Estelle", "Lyra", "Sirius", "Altair", "Sol", "Esther", "Selene", "Artemis", "Celestia", "Aster", "Vega", "Capella", "Rigel", "Deneb", "Luna", "Selene", "Phoebe", "Callisto", "Venus", "Mars", "Jupiter", "Mercury", "Saturn", "Uranus", "Neptune", "Pluto", "Orion", "Leo", "Cassiopeia", "Lyra", "Ursa", "Draco", "Aquila", "Andromeda", "Milky Way"};
        List<String> list = Arrays.asList(names);

        ListViewAdapter adapter = (ListViewAdapter) recyclerView1.getAdapter();

        int i = adapter.getItemCount();
        adapter.addAll(list);

        int n = list.size();
        adapter.notifyItemRangeInserted(i, n);
    }

    private void onToolsSort() {
        ListViewAdapter adapter;
        int n;

        adapter = (ListViewAdapter) recyclerView1.getAdapter();

        adapter.sort(new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return compareString(s, t1);
            }
        });

        n = adapter.getItemCount();
        adapter.notifyItemRangeChanged(0, n);

    }

    // A B = negative       B A = positive
    private int compareString(String str1, String str2) {

        int r, n1, n2;
        String s1, s2;

        n1 = str1.length();
        n2 = str2.length();

        if(n1 < n2) {
            s1 = str1;
            s2 = str2.substring(0, n1);
        } else {
            s1 = str1.substring(0, n2);
            s2 = str2;
        }

        r = s1.compareToIgnoreCase(s2);

        return r;
    }

}
