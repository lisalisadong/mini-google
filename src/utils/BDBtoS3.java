package utils;

//import java.io.FileNotFoundException;
//import java.io.PrintWriter;
//import java.io.UnsupportedEncodingException;
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
    

//	public static String FILE = "../db1";
//	
//    public static void main(String[] args) {
//        // open db connection
//        DBWrapper db = new DBWrapper(FILE);
//        db.setup();
//        
//        // open PrintWriter 
//        PrintWriter writer = null;
//        try {
//            writer = new PrintWriter("../pagerank-input.txt", "UTF-8");
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

//            sb.append(url).append(",");
//            for (String link : page.getLinks()) {
//                sb.append(link).append(";");
//            }
//            if (sb.charAt(sb.length() - 1) == ';') {
//            	sb.setLength(sb.length() - 1);
//            }
//            writer.println(sb.toString());
//            System.out.println("Finish file " + ++num);
//        }
//        
//        // close PrintWriter
//        writer.close();
//    }
    
}
