package item_item_collaborative;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import common.Item;
import common.Key_Value_Pair;
import common.SimilarityPair;
import common.User;
import org.apache.commons.lang3.StringUtils;
import user_user.Recommendation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by MertErgun on 3.11.2015.
 */
public class Main {
    public static final String mainPath = "C:\\Users\\MertErgun\\IdeaProjects\\RCS\\input files\\item-item\\";
    //public static final String mainPath = "C:\\Users\\MertErgun\\IdeaProjects\\RCS\\input files\\test\\";
    private static Map<Integer, Item> items = new HashMap<Integer, Item>();
    public static Map<Integer, User> users = new HashMap<Integer, User>();
    static List<Recommendation> recommendations = new ArrayList<Recommendation>();

    private static void readNorms(String filename) {
        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + filename));
            String[] nextLine;
            int id = 1;
            while ((nextLine = reader.readNext()) != null) {
                if (items.containsKey(id)) {
                    items.get(id).norm = Float.parseFloat(nextLine[0]);
                } else {
                    items.put(id, (new Item(id, Float.parseFloat(nextLine[0]))));
                }
                id++;
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        readNorms("norm_item_ratings.csv");
        System.out.println("Norm file has been read");
        System.out.println("Items have been read");
        readTrain();
        System.out.println("Train data was read");
        computeSimilarity_by_collaborative();
        System.out.println("End of calculation of similarity");
        recommend();
        System.out.println("End of predicting ratings");
        writeOutput();
        System.out.println("End of writing the top 5 best prediction");
    }

    private static void writeOutput() {
        CSVWriter writer = null;
        CSVReader reader = null;
        int counter = 0 ;
        try {
            writer = new CSVWriter(new FileWriter(mainPath + "submit.csv"), ',');
            reader = new CSVReader(new FileReader(mainPath + "submit_best_item_content.csv"));
            String[] entries = "userId#testItems".split("#");
            writer.writeNext(entries);
            reader.readNext();
            String[] line = null;
            for (Recommendation r : recommendations) {
                User theUser = users.get(r.user);
                //LinkedHashMap list = sortHashMapByValuesD(theUser.estimated_ratings,true);
                Iterator it = theUser.estimated_sorted_ratings.theList.iterator();
                int count = 0;
                while (count < 5 && it.hasNext()) {
                    Key_Value_Pair pair = (Key_Value_Pair) it.next();
                    if (r.recommendations.contains(pair.key)) continue;
                    r.recommendations.add(pair.key);
                    count++;
                }
                line = reader.readNext();/*
                if (count < 5) {
                    String[] items = line[1].split(" ");
                    int i = 0;
                    counter++;
                    while (count < 5) {
                        int item = Integer.parseInt(items[i]);
                        if (!r.recommendations.contains(item)) {
                            r.recommendations.add(item);
                            count++;
                        }
                        i++;
                    }
                }*/
                String s = "" + r.user + "," + StringUtils.join(r.recommendations.toArray(), ' ');
                writer.writeNext(s.split(","));
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("For " + counter + " many users I couldn't predict 5 movies");
    }

    private static void recommend() {
        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + "test.csv"));
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                int id = Integer.parseInt(nextLine[0]);
                User u = users.get(id);
                if (u == null) {
                    u = new User(id);
                    users.put(id,u);
                }
                Recommendation r = new Recommendation(id);
                for (Object key : u.ratings.keySet()) { //we should sort and get only the top 10 rating of a user
                    for (Object key_item_similar_o : items.get(key).similarities.theList) {
                        Key_Value_Pair key_item_similar = (Key_Value_Pair) key_item_similar_o;
                        if (u.ratings.containsKey(key_item_similar.key)) continue;
                        float value = u.ratings.get(key) * items.get(key).similarities.get(key_item_similar.key).value;
                        SimilarityPair pair = u.estimated_ratings.get(key_item_similar.key);
                        if (pair != null) {
                            pair.rating_sum += value;
                            pair.similarity_sum += items.get(key).similarities.get(key_item_similar.key).value;
                        } else {
                            pair = new SimilarityPair(value, items.get(key).similarities.get(key_item_similar.key).value);
                            u.estimated_ratings.put((Integer) key_item_similar.key, pair);
                        }
                    }
                }
                for (Object key : u.estimated_ratings.keySet()) {
                    SimilarityPair pair = u.estimated_ratings.get(key);
                    pair.rating_sum = pair.rating_sum / (pair.similarity_sum +2.0f /* shrink term*/);
                    u.estimated_sorted_ratings.add((Integer)key, pair.rating_sum);
                }
                u.estimated_ratings.clear();
                recommendations.add(r);
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void computeSimilarity_by_collaborative() {
        Float obj = null;
        Map<Integer, Float> temp_similarity = new HashMap<Integer, Float>();
        for (Integer key_item : items.keySet()) {
            temp_similarity.clear();
            Item item = items.get(key_item);
            //System.out.println("Current item: " + key_item.toString());
            for (Integer key_user : item.ratings.keySet()) {
                User theUser = users.get(key_user);
                if (theUser == null || theUser.ratings == null) continue;
                for (Integer key_item_rated : theUser.ratings.keySet()) {
                    if (key_item_rated.equals(key_item)) continue;
                    if (item.ratings.size() > items.get(key_item_rated).ratings.size()) continue;
                    else if (item.ratings.size() == items.get(key_item_rated).ratings.size() && key_item < key_item_rated) continue;// to eleminate the duplications
                    float rating = item.ratings.get(key_user) * theUser.ratings.get(key_item_rated);
                    obj = temp_similarity.get(key_item_rated);
                    if (obj == null) {
                        temp_similarity.put(key_item_rated,rating);
                    }
                    else {
                        temp_similarity.put(key_item_rated, obj + rating);
                    }
                }
            }

            //divide the norm
            for (Object key : temp_similarity.keySet()) {
                float value = temp_similarity.get(key);
                value = value / ((item.norm * items.get(key).norm) + 2.0f /* shrink term */);
                setSimilarity((Integer) key, key_item ,value);
            }

        }
    }

    public static LinkedHashMap sortHashMapByValuesD(Map passedMap, boolean flag) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        if (flag)
            Collections.sort(mapValues, new pairComperator());
        else
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
                    sortedMap.put((Integer) key,val);
                    break;
                }

            }

        }
        return sortedMap;
    }

    private static void readTrain() {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(mainPath + "user_sorted.csv"));
            //reader = new CSVReader(new FileReader(mainPath + "user_sorted.csv"));
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
                Item item = items.get(Integer.parseInt(nextLine[1]));
                item.ratings.put(u.user_id, Integer.parseInt(nextLine[2]));
            }
            users.put(flag,u);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setSimilarity(Integer item1, Integer item2, float value) {
        items.get(item2).similarities.add(item1, value);
        items.get(item1).similarities.add(item2, value);
    }
}

class pairComperator implements Comparator<SimilarityPair> {

    public int compare(SimilarityPair o1, SimilarityPair o2) {
        return -1 * Float.compare(o1.rating_sum, o2.rating_sum);
    }
}
