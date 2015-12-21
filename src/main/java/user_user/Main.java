package user_user; /**
 * Created by MertErgun on 25.10.2015.
 */

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import common.Key_Value_Pair;
import common.SimilarityPair;
import common.SortedList;
import common.User;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    static public Map<Integer, User> users = new HashMap<Integer, User>();
    static public List<MovieRating> top_movies = new ArrayList<MovieRating>();
    static public List<Recommendation> recommendations = new ArrayList<Recommendation>();
    private static String mainPath = "C:\\Users\\MertErgun\\IdeaProjects\\RCS\\input files\\user-user\\";

    public static LinkedHashMap sortHashMapByValuesD(Map passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues, Collections.reverseOrder());
        Collections.sort(mapKeys, Collections.reverseOrder());

        LinkedHashMap sortedMap = new LinkedHashMap();

        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((Integer) key, (Float) val);
                    break;
                }

            }

        }
        return sortedMap;
    }

    private static float getMax(float a, float b) {
        return a > b ? a : b;
    }

    private static void readOldResults() {
        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + "submit_best_item_content.csv"));
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                //top_movies.add(new MovieRating(Integer.parseInt(nextLine[0]), Float.parseFloat(nextLine[1])));
                int user_id = Integer.parseInt(nextLine[0]);
                User theUser = users.get(user_id);
                if (theUser == null) {
                    theUser = new User(user_id);
                    users.put(user_id, theUser);
                }
                for (String s : nextLine[1].split(" ")) {
                    theUser.old_results.add(Integer.parseInt(s));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void readTop() {

        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + "top_movies2.csv"));
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                top_movies.add(new MovieRating(Integer.parseInt(nextLine[0]), Float.parseFloat(nextLine[1])));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        private static void try1() {
            try {
                CSVReader reader = new CSVReader(new FileReader(mainPath + "similarity_10.csv"));
                int id = 1;
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    User theUser = users.get(id);
                    if (theUser != null) {
                        Map<Integer, Float> temp = new HashMap<Integer, Float>();
                        int numberOfSimilarUsers = 10;
                        for (int i = 0; i < numberOfSimilarUsers; i++) {
                            User otherUser = users.get(Integer.parseInt(nextLine[i]));
                            if (otherUser == null) continue;
                            if (Integer.parseInt(nextLine[i]) == 0) break;
                            temp.clear();
                            temp.putAll(theUser.ratings);
                            temp.keySet().retainAll(otherUser.ratings.keySet());
                            float sum = 0;
                            if (temp.keySet().size() == 0) continue;
                            for (Integer key : temp.keySet()) {
                                sum = sum - theUser.ratings.get(key) + otherUser.ratings.get(key);
                            }
                            sum = sum / temp.keySet().size();
                            temp.clear();
                            temp.putAll(otherUser.ratings);
                            temp.keySet().removeAll(theUser.ratings.keySet());
                            for (Integer key : temp.keySet()) {
                                float rating = otherUser.ratings.get(key) * Float.parseFloat(nextLine[i + numberOfSimilarUsers]);

                                //theUser.ratings.put(key, (int) rating);
                                if (!theUser.estimated_ratings.containsKey(key))
                                    theUser.estimated_ratings.put(key, rating);
                                // else
                                //   theUser.estimated_ratings.put(key, getMax(rating, theUser.estimated_ratings.get(key)));


                            }
                        }
                    } else {
                        // TODO
                    }
                    id = id + 1;
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    */
    private static void try3() {
        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + "similarity_50.csv"));
            int id = 1;
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                User theUser = users.get(id);
                if (theUser != null) {
                    Map<Integer, Float> temp = new HashMap<Integer, Float>();
                    int numberOfSimilarUsers = 50;
                    for (int i = 0; i < numberOfSimilarUsers; i++) {
                        User otherUser = users.get(Integer.parseInt(nextLine[i]));
                        if (otherUser == null) continue;
                        if (Integer.parseInt(nextLine[i]) == 0) break;
                        for (Integer ratingOfSimilarUser : otherUser.ratings.keySet()) {
                            if (theUser.ratings.containsKey(ratingOfSimilarUser)) continue;
                            SimilarityPair pair = theUser.estimated_ratings.get(ratingOfSimilarUser);
                            float similarity_coefficient = Float.parseFloat(nextLine[i + numberOfSimilarUsers]);
                            if (pair == null) {
                                pair = new SimilarityPair(otherUser.ratings.get(ratingOfSimilarUser) * similarity_coefficient,
                                        similarity_coefficient);
                                theUser.estimated_ratings.put(ratingOfSimilarUser, pair);
                            } else {
                                pair.rating_sum += otherUser.ratings.get(ratingOfSimilarUser) * similarity_coefficient;
                                pair.similarity_sum += similarity_coefficient;
                            }
                        }
                    }
                    for (Integer key : theUser.estimated_ratings.keySet()) {
                        SimilarityPair pair = theUser.estimated_ratings.get(key);
                        pair.rating_sum = pair.rating_sum / (pair.similarity_sum + 2.0f /*shrink term*/);
                        theUser.estimated_sorted_ratings.add(key, pair.rating_sum);
                    }
                    theUser.estimated_ratings.clear();
                } else {
                    // TODO
                }
                id = id + 1;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        private static void try2() {
            try {
                CSVReader reader = new CSVReader(new FileReader(mainPath + "similarity_pearson.csv"));
                CSVReader reader2 = new CSVReader(new FileReader(mainPath + "similarity.csv"));
                int id = 1;
                String[] nextLine;
                String[] nextLine2;
                while ((nextLine = reader.readNext()) != null && (nextLine2 = reader2.readNext()) != null) {
                    User theUser = users.get(id);
                    if (theUser != null) {
                        Map<Integer, Float> temp = new HashMap<Integer, Float>();
                        int numberOfSimilarUsers = 10;
                        Map<Integer, Float> sim_temp = new HashMap<Integer, Float>();
                        int i = 0;
                        for (; i < numberOfSimilarUsers; i++) {
                            User otherUser = users.get(Integer.parseInt(nextLine[i]));
                            if (otherUser == null) continue; //that means that user doesn't know anything go to next;
                            if (Integer.parseInt(nextLine[i]) == 0) break; // end of similar users
                            temp.clear();
                            temp.putAll(otherUser.ratings);
                            temp.keySet().removeAll(theUser.ratings.keySet()); //take the difference set of the movies rated
                            //ratings of the similar user which our user didn't rate
                            for (Integer key : temp.keySet()) {
                                float rating = (otherUser.ratings.get(key) - otherUser.avg_rating) * Float.parseFloat(nextLine[i + numberOfSimilarUsers]); // minus the avg rating of the user
                                if (!theUser.estimated_ratings.containsKey(key))
                                    theUser.estimated_ratings.put(key, rating);
                                else
                                    theUser.estimated_ratings.put(key, rating + theUser.estimated_ratings.get(key));

                                if (!sim_temp.containsKey(key))
                                    sim_temp.put(key, Float.parseFloat(nextLine[i + numberOfSimilarUsers]));
                                else
                                    sim_temp.put(key, sim_temp.get(key) + Float.parseFloat(nextLine[i + numberOfSimilarUsers]));
                            }
                        }
                        for (Integer key : sim_temp.keySet()) {//finish the weighted average
                            theUser.estimated_ratings.put(key, theUser.avg_rating + theUser.estimated_ratings.get(key) / sim_temp.get(key));
                        }

                    } else {
                        // TODO
                    }
                    id = id + 1;
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    */
    private static void readTrain() {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(mainPath + "sorted_train_set.csv"));
            String[] nextLine;
            int flag = 1;
            User u = new User(1);
            while ((nextLine = reader.readNext()) != null) {
                int current = Integer.parseInt(nextLine[0]);
                if (current != flag) {
                    users.put(flag, u);
                    u = new User(current);
                    u.ratings.put(Integer.parseInt(nextLine[1]), Float.parseFloat(nextLine[2]));
                    flag = current;
                } else {
                    u.ratings.put(Integer.parseInt(nextLine[1]), Float.parseFloat(nextLine[2]));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readAvgRatings() {
        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + "avg_rat.csv"));
            String[] nextLine;
            int count = 1;
            while ((nextLine = reader.readNext()) != null) {
                User theUser = users.get(count);
                if (theUser != null) {
                    theUser.avg_rating = Float.parseFloat(nextLine[0]);
                }
                count++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();


        }
    }

    private static void writeOutput() {
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(mainPath + "submit.csv"), ',', '\0', '\0');
            //String[] entries = "userId#testItems".split("#");
            //writer.writeNext(entries);
            for (Recommendation r : recommendations) {
                String s = "" + r.user + "," + StringUtils.join(r.recommendations.toArray(), ',');
                writer.writeNext(s.split(","));
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void recommend1() {
        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + "test_ev.csv"));
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                int id = Integer.parseInt(nextLine[0]);
                User u = users.get(id);
                Recommendation r = new Recommendation(id);
                LinkedHashMap l = sortHashMapByValuesD(u.estimated_ratings);
                Iterator it = l.keySet().iterator();
                int count = 0;
                while (it.hasNext() && count < 5) {
                    Object key = it.next();
                    float rat = (Float) l.get(key);
                    //if (rat < 1.9f) break;
                    r.recommendations.add((Integer) key);
                    count++;
                }
                int i = 0;
                while (count < 5) {
                    if (!u.ratings.containsKey(top_movies.get(i).movie) &&
                            !r.recommendations.contains(top_movies.get(i).movie)) {
                        r.recommendations.add(top_movies.get(i).movie);
                        count++;
                    }
                    i++;
                }
                recommendations.add(r);
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void recommend3() {
        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + "test_ev.csv"));
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                int id = Integer.parseInt(nextLine[0]);
                User u = users.get(id);
                Recommendation r = new Recommendation(id);
                Iterator it = u.estimated_sorted_ratings.theList.iterator();
                int count = 0;
                while (it.hasNext() && count < 2) {
                    Integer key = ((Key_Value_Pair) it.next()).key;
                    //float rat = u.estimated_sorted_ratings.get(key).value;
                    //if (rat < 1.9f) break;
                    r.recommendations.add(key);
                    count++;
                }
                int i = 0;
                while (count < 5) {
                    if (!u.ratings.containsKey(top_movies.get(i).movie) &&
                            !r.recommendations.contains(top_movies.get(i).movie)) {
                        r.recommendations.add(top_movies.get(i).movie);
                        count++;
                    }
                    i++;
                }
                recommendations.add(r);
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void recommend2() {
        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + "test_ev.csv"));
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                int id = Integer.parseInt(nextLine[0]);
                User u = users.get(id);
                Recommendation r = new Recommendation(id);
                Iterator it = u.estimated_sorted_ratings.theList.iterator();
                int count = 0;

                int i = 0;
                while (count < 3) {
                    if (!u.ratings.containsKey(u.old_results.get(i)) &&
                            !r.recommendations.contains(u.old_results.get(i))) {
                        r.recommendations.add(u.old_results.get(i));
                        count++;
                    }
                    i++;
                }
                recommendations.add(r);
                while (it.hasNext() && count < 5) {
                    Integer key = ((Key_Value_Pair) it.next()).key;
                    //float rat = u.estimated_sorted_ratings.get(key).value;
                    //if (rat < 1.9f) break;
                    if (!r.recommendations.contains(key)) {
                        r.recommendations.add(key);
                        count++;
                    }
                }
                while (count < 5) {
                    if (!u.ratings.containsKey(u.old_results.get(i)) &&
                            !r.recommendations.contains(u.old_results.get(i))) {
                        r.recommendations.add(u.old_results.get(i));
                        count++;
                    }
                    i++;
                }
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        readTrain();
        readAvgRatings();
        //readTop();
        readOldResults();
        try3();
        //recommend2();
        recommend_hybrid2();
        writeOutput();
        System.out.println("I'm done");
    }

    private static void recommend_hybrid() {
        int count_5 = 0;
        int count_4 = 0;
        int count_3 = 0;
        int count_2 = 0;
        int count_1 = 0;
        int count_0 = 0;
        int count_overall = 0;
        try {
            item_item.Main.main(null);
            CSVReader reader = new CSVReader(new FileReader(mainPath + "test_ev.csv"));
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                int id = Integer.parseInt(nextLine[0]);
                User u = users.get(id);
                if (u == null) u = new User(id);
                Recommendation r = new Recommendation(id);
                Iterator<Key_Value_Pair> it = u.estimated_sorted_ratings.theList.iterator();
                SortedList the_other_list = item_item.Main.users.get(id).estimated_sorted_ratings;
                int count = 0;
                int index_limiter = 0;
                while (count < 5 && it.hasNext() && index_limiter++ < 10) {
                    Key_Value_Pair pair = it.next();
                    Key_Value_Pair pair2 = the_other_list.get(pair.key);
                    if (pair2 != null) {
                        r.recommendations.add(pair.key);
                        count++;
                    } else {
                        continue;
                    }
                }
                switch (count) {
                    case 0:
                        count_0++;
                        break;
                    case 1:
                        count_1++;
                        break;
                    case 2:
                        count_2++;
                        break;
                    case 3:
                        count_3++;
                        break;
                    case 4:
                        count_4++;
                        break;
                    case 5:
                        count_5++;
                        break;
                }
                Iterator<Key_Value_Pair> it2 = the_other_list.theList.iterator();
                while (count < 5 && it2.hasNext()) {
                    Key_Value_Pair pair = it2.next();
                    if (pair != null && !r.recommendations.contains(pair.key)) {
                        r.recommendations.add(pair.key);
                        count++;
                    } else {
                        continue;
                    }
                }

                it = u.estimated_sorted_ratings.theList.listIterator();
                while (count < 5 && it.hasNext()) {
                    Key_Value_Pair pair = it.next();
                    if (pair != null && !r.recommendations.contains(pair.key)) {
                        r.recommendations.add(pair.key);
                        count++;
                    } else {
                        continue;
                    }
                }

                int i = 0;
                while (count < 5) {
                    if (!r.recommendations.contains(u.old_results.get(i))) {
                        r.recommendations.add(u.old_results.get(i));
                        count++;
                    }
                    i++;
                }
                recommendations.add(r);
                if (count < 5) count_overall++;

            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        float total = count_0 + count_1 + count_2 + count_3 + count_4 + count_5;
        System.out.println("count 0: " + count_0 + " %: " + (count_0 / total));
        System.out.println("count 1: " + count_1 + " %: " + (count_1 / total));
        System.out.println("count 2: " + count_2 + " %: " + (count_2 / total));
        System.out.println("count 3: " + count_3 + " %: " + (count_3 / total));
        System.out.println("count 4: " + count_4 + " %: " + (count_4 / total));
        System.out.println("count 5: " + count_5 + " %: " + (count_5 / total));
        System.out.println("count overall: " + count_overall);
    }

    private static void recommend_hybrid2() {
        int count_5 = 0;
        int count_4 = 0;
        int count_3 = 0;
        int count_2 = 0;
        int count_1 = 0;
        int count_0 = 0;
        int count_overall = 0;
        try {
            item_item.Main.main(null);
            item_item_collaborative.Main.main(null);
            CSVReader reader = new CSVReader(new FileReader(mainPath + "test_ev.csv"));
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                int id = Integer.parseInt(nextLine[0]);
                User u = users.get(id);
                if (u == null) u = new User(id);
                Recommendation r = new Recommendation(id);
                Iterator<Key_Value_Pair> it = u.estimated_sorted_ratings.theList.iterator();
                Iterator<Key_Value_Pair> it2 = item_item.Main.users.get(id).estimated_sorted_ratings.theList.iterator();
                Iterator<Key_Value_Pair> it3 = item_item_collaborative.Main.users.get(id).estimated_sorted_ratings.theList.iterator();
                SortedList theList = new SortedList(10);
                int count = 0;
                boolean flag = false;
                while (count < 5) {
                    Key_Value_Pair pair = null;
                    try {
                        pair = it.next();
                    } catch (java.util.NoSuchElementException e) {

                    }

                    Key_Value_Pair pair2 = null;
                    try {
                        pair2 = it2.next();
                    } catch (java.util.NoSuchElementException e) {

                    }
                    Key_Value_Pair pair3 = null;
                    try {
                        pair3 = it3.next();
                    } catch (java.util.NoSuchElementException e) {

                    }
                    flag = false;
                    if (pair != null) {
                        theList.add(pair.key, pair.value);
                        flag = true;
                    }
                    if (pair2 != null) {
                        theList.add(pair2.key, pair2.value * 1.1f);
                        flag = true;
                    }
                    if (pair3 != null) {
                        //theList.add(pair3.key,pair3.value*0.85f);
                        flag = true;
                    }
                    if (flag) count++;
                    else break;
                }
                int work_around_upper_bound = 5;
                for (int j = 0; j < work_around_upper_bound && j < count && j < theList.theList.size(); j++) {
                    Key_Value_Pair pair = (Key_Value_Pair) theList.theList.get(j);
                    if (pair != null) {
                        if (r.recommendations.contains(pair.key)) {
                            work_around_upper_bound++;
                        } else r.recommendations.add(pair.key);
                    } else break;
                }
                switch (count) {
                    case 0:
                        count_0++;
                        break;
                    case 1:
                        count_1++;
                        break;
                    case 2:
                        count_2++;
                        break;
                    case 3:
                        count_3++;
                        break;
                    case 4:
                        count_4++;
                        break;
                    case 5:
                        count_5++;
                        break;
                }

                int i = 0;
                while (count < 5 && i < u.old_results.size()) {
                    if (!r.recommendations.contains(u.old_results.get(i))) {
                        r.recommendations.add(u.old_results.get(i));
                        count++;
                    }
                    i++;
                }
                recommendations.add(r);
                if (count < 5) count_overall++;

            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        float total = count_0 + count_1 + count_2 + count_3 + count_4 + count_5;
        System.out.println("count 0: " + count_0 + " %: " + (count_0 / total));
        System.out.println("count 1: " + count_1 + " %: " + (count_1 / total));
        System.out.println("count 2: " + count_2 + " %: " + (count_2 / total));
        System.out.println("count 3: " + count_3 + " %: " + (count_3 / total));
        System.out.println("count 4: " + count_4 + " %: " + (count_4 / total));
        System.out.println("count 5: " + count_5 + " %: " + (count_5 / total));
        System.out.println("count overall: " + count_overall);
    }


}
