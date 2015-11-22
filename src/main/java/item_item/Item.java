package item_item;

import java.util.*;

/**
 * Created by MertErgun on 3.11.2015.
 */
public class Item {
    public float norm;
    public int id;
    public LinkedHashMap<Integer,Float> similarities;
    public List<Feature> feautures;
    public Item(int id) {
        this.id = id;
        similarities = new LinkedHashMap<Integer, Float>();
        feautures = new ArrayList<Feature>();
    }

    public Item(int id, float v) {
        this.id = id;
        this.norm = v;
        this.similarities = new LinkedHashMap<Integer, Float>();
        this.feautures = new ArrayList<Feature>();
    }
}
