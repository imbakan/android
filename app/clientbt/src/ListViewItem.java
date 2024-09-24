package balikbayan.box.clientbt;

public class ListViewItem {
    private String str1, str2, str3;

    public ListViewItem(String str1, String str2, String str3) {
        this.str1 = str1;
        this.str2 = str2;
        this.str3 = str3;
    }

    public String getString1() {
        return str1;
    }

    public void setString1(String str) {
        str1 = str;
    }

    public String getString2() {
        return str2;
    }

    public void setString2(String str) {
        str2 = str;
    }

    public String getString3() {
        return str3;
    }

    public void setString3(String str) {
        str3 = str;
    }
}
