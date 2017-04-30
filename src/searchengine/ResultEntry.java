package searchengine;

/**
 * Created by QingxiaoDong on 4/29/17.
 *
 * ResultEntry is a wrapper class for a result entry. It store information about a page, e.g.
 *      - title,
 *      - location,
 *      - digest,
 *      - last modified time,
 *      - scores associated with the page (tf-idf...)
 */
public class ResultEntry {
    String title;
    String location;
    String digest;
    String lastModified;
    double tf;
    double idf;
}
