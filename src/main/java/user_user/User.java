package user_user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MertErgun on 25.10.2015.
 */
public class User {
    public Map<Integer,Float> ratings;
    public Map<Integer,Float> estimated_ratings;
    public List<Integer> old_results;
    public int user_id;
    public float avg_rating;
    public User(int i) {
        user_id = i;
        ratings = new HashMap<Integer, Float>();
        estimated_ratings = new HashMap<Integer, Float>();
        old_results = new ArrayList<Integer>();
    }

}
