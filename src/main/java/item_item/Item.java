package item_item;

import java.util.*;

/**
 * Created by MertErgun on 3.11.2015.
 */
public class Item {
    public float norm;
    public int id;
    public Similarity similarities;
    public List<Feature> feautures;
    public Item(int id) {
        this.id = id;
        similarities = new Similarity();
        feautures = new ArrayList<Feature>();
    }

    public Item(int id, float v) {
        this.id = id;
        this.norm = v;
        this.similarities = new Similarity();
        this.feautures = new ArrayList<Feature>();
    }
}
