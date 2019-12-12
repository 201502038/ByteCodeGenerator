package listener.main;

public class Struct {
    private int count;
    private String[] field;

    public Struct(int count, String[] field) {
        this.count = count;
        this.field = field;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String[] getField() {
        return field;
    }

    public void setField(String[] field) {
        this.field = field;
    }
}
