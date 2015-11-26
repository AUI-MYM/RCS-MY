package item_item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

/**
 * Created by MertErgun on 26.11.2015.
 * This class is written in order to keep a sorted list which has the size 50
 */
public class Similarity {
    private final static int max_number_of_similarity = 50;
    SortedArrayList similarities = new SortedArrayList();
    public void add(Integer key, Float value) {
        if (similarities.size() >= max_number_of_similarity) {
            float theLast = ((Similarity_Key_Value)similarities.get(max_number_of_similarity - 1)).value;
            if (value > theLast) {
                similarities.remove(max_number_of_similarity - 1 );
                similarities.insertSorted(new Similarity_Key_Value(key, value));
            }
        }
        else {
            similarities.insertSorted(new Similarity_Key_Value(key, value));
        }
    }

    public Similarity_Key_Value get(Integer key) {
        for (Object o : similarities) {
            Similarity_Key_Value object = (Similarity_Key_Value)o;
            if (object.key == key) {
                return object;
            }
        }
        return null;
    }
}

class SortedArrayList extends ArrayList {

    @SuppressWarnings("unchecked")
    public void insertSorted(Similarity_Key_Value value) {
        add(value);
        for (int i = size()-1; i > 0 && compare(value, (Similarity_Key_Value) get(i-1)) < 0; i--)
            Collections.swap(this, i, i-1);
    }
    public int compare(Similarity_Key_Value o1, Similarity_Key_Value o2) {
        return -1 * Float.compare(o1.value, o2.value);
    }
}

