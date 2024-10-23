package balikbayan.box.opendialogbox;

public interface ListViewEventListener {
    void onItemChanged(ListViewItem item);
    void onItemSelected(ListViewItem item);
    void onItemUnselected();
}
