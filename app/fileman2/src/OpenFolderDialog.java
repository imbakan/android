package balikbayan.box.fileman;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.util.Comparator;
import java.util.Set;

public class OpenFolderDialog extends DialogFragment {

    private static String current_pathname;

    private AlertDialog dialog;
    private Context context;
    private OnClickListener listener;
    private int resid;

    private ImageButton imageButton;
    private Spinner spinner;
    private RecyclerView recyclerView;

    private ArrayList<String> storage_paths, storage_names;

    public OpenFolderDialog(Context context, OnClickListener listener, int resid) {
        this.context = context;
        this.listener = listener;
        this.resid = resid;

        storage_paths = new ArrayList<>();
        storage_names = new ArrayList<>();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.open_folder_dialog, null);

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

            // lagyan ng elbow character sa unahan ng folder, hindi kasama ang device para sa drop down view
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                String str;

                if (position == 0)
                    str = getItem(position);
                else
                    str = "└╼ " + getItem(position);

                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView)view.findViewById(android.R.id.text1);
                textView.setTextColor(getResources().getColor(R.color.gray_C0, getResources().newTheme()));
                textView.setText(str);

                return view;
            }

            // normal view
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView)view.findViewById(android.R.id.text1);
                textView.setTextColor(getResources().getColor(R.color.gray_C0, getResources().newTheme()));
                return view;
            }
        };

        spinner.setAdapter(adapter1);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ArrayList<String> array = new ArrayList<>();
                String pathname;

                // ienable lang ang back arrow icon kung hindi device ang pinili ( i > 0)
                imageButton.setEnabled(i > 0);

                // iayos ang mga string na nasa spinner
                trimSpinner((Spinner)adapterView, i);

                // ipopulate ang recycler view ng mga drive (i == 0)
                // o ng mga directory at mga file ( i > 0)
                if (i == 0) {
                    populateWithStorage(recyclerView, storage_names);
                } else {
                    getPathName((Spinner)adapterView, array);
                    pathname = getPathName(storage_names, storage_paths, array);
                    populateWithDirectory(recyclerView, pathname);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //  +------------------------------------------------------------------------+
        //  |                           recycler view                                |
        //  +------------------------------------------------------------------------+

        FolderViewAdapter adapter2 = new FolderViewAdapter(context, new FolderViewAdapter.OnEventListener() {
            @Override
            public void onItemUnselected() {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }

            @Override
            public void onItemSelected(FolderViewItem item) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }

            @Override
            public void onItemChanged(FolderViewItem item) {
                pushFolder(spinner, item.getString());
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter2);

        //  +------------------------------------------------------------------------+
        //  |                               initialize                               |
        //  +------------------------------------------------------------------------+

        getDeviceStorages(storage_paths, storage_names);

        if (current_pathname == null)
            populateWithDevice(context, spinner);
        else
            restoreInstanceState(current_pathname, spinner);

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
        textView.setPadding(0, 32, 0, 0);
        textView.setText(resid);

        builder.setCustomTitle(textView);
        builder.setView(view);

        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FolderViewAdapter adapter;
                FolderViewItem item;
                ArrayList<String> array = new ArrayList<>();
                String[] name = new String[2];

                getPathName(spinner, array);
                current_pathname = saveInstanceState(array);
                name[0] = getPathName(storage_names, storage_paths, array);

                adapter = (FolderViewAdapter) recyclerView.getAdapter();
                item = adapter.getItem();
                name[1] = item.getString();

                listener.onClick(name[0] + "/" + name[1]);
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
    private String getPathName(ArrayList<String> names, ArrayList<String> paths, ArrayList<String> array) {
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

    // kunin ang mga storage
    // ilagay ang mga nakuha sa ArrayList paths at names
    private void getDeviceStorages(ArrayList<String> paths, ArrayList<String> names) {
        File file;
        String[] str = new String[2];
        String str3;
        int i;

        paths.clear();
        names.clear();

        // internal storage
        file = Environment.getExternalStorageDirectory();

        str[0] = file.toString();
        str[1] = "Internal Storage";

        paths.add(str[0]);
        names.add(str[1]);

        // external storage
        Set<String> volumes = MediaStore.getExternalVolumeNames(context);
        int n = volumes.size();

        if (n > 1) {

            String[] array = new String[n];
            volumes.toArray(array);

            str[0] = "/storage/" + array[1].toUpperCase();
            str[1] = "External Storage";

            paths.add(str[0]);
            names.add(str[1]);
        }
    }

    // ilagay and device name sa spinner
    // maeexecute nito ang spinner.setOnItemSelectedListener
    void populateWithDevice(Context context, Spinner spinner) {
        String name = Build.MANUFACTURER.toString().toUpperCase() + " " + Build.MODEL.toString();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        adapter.add(name);
    }

    // ilagay ang pangalan ng storage (array) sa recycler view
    private void populateWithStorage(RecyclerView recyclerView, ArrayList<String> array) {
        FolderViewAdapter adapter;
        int n;

        adapter = (FolderViewAdapter) recyclerView.getAdapter();

        // clear recyclerview
        n = adapter.getItemCount();
        adapter.clear();
        adapter.notifyItemRangeRemoved(0, n);

        // populate recyclerview with device storage
        for (String str : storage_names)
            adapter.add(new FolderViewItem(R.raw.drive2, str));

        n = array.size();
        adapter.notifyItemRangeInserted(0, n);
    }

    // ilagay ang pangalan ng sub directory under directory pathname sa recycler view
    private void populateWithDirectory(RecyclerView recyclerView, String pathname) {
        FolderViewAdapter adapter;
        ArrayList<String> array = new ArrayList<>();
        File file;
        File[] files;
        int n;
        String str;

        // clear recyclerview
        adapter = (FolderViewAdapter) recyclerView.getAdapter();

        n = adapter.getItemCount();
        adapter.clear();
        adapter.notifyItemRangeRemoved(0, n);

        // populate recyclerview with name of directory
        file = new File(pathname);
        files = file.listFiles();

        // error checking
        if(files == null) {
            Toast.makeText(getActivity(), "Get directory failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (File file1 : files)
            if (file1.isDirectory())
                array.add(file1.getName());

        array.sort(new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return compareString(s, t1);
            }
        });

        n = array.size();

        while (!array.isEmpty()) {
            str = array.remove(0);
            adapter.add(new FolderViewItem(R.raw.folder1, str));
        }

        adapter.notifyItemRangeInserted(0, n);
    }

    // ito ay nakalagay sa positive button ng dialog
    // ito ay para pagduktungin ang mga directory para sa restoreInstanceState
    private String saveInstanceState(ArrayList<String> array) {
        StringBuilder sb;
        String str;
        int i, n;

        n = array.size();
        str = array.get(0);

        sb = new StringBuilder(str);

        for (i=1; i<n; i++)
            sb.append("/").append(array.get(i));

        return sb.toString();
    }

    // para irestore ang piniling directory
    // ilagay ang nasave na directory sa spinner
    private void restoreInstanceState(String pathname, Spinner spinner) {
        ArrayAdapter<String> adapter;
        int i, i1, i2;
        boolean loop;
        String str;

        adapter = (ArrayAdapter<String>) spinner.getAdapter();

        str = Build.MANUFACTURER.toString().toUpperCase() + " " + Build.MODEL.toString();
        adapter.add(str);

        loop = true;
        i1 = 0;

        do {

            i2 = pathname.indexOf("/", i1);

            if(i2 < 0) {
                i2 = pathname.length();
                loop = false;
            }

            str = pathname.substring(i1, i2);

            //Log.d("KLGYN", str);

            adapter.add(str);

            i1 = i2 + 1;

        } while (loop);

        i = adapter.getCount() - 1;
        spinner.setSelection(i);
    }

    public static void setInitialDirectory(String pathname) {
        current_pathname = pathname;
    }

    public interface OnClickListener {
        void onClick(String str);
    }
}
