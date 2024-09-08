package balikbayan.box.clientbt;

public class Attribute {
    private String str;
    private long value;

    public Attribute(String str, long value) {
        this.str = str;
        this.value = value;
    }

    public String getString() {
        return str;
    }

    public void setString(String str) {
        this.str = str;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
