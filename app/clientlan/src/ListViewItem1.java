package balikbayan.box.client_lan;

public class ListViewItem1 {

    private String str;
    private int icon, depth, indent;
    private long id;
    private boolean collapse;

    public ListViewItem1(String str, long id, int icon, int depth, int indent) {
        this.str = str;
        this.id = id;
        this.icon = icon;
        this.depth = depth;
        this.indent = indent;
        collapse = true;
    }

    public String getString() {
        return str;
    }

    public void setString(String str) {
        this.str = str;
    }

    public long getId() {
        return id;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getDepth() {
        return depth;
    }

    public int getIndent() {
        return indent;
    }

    public void setCollapse(boolean collapse) {
        this.collapse = collapse;
    }

    public boolean isCollapse() {
        return collapse;
    }
}
