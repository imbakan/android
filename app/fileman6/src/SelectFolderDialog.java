package balikbayan.box.fileman_a6;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SelectFolderDialog extends DialogFragment {

    private AlertDialog dialog;
    private Context context;
    private OnClickListener listener;

    private ImageButton imageButton;
    private Spinner spinner;
    private RecyclerView recyclerView;
    private int resId;

    private ArrayList<String> storage_paths, storage_names;

    public SelectFolderDialog(Context context, OnClickListener listener, int resId) {
        this.context = context;
        this.listener = listener;
        this.resId = resId;

        storage_paths = new ArrayList<>();
        storage_names = new ArrayList<>();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.select_folder_dialog, null);

        imageButton = view.findViewById(R.id.imageButtonFV);
        spinner = view.findViewById(R.id.spinnerFV);
        recyclerView = view.findViewById(R.id.recyclerViewFV);

        //  +------------------------------------------------------------------------+
        //  |                              button                                    |
        //  +------------------------------------------------------------------------+

        // muling ibalik sa dating folder
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popFolder(spinner);
            }
        });

        //  +------------------------------------------------------------------------+
        //  |                               spinner                                  |
        //  +------------------------------------------------------------------------+

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item) {

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                String str;

                if (position == 0)
                    str = getItem(position);
                else
                    str = "└╼ " + getItem(position);

                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView)view.findViewById(android.R.id.text1);
                textView.setText(str);

                return view;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return super.getView(position, convertView, parent);
            }
        };

        spinner.setAdapter(adapter1);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ArrayList<String> array = new ArrayList<>();
                ArrayList<String> directories = new ArrayList<>();
                String pathname;

                // ienable lang ang back arrow icon kung hindi device ang pinili ( i > 0)
                imageButton.setEnabled(i > 0);

                // iayos ang mga string na nasa spinner
                trimSpinner((Spinner)adapterView, i);

                // ipopulate ang recycler view ng mga drive (i == 0)
                // o ng mga directory ( i > 0)
                if (i == 0) {
                    fillArrayWithStorage(storage_paths, storage_names);
                    populateWithArray(recyclerView, storage_names, R.drawable.outline_sd_card_24);
                } else  {
                    getPathName((Spinner)adapterView, array);
                    pathname = getPathName( storage_paths,storage_names, array);
                    fillArrayWithDirectory(pathname, directories);
                    populateWithArray(recyclerView, directories, R.drawable.outline_folder_24);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //  +------------------------------------------------------------------------+
        //  |                           recycler view                                |
        //  +------------------------------------------------------------------------+

        FolderViewAdapter adapter2 = new FolderViewAdapter(context, new FolderViewAdapter.EventListener() {
            @Override
            public void onItemChanged(FolderViewItem item) {
                pushFolder(spinner, item.getString());
            }

            @Override
            public void onItemSelected(FolderViewItem item) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }

            @Override
            public void onItemUnselected() {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter2);

        //  +------------------------------------------------------------------------+
        //  |                               initialize                               |
        //  +------------------------------------------------------------------------+

        populateWithDevice(context, spinner);

        AlertDialog.Builder builder = getBuilder(view);

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private AlertDialog.Builder getBuilder(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        TextView textView = new TextView(context);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setHeight(128);
        textView.setPadding(32, 32, 32, 32);
        textView.setText(resId);

        builder.setCustomTitle(textView);
        builder.setView(view);

        builder.setPositiveButton("move", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ArrayAdapter<String> adapter1;
                FolderViewAdapter adapter2;
                FolderViewItem item;
                ArrayList<String> array = new ArrayList<>();
                String pathname;

                adapter1 = (ArrayAdapter<String>) spinner.getAdapter();
                adapter2 = (FolderViewAdapter) recyclerView.getAdapter();
                item = adapter2.getItem();

                if (adapter1.getCount() == 1) {
                    pathname = getPathName(storage_paths, storage_names, item.getString());
                } else {
                    getPathName(spinner, array);
                    pathname = getPathName(storage_paths, storage_names, array) + "/" + item.getString();
                }

                listener.onClick(pathname);
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return builder;
    }

    // A B = negative       B A = positive
    private int compareString(String str1, String str2) {

        int n1, n2;
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

        return s1.compareToIgnoreCase(s2);
    }

    // iadd ang String str sa spinner
    // maeexecute nito ang spinner.setOnItemSelectedListener
    private void pushFolder(Spinner spinner, String str) {
        ArrayAdapter<String> adapter;
        int i;

        adapter = (ArrayAdapter<String>) spinner.getAdapter();
        adapter.add(str);

        i = adapter.getCount() - 1;
        spinner.setSelection(i);
    }

    // iremove ang huling string na nasa spinner
    // maeexecute nito ang spinner.setOnItemSelectedListener
    private void popFolder(Spinner spinner) {
        ArrayAdapter<String> adapter;
        String str;
        int i;

        adapter = (ArrayAdapter<String>) spinner.getAdapter();
        i = adapter.getCount() - 1;
        str = adapter.getItem(i);
        adapter.remove(str);

        i = adapter.getCount() - 1;
        spinner.setSelection(i);
    }

    // alisin ang lahat na item na nasa ilalim ng piniling item
    // halimbawa : kung ang piniling item ay i=2
    //             aliin ang lahat na item mula 3 hanggang 6
    //
    //     0
    //     1
    //     2 <-- i
    //     3 ---------+
    //     4          | alisin ang apat na item na 'to
    //     5          |
    //     6 ---------+
    //
    private void trimSpinner(Spinner spinner, int i) {
        ArrayAdapter<String> adapter;
        int j, k, n;

        adapter = (ArrayAdapter<String>) spinner.getAdapter();

        // bilang ng aalisin na item
        n = adapter.getCount() - (i + 1);

        // alisin ang huling item ng n beses
        for(j=0; j<n; j++) {
            k = adapter.getCount() - 1;
            adapter.remove(adapter.getItem(k));
        }
    }

    // kunin ang pangalan ng mga directory na nasa spinner
    // hindi kasama ang device name
    // ilagay ang mga nakuha sa ArrayList array
    private void getPathName(Spinner spinner, ArrayList<String> array) {
        ArrayAdapter<String> adapter;
        int i, n;

        adapter = (ArrayAdapter<String>) spinner.getAdapter();
        n = adapter.getCount();

        for (i=1; i<n; i++)
            array.add(adapter.getItem(i));
    }

    // buuin ang pathname na nasa ArrayList array
    private String getPathName(ArrayList<String> paths, ArrayList<String> names, ArrayList<String> array) {
        StringBuilder sb;
        String str1, str2;
        int i, n;

        n = array.size();
        str1 = array.get(0);

        str2 = "";
        for (i=0; i<names.size(); i++)
            if (str1.compareTo(names.get(i)) == 0) {
                str2 = paths.get(i);
                break;
            }

        sb = new StringBuilder(str2);

        for (i=1; i<n; i++)
            sb.append("/").append(array.get(i));

        return sb.toString();
    }

    // kunin ang pathname ng storage name
    private String getPathName(ArrayList<String> paths, ArrayList<String> names, String storage) {
        String str;
        int i;

        str = "";
        for (i=0; i<names.size(); i++)
            if (storage.compareTo(names.get(i)) == 0) {
                str = paths.get(i);
                break;
            }

        return str;
    }

    // ilagay and device name sa spinner
    // maeexecute nito ang spinner.setOnItemSelectedListener
    void populateWithDevice(Context context, Spinner spinner) {
        String name = Build.MANUFACTURER.toString().toUpperCase() + " " + Build.MODEL.toString();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        adapter.add(name);
    }

    private void populateWithArray(RecyclerView recyclerView, ArrayList<String> array, int icon) {
        FolderViewAdapter adapter;
        int count;

        adapter = (FolderViewAdapter) recyclerView.getAdapter();

        count = adapter.getItemCount();
        adapter.clear();
        adapter.notifyItemRangeRemoved(0, count);

        for (String str : array)
            adapter.add(new FolderViewItem(str, icon));

        count = array.size();
        adapter.notifyItemRangeInserted(0, count);
    }

    private void fillArrayWithStorage(ArrayList<String> storage_path, ArrayList<String> storage_name) {
        File file;
        File[] files;
        String[] str = new String[2];

        storage_path.clear();
        storage_name.clear();

        // internal storage
        file = Environment.getExternalStorageDirectory();

        str[0] = file.toString();
        str[1] = "Internal Storage";

        storage_path.add(str[0]);
        storage_name.add(str[1]);

        // external storage
        //
        // files.length equal 3                                 files.length equal 2
        //
        // index zero       /storage/32BB-3E9F                  index zero      /storage/emulated
        // index one        /storage/emulated                   index one       /storage/self
        // index two        /storage/self
        //
        file = new File("/storage");
        files = file.listFiles();

        if (files != null) {

            if (files.length == 3) {

                str[0] = files[0].toString();
                str[1] = "External Storage";

                storage_path.add(str[0]);
                storage_name.add(str[1]);
            }
        }
    }

    private void fillArrayWithDirectory(String pathname, ArrayList<String> array) {
        File[] files;
        File file;

        file = new File(pathname);
        files = file.listFiles();

        // error checking
        if(files == null) return;

        for (File file1 : files)
            if (file1.isDirectory())
                array.add(file1.getName());

        // sa android 6, walang sort function ang class ArrayList
        // kaya class Collection ang gamitin
        Collections.sort(array, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return compareString(s, t1);
            }
        });
    }

    public interface OnClickListener {
        void onClick(String pathname);
    }

}
