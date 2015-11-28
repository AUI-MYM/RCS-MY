package item_item_collaborative;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
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
    private static Map<Integer, Item> items = new HashMap<Integer, Item>();
    static Map<Integer, User> users = new HashMap<Integer, User>();
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
        readTrain();
        computeSimilarity_by_collaborative();
        writeSim();
        recommend();
        writeOutput();
    }

    private static void writeOutput() {
        CSVWriter writer = null;
        CSVReader reader = null;
        try {
            writer = new CSVWriter(new FileWriter(mainPath + "submit.csv"), ',');
            reader = new CSVReader(new FileReader(mainPath + "submit_user.csv"));
            String[] entries = "userId#testItems".split("#");
            writer.writeNext(entries);
            reader.readNext();
            String[] line = null;
            for (Recommendation r : recommendations) {
                User theUser = users.get(r.user);
                LinkedHashMap list = sortHashMapByValuesD(theUser.estimated_ratings,true);
                Iterator it = list.keySet().iterator();
                int count = 0;
                while (count < 5 && it.hasNext()) {
                    r.recommendations.add((Integer) it.next());
                    count++;
                }
                line = reader.readNext();
                if (count < 5) {
                    String[] items = line[1].split(" ");
                    int i = 0;
                    while (count < 5) {
                        int item = Integer.parseInt(items[i]);
                        if (!r.recommendations.contains(item)) {
                            r.recommendations.add(item);
                            count++;
                        }
                        i++;
                    }
                }
                String s = "" + r.user + "," + StringUtils.join(r.recommendations.toArray(), ' ');
                writer.writeNext(s.split(","));
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeSim() {
        CSVWriter writer = null;
        CSVReader reader = null;
        try {
            writer = new CSVWriter(new FileWriter(mainPath + "sim_com.csv"), ',');
            for (Integer key : items.keySet()) {
                Item item = items.get(key);
                String s = "";
                for (Integer key2 : item.similarities.keySet()) {
                    s += key2 + ",";
                    s += item.similarities.get(key2) + ",";
                }
                writer.writeNext(s.split(","));
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void recommend() {
        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + "test.csv"));
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                int id = Integer.parseInt(nextLine[0]);
                User u = users.get(id);
                Recommendation r = new Recommendation(id);
                for (Object key : u.ratings.keySet()) { //we should sort and get only the top 10 rating of a user
                    for (Object key_item_similar : items.get(key).similarities.keySet()) {
                        if (u.ratings.containsKey(key_item_similar)) continue;
                        float value = u.ratings.get(key) * items.get(key).similarities.get(key_item_similar);
                        SimilarityPair pair = u.estimated_ratings.get(key_item_similar);
                        if (pair != null) {
                            pair.rating_sum += value;
                            pair.similarity_sum += items.get(key).similarities.get(key_item_similar);
                        } else {
                            pair = new SimilarityPair(value, items.get(key).similarities.get(key_item_similar));
                            u.estimated_ratings.put((Integer)key_item_similar, pair);
                        }
                    }
                }
                for (Object key : u.estimated_ratings.keySet()) {
                    SimilarityPair pair = u.estimated_ratings.get(key);
                    pair.rating_sum = pair.rating_sum / (pair.similarity_sum + 2.0f);
                }
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
                temp_similarity.put((Integer) key, value);
            }

            //sort the similarity
            LinkedHashMap sortedHash = sortHashMapByValuesD(temp_similarity,false);
            Iterator it = sortedHash.keySet().iterator();
            int count = 0;
            while (it.hasNext() && count < 50) {
                Object key = it.next();
                item.similarities.put((Integer) key, (Float) sortedHash.get(key));
                count++;
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class pairComperator implements Comparator<SimilarityPair> {

    public int compare(SimilarityPair o1, SimilarityPair o2) {
        return -1 * Float.compare(o1.rating_sum, o2.rating_sum);
    }
}
