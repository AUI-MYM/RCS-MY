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
    public static final String mainPath = "C:\\Users\\MertErgun\\IdeaProjects\\RCS\\input files\\item-item\\";
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
        System.out.println("Norm file has been read");
        readItems();
        System.out.println("Items have been read");
        readTrain();
        System.out.println("Train data was read");
        computeSimilarity();
        System.out.println("End of calculation of similarity");
        recommend();
        System.out.println("End of predicting ratings");
        writeOutput();
        System.out.println("End of writing the top 5 best prediction");
    }

    private static void writeOutput() {
        CSVWriter writer = null;
        CSVReader reader = null;
        int counter = 0;
        user_user.Main.main(null);
        try {
            writer = new CSVWriter(new FileWriter(mainPath + "submit.csv"), ',');
            reader = new CSVReader(new FileReader(mainPath + "submit_best_item_content.csv"));
            String[] entries = "userId#testItems".split("#");
            writer.writeNext(entries);
            reader.readNext();
            String[] line = null;
            for (Recommendation r : recommendations) {
                User theUser = users.get(r.user);
                Iterator it = theUser.estimated_sorted_ratings.theList.iterator();
                int count = 0;
                while (count < 5 && it.hasNext()) {
                    Key_Value_Pair pair = (Key_Value_Pair) it.next();
                    if (r.recommendations.contains(pair.key)) continue;
                    r.recommendations.add(pair.key);
                    count++;
                }
                line = reader.readNext();
                if (count < 5) {
                    String[] items = line[1].split(" ");
                    counter++;
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
                        if (u.ratings.containsKey(key_item_similar)) continue;
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
                    pair.rating_sum = pair.rating_sum / (pair.similarity_sum + 2.0f);
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

    private static void computeSimilarity() {
        Map<Integer, Float> temp_similarity = new HashMap<Integer, Float>();
        for (Integer key_item : items.keySet()) {//print something
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
                value = value / ((item.norm * items.get(key).norm) + 2.0f /*shrink term */);
                setSimilarity((Integer) key, key_item ,value);
            }

        }
    }

    private static void readTrain() {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(mainPath + "user_sorted_filtered.csv"));
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
