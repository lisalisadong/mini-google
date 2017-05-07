package searchengine.amazon;

/**
 * Created by QingxiaoDong on 5/5/17.
 */
public class AmazonItem {

    public String title;
    public String image;
    public String price;
    public String url;
    public String label;
    public String binding;

    public AmazonItem(String title, String image, String price, String url, String label, String binding){
        this.title = title;
        this.image = image;
        this.price = price;
        this.url = url;
        this.label = label;
        this.binding = binding;
    }
}
