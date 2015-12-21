package common;

import common.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MertErgun on 8.11.2015.
 */
public class Feature {
    public List<Item> items;
    public int id;
    public Feature(int id) {
        this.id = id;
        items = new ArrayList<Item>();
    }
}
