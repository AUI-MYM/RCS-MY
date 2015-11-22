package item_item;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import user_user.Recommendation;
import item_item.Item;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by MertErgun on 3.11.2015.
 */
public class Main {
    public static final String mainPath = "D:\\Yedek\\desktop backup 23-02-2015\\polimi 2nd year\\Recommender Systems\\competition\\item-item\\";
    private static Map<Integer, Item> items = new HashMap<Integer, Item>();
    private static Map<Integer, Feature> features = new HashMap<Integer, Feature>();
    static Map<Integer, User> users = new HashMap<Integer, User>();
    static List<Recommendation> recommendations = new ArrayList<Recommendation>();

    private static void readItems() {
        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + "icm.csv"));
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                int item_id = Integer.parseInt(nextLine[0]);
                int feature_id = Integer.parseInt(nextLine[1]);
                Feature feature = features.get(feature_id);
                if (feature == null) {
                    feature = new Feature(feature_id);
                    features.put(feature_id, feature);
                }
                Item item = items.get(item_id);
                if (item == null) {
                    item = new Item(item_id);
                    items.put(item_id, item);
                }
                item.feautures.add(feature);
                feature.items.add(item);
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readNorms() {
        try {
            CSVReader reader = new CSVReader(new FileReader(mainPath + "norms.csv"));
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
        readNorms();
        readItems();
        readTrain();
        computeSimilarity();
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
                    pair.rating_sum = pair.rating_sum / pair.similarity_sum;
                }
                recommendations.add(r);
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void computeSimilarity() {
        Map<Integer, Float> temp_similarity = new HashMap<Integer, Float>();
        for (Object key_item : items.keySet()) {//print something
            Item item = items.get(key_item);
            //System.out.println("item id: " + key_item.toString());
            temp_similarity.clear();
            for (Feature feature : item.feautures) {
                if (feature.items.size() < 500)
                    for (Item similar_item : feature.items) {
                        if (similar_item.id == key_item) continue;
                        if (temp_similarity.containsKey(similar_item.id)) { //increment
                            temp_similarity.put(similar_item.id, temp_similarity.get(similar_item.id) + 1);
                        } else {
                            temp_similarity.put(similar_item.id, 1.0f);
                        }
                    }
            }

            //divide the norm
            for (Object key : temp_similarity.keySet()) {
                float value = temp_similarity.get(key);
                value = value / ((item.norm * items.get(key).norm) + 2.5f /*shrink term */);
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
            //System.out.println("item id: " + key_item.toString() + " finished");
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
