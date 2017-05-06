package searchengine.amazon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.Logger;

/**
 * Created by QingxiaoDong on 5/5/17.
 * This class call the Amazon Product Advertising API.
 */
public class ItemSearchService {

    private static final Logger log = new Logger(ItemSearchService.class.getSimpleName());
    /*
     * Your AWS Access Key ID, as taken from the AWS Your Account page.
     */
    private static final String AWS_ACCESS_KEY_ID = "AKIAJVTU6YAIEJI26SGA";

    /*
     * Your AWS Secret Key corresponding to the above ID, as taken from the AWS
     * Your Account page.
     */
    private static final String AWS_SECRET_KEY = "CbLiKWY8ycHdNMWQ2eoVi12oE6ISxiJcFbFK6R0O";

    /*
     * Use one of the following end-points, according to the region you are
     * interested in:
     *
     *      US: ecs.amazonaws.com
     *      CA: ecs.amazonaws.ca
     *      UK: ecs.amazonaws.co.uk
     *      DE: ecs.amazonaws.de
     *      FR: ecs.amazonaws.fr
     *      JP: ecs.amazonaws.jp
     *
     */
    private static final String ENDPOINT = "ecs.amazonaws.com";

    /*
     * The Item ID to lookup. The value below was selected for the US locale.
     * You can choose a different value if this value does not work in the
     * locale of your choice.
     */
    private static ArrayList<String> itemIds;

    public static ArrayList<AmazonItem> search(String keywords) {
        ArrayList<AmazonItem> items = new ArrayList<>();

        /*
         * Set up the signed requests helper
         */
        SignedRequestsHelper helper;
        try {
            helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return items;
        }

        String requestUrl;

        /* The helper can sign requests in two forms - map form and string form */

        /*
         * Here is an example in map form, where the request parameters are stored in a map.
         */
        Map<String, String> params = new HashMap<String, String>();
        params.put("Service", "AWSECommerceService");
        params.put("Version", "2011-08-01");
        params.put("Operation", "ItemSearch");
        params.put("ItemPage", "1");
        params.put("SearchIndex", "All");
        params.put("Keywords", "ps4 game");
        params.put("ResponseGroup", "Small");
        params.put("AssociateTag","th0426-20");

        requestUrl = helper.sign(params);
        log.warn("Signed Request is \"" + requestUrl + "\"");

        itemIds = fetchItemIds(requestUrl);

        for(int i = 0; i < itemIds.size(); i++) {
            params.clear();
            params.put("Service", "AWSECommerceService");
            params.put("Version", "2011-08-01");
            params.put("Operation", "ItemLookup");
            params.put("ItemPage", "1");
            params.put("IdType", "ISBN");
            params.put("ItemId", itemIds.get(i));
            params.put("SearchIndex", "All");
            params.put("AssociateTag","th0426-20");
            params.put("ResponseGroup", "Medium");
            requestUrl = helper.sign(params);
            log.warn("Signed Medium Request is \"" + requestUrl + "\"");
            AmazonItem item = fetchItemDetals(requestUrl);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    /*
     * Utility function to fetch the response from the service and extract the
     * item id from the XML.
     */
    private static ArrayList<String> fetchItemIds(String requestUrl) {
        ArrayList<String> titles = new ArrayList<String>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(requestUrl);
            NodeList nodes = doc.getElementsByTagName("ASIN");

            for(int i = 0; i < nodes.getLength(); i++) {
                titles.add(nodes.item(i).getTextContent());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return titles;
    }

    /*
    * Utility function to fetch the response from the service and extract the
    * item id from the XML.
    */
    private static AmazonItem fetchItemDetals(String requestUrl) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(requestUrl);

            // title
            Node node = doc.getElementsByTagName("Title").item(0);
            if (node == null) return null;
            String title = node.getTextContent();

            // image
            node = doc.getElementsByTagName("MediumImage").item(0).getFirstChild();
            if (node == null) return null;
            String image = node.getTextContent();

            // price
            node = doc.getElementsByTagName("FormattedPrice").item(0);
            if (node == null) return null;
            String price = node.getTextContent();

            // url
            node = doc.getElementsByTagName("DetailPageURL").item(0);
            if (node == null) return null;
            String url = node.getTextContent();

            return new AmazonItem(title, image, price, url);
        } catch (Exception e) {
            return null;
        }
    }

}