package evaluation;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by MertErgun on 21.12.2015.
 */
public class Main {
    public static final String mainPath = "C:\\Users\\MertErgun\\IdeaProjects\\RCS\\input files\\evaluation\\";
    public static Map<Integer, User_evaluation> users = new HashMap<Integer, User_evaluation>();
    public static void read_train_data() {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(mainPath + "train_set.csv"));
            String[] nextLine;
            User_evaluation u = null;
            while ((nextLine = reader.readNext()) != null) {
                int current = Integer.parseInt(nextLine[0]);
                u = users.get(current);
                if (u != null) {
                    u.ratings.put(Integer.parseInt(nextLine[1]), Float.parseFloat(nextLine[2]));
                } else {
                    u = new User_evaluation(current);
                    u.ratings.put(Integer.parseInt(nextLine[1]), Float.parseFloat(nextLine[2]));
                    users.put(current, u);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Train data is read");
    }

    public static void read_test_data() {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(mainPath + "test_set.csv"));
            String[] nextLine;
            User_evaluation u = null;
            while ((nextLine = reader.readNext()) != null) {
                int current = Integer.parseInt(nextLine[0]);
                u = users.get(current);
                if (u != null) {
                    u.test_ratings.put(Integer.parseInt(nextLine[1]), Float.parseFloat(nextLine[2]));
                } else {
                    u = new User_evaluation(current);
                    u.test_ratings.put(Integer.parseInt(nextLine[1]), Float.parseFloat(nextLine[2]));
                    users.put(current, u);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Train data is read");
    }

    public static void main(String[] args) {
        read_train_data();
        read_test_data();
        System.out.println("I'm done");
    }
}
