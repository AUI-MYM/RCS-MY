package item_item_collaborative;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by MertErgun on 26.11.2015.
 * This class is written in order to keep a sorted list which has the size 50
 */
public class SortedList {
    private final static int max_number_of_similarity = 100;
    SortedArrayList theList = new SortedArrayList();
    public void add(Integer key, Float value) {
        if (theList.size() >= max_number_of_similarity) {
            float theLast = ((Key_Value_Pair) theList.get(max_number_of_similarity - 1)).value;
            if (value > theLast) {
                theList.remove(max_number_of_similarity - 1 );
                theList.insertSorted(new Key_Value_Pair(key, value));
            }
        }
        else {
            theList.insertSorted(new Key_Value_Pair(key, value));
        }
    }

    public Key_Value_Pair get(Integer key) {
        for (Object o : theList) {
            Key_Value_Pair object = (Key_Value_Pair)o;
            if (object.key == key) {
                return object;
            }
        }
        return null;
    }
}

class SortedArrayList extends ArrayList {

    @SuppressWarnings("unchecked")
    public void insertSorted(Key_Value_Pair value) {
        add(value);
        for (int i = size()-1; i > 0 && compare(value, (Key_Value_Pair) get(i-1)) < 0; i--)
            Collections.swap(this, i, i-1);
    }
    public int compare(Key_Value_Pair o1, Key_Value_Pair o2) {
        return -1 * Float.compare(o1.value, o2.value);
    }
}