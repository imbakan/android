package balikbayan.box.aswitch;

public class LightAttribute {
    private String str;
    private int val;

    public LightAttribute(String str, int val) {
        this.str = str;
        this.val = val;
    }

    public String getString() {
        return str;
    }

    public void setString(String str) {
        this.str = str;
    }

    public int getValue() {
        return val;
    }

    public void setValue(int val) {
        this.val = val;
    }
}
