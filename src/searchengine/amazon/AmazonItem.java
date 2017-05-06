package searchengine.amazon;

/**
 * Created by QingxiaoDong on 5/5/17.
 */
public class AmazonItem {

    private String title;
    private String image;
    private String price;
    private String url;

    public AmazonItem(String title, String image, String price, String url){
        this.title = title;
        this.image = image;
        this.price = price;
        this.url = url;
    }
}
