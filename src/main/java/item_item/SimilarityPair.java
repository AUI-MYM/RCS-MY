package item_item;

/**
 * Created by MertErgun on 3.11.2015.
 */
public class SimilarityPair {
    public float similarity_sum;
    public float rating_sum;
    public SimilarityPair(float similarity_sum, float rating_sum) {
        this.similarity_sum = similarity_sum;
        this.rating_sum = rating_sum;
    }
}
