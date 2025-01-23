import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Page {
    private int size;
    private int b;
    private int id;
    private List<Record> records = new ArrayList<>();

    public Page(int id, int b) {
        this.id = id;
        this.b = b;
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return size;
    }



    public List<Record> getRecords() {
        return records;
    }

    public boolean isFull() {
        return (size == b);
    }

    public void addRecord(Record record) {
        records.add(record);
        size++;
    }

    public Record getRecord(int key) {
        for (int i=records.size()-1; i>=0; i--) {
            Record record = records.get(i);
            if(record.getKey()<=key) {
                return record;
            }
        }
        return null;
    }

    public Integer getRecordID(int key) {
        for (int i=records.size()-1; i>=0; i--) {
            Record record = records.get(i);
            if(record.getKey()<=key) {
                return i;
            }
        }
        return null;
    }
}
