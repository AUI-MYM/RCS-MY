package evaluation;


import common.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MertErgun on 21.12.2015.
 */
public class User_evaluation extends User {
    public Map<Integer,Float> test_ratings;
    public User_evaluation(int id) {
        super(id);
        test_ratings = new HashMap<Integer, Float>();
    }
}
