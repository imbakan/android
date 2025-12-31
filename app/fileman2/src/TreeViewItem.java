package balikbayan.box.fileman;

public class TreeViewItem {
    private TreeViewItem parent;
    private String str;
    private int icon, depth, padding;
    private boolean is_collapse;

    public TreeViewItem(TreeViewItem parent, String str, int icon, int padding, int depth) {
        this.parent = parent;
        this.str = str;
        this.icon = icon;
        this.padding = padding;
        this.depth = depth;
        is_collapse = true;
    }

    public TreeViewItem getParent() {
        return parent;
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

    public int getIcon(int icon) {
        int icon2;

        if (icon == R.raw.drive1)
            icon2 = R.raw.drive2;
        else if (icon == R.raw.drive2)
            icon2 = R.raw.drive1;
        else if (icon == R.raw.folder1)
            icon2 = R.raw.folder2;
        else if (icon == R.raw.folder2)
            icon2 = R.raw.folder1;
        else
            icon2 = R.raw.file1;

        return icon2;
    }

    public int getPadding() {
        return padding;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isCollapse() {
        return is_collapse;
    }

    public void setCollapse(boolean collapse) {
        is_collapse = collapse;
    }
}
