package indexer.DB;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;


public class DocInfoRetrival {

	static DBWrapper db = new DBWrapper(DBWrapper.INDEXER_DB_DIR);
	static String inputDir;
	static BlockingQueue<File> queue = new LinkedBlockingQueue<>();

	private static void handle(File file) {
		if (file.isFile()) {
			String docID = file.getName();
			if (docID.equals(".DS_Store")) {
				return;
			}
			try {
				String content = new String(Files.readAllBytes(Paths.get(inputDir + "/" + docID)));
				String[] parts = content.split("\t", 3);
				String url = parts[0];
				Document doc = Jsoup.parse(parts[2]);
				String title = doc.title();
				String description = "";
				Elements meta = doc.select("meta");
				for (Element m : meta) {
					if (m.attr("name").equals("description")) {
						description = m.attr("content");
						break;
					}
				}
				if (description.length() < 10) {
					String body = doc.body().text();
					description = body.substring(0, Math.min(300, body.length()));
				}
				DocInfo docInfo = new DocInfo(docID, url, title, description);
				db.putDocInfo(docInfo);
				System.out.println("url: " + url + " title: " + title + " description: " + description);
			} catch (Exception e) {
			}
			System.out.println("=============================docID: " + docID);
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("input: [input_directory]");
			System.exit(1);
		}
		inputDir = args[0];
		db = new DBWrapper(DBWrapper.INDEXER_DB_DIR);
		File folder = new File(inputDir);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			queue.add(file);
		}

		Thread[] pool = new Thread[20];
		for (int i = 0; i < pool.length; i++) {
			pool[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					File file = queue.poll();
					handle(file);
				}
			});
		}
		db.sync();
		db.close();
	}

}
