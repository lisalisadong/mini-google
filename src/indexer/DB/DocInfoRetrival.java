package indexer.DB;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class DocInfoRetrival {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("input: [input_directory]");
			System.exit(1);
		}
		String inputDir = args[0];
		DBWrapper db = new DBWrapper(DBWrapper.INDEXER_DB_DIR);
		File folder = new File(inputDir);
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	String docID = file.getName();
		    	if (docID.equals(".DS_Store")) {
		    		continue;
		    	}
		    	try {
					String content = new String(Files.readAllBytes(Paths.get(inputDir + "/" + docID)));
					String[] parts = content.split("\t", 3);
					String url = parts[0];
					Document doc = Jsoup.parse(parts[2]);
					String title = doc.title();
					String description = "";
					Elements meta = doc.select("meta");
					if (meta.attr("name").equals("description")) {
						description = meta.attr("content");
					} else {
						String body = doc.body().text();
						description = body.substring(0, Math.min(300, body.length()));
					}
					DocInfo docInfo = new DocInfo(docID, url, title, description);
					db.putDocInfo(docInfo);
//					System.out.println("url: " + url + " title: " + title + " description: " + description);
				} catch (Exception e) {
				}
		        System.out.println("=============================docID: " + docID);
		    }
		}
		db.sync();
		db.close();
	}

}
