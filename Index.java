import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Index {
    private List<Pair<Integer, Integer>> index = new ArrayList<>(); // klucz, strona

    public Index() {
    }

    public List<Pair<Integer, Integer>> getIndex() {
        return index;
    }

    public void addIndexPair(Page page) {
        int key = page.getRecords().get(0).getKey();
        int pageId = page.getId();
        index.add(new Pair<>(key, pageId));
    }

    public Integer findPage(int key) {
        Pair<Integer, Integer> pair;
        for (int i=index.size()-1; i>=0; i--) {
            pair = index.get(i);
            if(key >= pair.getKey()) {
                return pair.getValue();
            }
        }
        return null;
    }

    public void clearIndex() {
        index.clear();
    }

    @Override
    public String toString() {
        String result = "=== Index ===\n";
        for (Pair<Integer,Integer> pair: index) {
            result += (pair.getKey() + " | " + pair.getValue() + "\n");
        }
        return result;
    }
}
