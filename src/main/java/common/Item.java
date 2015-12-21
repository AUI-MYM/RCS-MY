package common;

import java.util.*;

/**
 * Created by MertErgun on 3.11.2015.
 */
public class Item {
    public float norm;
    public int id;
    public SortedList similarities;
    public List<Feature> feautures;
    public Map<Integer,Integer> ratings;
    public Item(int id) {
        this.id = id;
        similarities = new SortedList(50);
        ratings = new HashMap<Integer, Integer>();
        feautures = new ArrayList<Feature>();
    }

    public Item(int id, float v) {
        this.id = id;
        this.norm = v;
        this.similarities = new SortedList(50);
        this.feautures = new ArrayList<Feature>();
        ratings = new HashMap<Integer, Integer>();
    }
}
