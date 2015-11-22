package user_user;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MertErgun on 25.10.2015.
 */
public class Recommendation {
    public int user;
    public List<Integer> recommendations;
    public Recommendation(int id) {
        user = id;
        recommendations = new ArrayList<Integer>();
    }
}
