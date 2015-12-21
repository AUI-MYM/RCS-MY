package common;

import java.util.ArrayList;
import java.util.Collections;

public class SortedArrayList extends ArrayList {

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
