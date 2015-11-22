package user_user; /**
 * Created by MertErgun on 25.10.2015.
 */

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    static Map<Integer, User> users = new HashMap<Integer, User>();
    static List<MovieRating> top_movies = new ArrayList<MovieRating>();
    static List<Recommendation> recommendations = new ArrayList<Recommendation>();
    private static String mainPath = "D:\\Yedek\\desktop backup 23-02-2015\\polimi 2nd year\\Recommender Systems\\competition\\user-user\\";

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
            CSVReader reader = new CSVReader(new FileReader(mainPath + "submit4-27-10.csv"));
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                //top_movies.add(new MovieRating(Integer.parseInt(nextLine[0]), Float.parseFloat(nextLine[1])));
                int user_id = Integer.parseInt(nextLine[0]);
                User theUser = users.get(user_id);
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

    private static void try1() {
        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + "similarity.csv"));
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
                            float rating = (otherUser.ratings.get(key) - sum) * Float.parseFloat(nextLine[i + numberOfSimilarUsers]);

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

    private static void readTrain() {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(mainPath + "user_sorted.csv"));
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
            writer = new CSVWriter(new FileWriter(mainPath + "submit.csv"), ',');
            String[] entries = "userId#testItems".split("#");
            writer.writeNext(entries);
            for (Recommendation r : recommendations) {
                String s = "" + r.user + "," + StringUtils.join(r.recommendations.toArray(), ' ');
                writer.writeNext(s.split(","));
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void recommend1() {
        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + "test.csv"));
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

    private static void recommend2() {
        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + "test.csv"));
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
                    // if (rat < 2.9f) break;
                    r.recommendations.add((Integer) key);
                    count++;
                }
                int i = 0;
                while (count < 5) {
                    if (!u.ratings.containsKey(u.old_results.get(i)) &&
                            !r.recommendations.contains(u.old_results.get(i))) {
                        r.recommendations.add(u.old_results.get(i));
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


    public static void main(String[] args) {
        readTrain();
        readAvgRatings();
        //readTop();
        readOldResults();
        try2();
        recommend2();
        writeOutput();
        System.out.println("I'm done");
    }


}
