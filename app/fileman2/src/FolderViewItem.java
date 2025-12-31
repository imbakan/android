package balikbayan.box.fileman;

public class FolderViewItem {
    private int icon;
    private String str;

    public FolderViewItem(int icon, String str) {
        this.icon = icon;
        this.str = str;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getString() {
        return str;
    }
}
