package balikbayan.box.opendialogbox;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

public class OpenFileDialog extends DialogFragment {

    public static final int OPEN_FILE = 1000;

    private AlertDialog dialog;
    private Handler handler;
    private Context context;
    private String pathname;
    private String[] storage_name;
    private String[] storage_path;

    public OpenFileDialog(Handler handler, Context context, String pathname) {
        this.handler = handler;
        this.context = context;
        this.pathname = pathname;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        final Button button1 = view.findViewById(R.id.button1);
        final Spinner spinner1 = view.findViewById(R.id.spinner1);
        final RecyclerView recyclerView1 = view.findViewById(R.id.recyclerView1);
        final TextView textView1 = view.findViewById(R.id.textView4);

        //  +------------------------------------------------------------------------+
        //  |                              back button                               |
        //  +------------------------------------------------------------------------+

        // ibalik sa dating directory
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popFolder(spinner1);
            }
        });

        //  +------------------------------------------------------------------------+
        //  |                              spinner                                   |
        //  +------------------------------------------------------------------------+

        // iset ang data ng spinner
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item) {

            // iset ang mga dropdown text
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                String str;

                // lagyan ng elbow character sa unahan ng folder, hindi kasama ang device
                if (position == 0)
                    str = getItem(position);
                else
                    str = "└╼ " + getItem(position);

                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView)view.findViewById(android.R.id.text1);
                textView.setText(str);

                return view;
            }

            // iset ang display text
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return super.getView(position, convertView, parent);
            }

        };

        spinner1.setAdapter(adapter1);

        // iset ang event listener ng spinner
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // gawing huling item ang piniling item
                trimSpinnerAdapter((Spinner)adapterView, i);

                // kung ang i equal zero, device name ang pinili
                // ienable lang ang back arrow icon kung hindi device ang pinili
                button1.setEnabled(i != 0);

                if (i == 0) {

                    // kung device name ang pinili, lagyan ng mga drive ang recycler view
                    populateWithDrive(recyclerView1, textView1, storage_name, storage_path);

                } else {

                    // kung hindi device name ang pinili, lagyan ng mga folder o file ang recycler view
                    String pathname = getPathname((Spinner)adapterView, storage_name, storage_path);
                    populateWithFolder(recyclerView1, textView1, storage_name, storage_path, pathname);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //  +------------------------------------------------------------------------+
        //  |                        Recycler view                                   |
        //  +------------------------------------------------------------------------+

        ListViewAdapter adapter2 = new ListViewAdapter(context, new ListViewEventListener() {

            // punta sa piniling directory
            @Override
            public void onItemChanged(ListViewItem item) {
                pushFolder(spinner1, item.getString1());
            }

            // idisable ang spinner at dalawang button pagsinelect ang item
            @Override
            public void onItemSelected(ListViewItem item) {
                spinner1.setEnabled(false);
                button1.setEnabled(false);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }

            // ienable ang spinner at dalawang button paginanselect ang item
            @Override
            public void onItemUnselected() {
                spinner1.setEnabled(true);
                button1.setEnabled(true);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        recyclerView1.setLayoutManager(new LinearLayoutManager(context));
        recyclerView1.setAdapter(adapter2);

        //  +------------------------------------------------------------------------+
        //  |                           dialog button                                |
        //  +------------------------------------------------------------------------+

        builder.setPositiveButton("open", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ListViewAdapter adapter;
                ListViewItem item;
                String[] str = new String[2];
                Message msg;

                str[0] = getPathname(spinner1, storage_name, storage_path);

                adapter = (ListViewAdapter) recyclerView1.getAdapter();
                item = adapter.getSelectedItem();
                str[1] =  item.getString1();

                msg = handler.obtainMessage(OPEN_FILE, str);
                msg.sendToTarget();
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //  +------------------------------------------------------------------------+
        //  |                               initialize                               |
        //  +------------------------------------------------------------------------+

        getStorages(context);
        boolean result = setPathname(spinner1, storage_name, storage_path, pathname);

        if (!result) {
            populateWithDevice(spinner1);
        }

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
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

    // A B = negative       B A = positive
    private int compareString(ListViewItem item1, ListViewItem item2) {
        int n1, n2;
        String s1, s2, str1, str2;

        str1 = item1.getString1();
        str2 = item2.getString1();

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

    // gawing huling item ang piniling item
    // halimbawa: kung may 7 item sa spinner at ang piniling item ay 2
    // alisin ang 4 na item sa baba
    //
    //     0
    //     1
    //     2 <-- i
    //     3           1 ---------+
    //     4           2          | alisin ang 4 item na 'to
    //     5           3          |
    //     6           4 ---------+
    //
    private void trimSpinnerAdapter(Spinner spinner, int i) {
        ArrayAdapter<String> adapter;
        int j, k, n;

        adapter = (ArrayAdapter<String>) spinner.getAdapter();
        k = adapter.getCount() - 1;

        if (i < k) {

            n = k - i;
            for (j=0; j<n; j++) {
                k = adapter.getCount() - 1;
                adapter.remove(adapter.getItem(k));
            }
        }
    }

    // matitriger nito ang event onItemSelected ng spinner
    private void pushFolder(Spinner spinner, String str) {
        ArrayAdapter<String> adapter;
        int i;

        adapter = (ArrayAdapter<String>) spinner.getAdapter();
        adapter.add(str);

        i = adapter.getCount() - 1;
        spinner.setSelection(i);
    }

    // matitriger nito ang event onItemSelected ng spinner
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

    // kunin ang path name na nasa spinner
    private String getPathname(Spinner spinner, String[] name, String[] path) {
        ArrayAdapter<String> adapter;
        ArrayList<String> array = new ArrayList<>();
        String str, str1, str2;
        StringBuilder sb;
        int i, n;

        adapter = (ArrayAdapter<String>) spinner.getAdapter();
        n = adapter.getCount();

        // kunin ang mga folder name na nasa spinner
        for (i=0; i<n; i++)
            array.add(adapter.getItem(i));

        // alisin ang device name
        array.remove(0);

        // palitan ng path name ang storage name
        str1 = array.remove(0);

        str2 = "";
        for (i=0; i<name.length; i++)
            if (str1.compareTo(name[i]) == 0) {
                str2 = path[i];
                break;
            }

        // buuin ang path name
        sb = new StringBuilder(str2);

        while (!array.isEmpty()) {
            str = array.remove(0);
            sb.append("/").append(str);
        }

        return  sb.toString();
    }

    // kunin ang mga storage at ilagay sa storage_name at storage_path
    private void getStorages(Context context) {
        ArrayList<String> array1 = new ArrayList<>();
        ArrayList<String> array2 = new ArrayList<>();
        Set<String> volumes;
        String[] array;
        String str, str1, str2;
        int i, n;

        // internal storage
        File file = Environment.getExternalStorageDirectory();
        str = file.toString();
        array1.add("Internal Storage");
        array2.add(str);

        // external storage
        volumes = MediaStore.getExternalVolumeNames(context);
        n = volumes.size();
        if (n > 0) {
            array = new String[n];
            volumes.toArray(array);
            str = "/storage/" + array[1];
            array1.add("SD Card");
            array2.add(str);
        }

        n = array1.size();

        storage_name = new String[n];
        storage_path = new String[n];

        i = 0;

        while (!array1.isEmpty()) {

            str1 = array1.remove(0);
            str2 = array2.remove(0);

            storage_name[i] = str1;
            storage_path[i] = str2;

            ++i;
        }
    }

    //
    private boolean setPathname(Spinner spinner, String[] name, String[] path, String str) {
        ArrayAdapter<String> adapter;
        ArrayList<String> array = new ArrayList<>();
        File[] files;
        File file;
        String str1;
        int i, i1, i2;
        boolean loop;

        // alamin kung valid ang path name
        str1 = "";
        for (i=0; i<name.length; i++)
            if (str.contains(name[i])) {
                str1 = str.replace(name[i], path[i]);
                break;
            }

        file = new File(str1);
        files = file.listFiles();

        if (files == null) return false;

        // iextract ang mga directory sa path name
        loop = true;
        i1 = 0;

        do {

            i2 = str.indexOf("/", i1);

            if(i2 < 0) {
                i2 = str.length();
                loop = false;
            }

            str1 = str.substring(i1, i2);

            array.add(str1);

            i1 = i2 + 1;

        } while(loop);

        // ilagay sa spinner ang mga nakuha
        adapter = (ArrayAdapter<String>) spinner.getAdapter();

        str1 = Build.MANUFACTURER.toString().toUpperCase() + " " + Build.MODEL.toString();
        adapter.add(str1);

        while (!array.isEmpty()) {
            str1 = array.remove(0);
            adapter.add(str1);
        }

        i = adapter.getCount() - 1;
        spinner.setSelection(i);

        return true;
    }

    // ilagay ang pangalan ng device sa spinner
    // matitriger nito ang event onItemSelected ng spinner
    private void populateWithDevice(Spinner spinner) {

        String name = Build.MANUFACTURER.toString().toUpperCase() + " " + Build.MODEL.toString();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        adapter.add(name);
    }

    // ilagay ang mga pangalan ng drive sa recycler view
    private void populateWithDrive(RecyclerView recyclerView, TextView textView, String[] name, String[] path) {
        ListViewAdapter adapter;
        File file;
        String str;
        int i, n;

        // iclear muna ang recycler view
        adapter = (ListViewAdapter) recyclerView.getAdapter();
        n = adapter.getItemCount();
        adapter.clear();
        adapter.notifyItemRangeRemoved(0, n);

        // tapos, lagyan ng mga drive ito
        n = name.length;
        for (i=0; i<n; i++) {
            file = new File(path[i]);
            str = String.format(Locale.US, "%,d free of %,d", file.getFreeSpace(), file.getTotalSpace());
            adapter.add(new ListViewItem(R.raw.drive1, name[i], "", str));
        }
        adapter.notifyItemRangeInserted(0, n);

        textView.setText(String.format(Locale.US, "%d drives", n));
    }

    // ilagay ang pangalan ng mga folder o ng mga file sa recycler view
    private void populateWithFolder(RecyclerView recyclerView, TextView textView, String[] name, String[] path, String str) {
        ListViewAdapter adapter;
        File[] files;
        File file;
        ArrayList<String> array1 = new ArrayList<>();
        ArrayList<ListViewItem> array2 = new ArrayList<>();
        ListViewItem item;
        Calendar c;
        String str1, str2, str3, str4;
        int n, n1, n2, YYYY, MM, DD, hh, mm, ss;

        n1 = n2 = 0;

        // iclear muna ang recycler view
        adapter = (ListViewAdapter) recyclerView.getAdapter();
        n = adapter.getItemCount();
        adapter.clear();
        adapter.notifyItemRangeRemoved(0, n);

        // kunin ang mga directory o mga file
        file = new File(str);
        files = file.listFiles();

        // kung wala, walang gagawin
        if(files != null) {

            // paghiwalayin ang directory at file
            for (File file1 : files)
                if (file1.isDirectory()) {

                    array1.add(file1.getName());

                } else {

                    c = Calendar.getInstance();
                    c.setTimeInMillis(file1.lastModified());
                    YYYY = c.get(Calendar.YEAR);
                    MM = c.get(Calendar.MONTH) + 1;
                    DD = c.get(Calendar.DAY_OF_MONTH);
                    hh = c.get(Calendar.HOUR_OF_DAY);
                    mm = c.get(Calendar.MINUTE);
                    ss = c.get(Calendar.SECOND);

                    if (hh == 12) {
                        str4 = "PM";
                    } else if (hh > 12) {
                        str4 = "PM";
                        hh -= 12;
                    } else {
                        str4 = "AM";
                    }

                    str1 = file1.getName();
                    str2 = String.format(Locale.US, "%d-%d-%d %2d:%02d:%02d %s", MM, DD, YYYY, hh, mm, ss, str4);
                    str3 = String.format(Locale.US, "%,d", file1.length());

                    array2.add(new ListViewItem(R.raw.file1, str1, str2, str3));
                }

            // directory
            if (!array1.isEmpty()) {

                n1 = array1.size();

                // sort directory
                array1.sort(new Comparator<String>() {
                    @Override
                    public int compare(String s, String t1) {
                        return compareString(s, t1);
                    }
                });

                // ilagay sa recycler view
                while (!array1.isEmpty()) {
                    str = array1.remove(0);
                    adapter.add(new ListViewItem(R.raw.folder1, str, "", ""));
                }

            }

            // file
            if (!array2.isEmpty()) {

                n2 = array2.size();

                // sort file
                array2.sort(new Comparator<ListViewItem>() {
                    @Override
                    public int compare(ListViewItem listViewItem, ListViewItem t1) {
                        return compareString(listViewItem, t1);
                    }
                });

                // ilagay sa recycler view
                while (!array2.isEmpty()) {
                    item = array2.remove(0);
                    adapter.add(new ListViewItem(R.raw.file1, item.getString1(), item.getString2(), item.getString3()));
                }

            }

            adapter.notifyItemRangeInserted(0, n1 + n2);
        }

        textView.setText(String.format(Locale.US, "%d Folders   %d Files", n1, n2));
    }
}
