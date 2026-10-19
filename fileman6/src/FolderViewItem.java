package balikbayan.box.fileman_a6;

public class FolderViewItem {
    private int icon;
    private String str;

    public FolderViewItem(String str, int icon) {
        this.str = str;
        this.icon = icon;
    }

    public String getString() {
        return str;
    }

    public int getIcon() {
        return icon;
    }

}
