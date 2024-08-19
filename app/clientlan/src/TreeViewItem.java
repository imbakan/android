package balikbayan.box.clientlan;

public class TreeViewItem {

    private TreeViewItem parent;
    private String str;
    private long value;
    private int icon, hierarchy, padding;
    private boolean is_collapse;

    public TreeViewItem(TreeViewItem parent, String str, long value, int icon, int padding, int hierarchy) {
        this.parent = parent;
        this.str = str;
        this.value = value;
        this.icon = icon;
        this.padding = padding;
        this.hierarchy = hierarchy;
        is_collapse = true;
    }

    public TreeViewItem getParent() {
        return parent;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long id) {
        this.value = value;
    }

    public String getString() {
        return str;
    }

    public void setString(String str) {
        this.str = str;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int changeIcon(int icon) {
        int icon2;

        if (icon == R.raw.device1)
            icon2 = R.raw.device2;
        else if (icon == R.raw.device2)
            icon2 = R.raw.device1;
        else if (icon == R.raw.drive1)
            icon2 = R.raw.drive2;
        else if (icon == R.raw.drive2)
            icon2 = R.raw.drive1;
        else if (icon == R.raw.folder1)
            icon2 = R.raw.folder2;
        else if (icon == R.raw.folder2)
            icon2 = R.raw.folder1;
        else
            icon2 = R.raw.device1;

        return icon2;
    }

    public int getPadding() {
        return padding;
    }

    public int getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(int hierarchy) {
        this.hierarchy = hierarchy;
    }

    public boolean isCollapse() {
        return is_collapse;
    }

    public void setCollapse(boolean collapse) {
        is_collapse = collapse;
    }

}
