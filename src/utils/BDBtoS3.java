package utils;

//import java.io.FileNotFoundException;
//import java.io.PrintWriter;
//import java.io.UnsupportedEncodingException;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//
//import javax.xml.bind.DatatypeConverter;
//
//import crawler.storage.CrawledPage;
//import crawler.storage.DBWrapper;

/**
 * Transfer BDB crawled pages to S3 files
 * 
 * @author yishang
 *
 */
public class BDBtoS3 {

    public static String FILE = "./db";

//    private static String hashUrl(String url) {
//        try {
//            MessageDigest digest = MessageDigest.getInstance("MD5");
//            digest.reset();
//            digest.update(url.getBytes("utf-8"));
//            return DatatypeConverter.printHexBinary(digest.digest());
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public static void main(String[] args) {
//        // open db connection
//        DBWrapper db = new DBWrapper(FILE);
//        db.setup();
//
//        // open PrintWriter
//        PrintWriter writer = null;
//        try {
//            writer = new PrintWriter("pagerank-input.txt", "UTF-8");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        int num = 0;
//
//        // loop through all pages
//        for (String url : db.pIdx.keys()) {
//            CrawledPage page = db.pIdx.get(url);
//            StringBuilder sb = new StringBuilder();
//            sb.append(hashUrl(url)).append("\t");
//            for (String link : page.getLinks()) {
//                sb.append(hashUrl(link)).append("\t");
//            }
//            if (sb.charAt(sb.length() - 1) == '\t') {
//                sb.setLength(sb.length() - 1);
//            }
//            writer.println(sb.toString());
//            System.out.println("Finish file " + ++num);
//        }
//
//        // close PrintWriter
//        writer.close();
    }

}
