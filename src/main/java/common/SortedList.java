package common;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by MertErgun on 26.11.2015.
 * This class is written in order to keep a sorted list which has the size 50
 */
public class SortedList {
    private int max_number_of_similarity = 100;
    public SortedArrayList theList = new SortedArrayList();

    public SortedList(int i) {
        max_number_of_similarity = i;
    }

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

