import javafx.util.Pair;

public class Record {
    private int key;
    private Pair<Double, Double> data;
    private Integer overflow = null;
    private Integer position = null;

    private boolean isInOverflow = false;
    private boolean deleted = false;
    public Record(int key, Pair<Double, Double> data) {
        this.key = key;
        this.data = data;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Record(int key, Pair<Double, Double> data, boolean deleted, Integer overflow) {
        this.key = key;
        this.data = data;
        this.overflow = overflow;
        this.deleted = deleted;
    }

    public int getKey() {
        return key;
    }

    public Pair<Double, Double> getData() {
        return data;
    }

    public Integer getOverflow() {
        return overflow;
    }

    public void setOverflow(Integer overflow) {
        this.overflow = overflow;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isInOverflow() {
        return isInOverflow;
    }

    public void setInOverflow(boolean inOverflow) {
        isInOverflow = inOverflow;
    }

    @Override
    public String toString() {
        return "K: " + key +
                "\tV: [" + data.getKey() + ", " + data.getValue() +
                " ]\t\t D: " + (deleted?"1":"0") +
                "\tO: " + (overflow==null?"-":overflow);
    }
}
