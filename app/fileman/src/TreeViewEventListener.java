package balikbayan.box.fileman06;

public interface TreeViewEventListener {
    void onSelChanged(TreeViewItem item);
    void onItemExpanding(TreeViewItem item, TreeViewAdapter adapter);
    void onItemExpanded(TreeViewItem item, TreeViewAdapter adapter);
}
