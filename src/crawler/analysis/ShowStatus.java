package crawler.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import crawler.storage.DBWrapper;

public class ShowStatus {
		
	public static void main(String[] args) {
		if(args.length != 2 && args.length != 1) {
			System.out.println("Usage: [DB Path] [(optional)if this param exists, print msg in console]");
			return;
		}
		
		DBWrapper db = new DBWrapper(args[0]);
		db.setup();
			
		System.out.println("Page crawled: " + db.pIdx.map().size());
		System.out.println("URL Frontier size: " + db.uwIdx.map().size());
		System.out.println("URL set size: " + db.vIdx.map().size());
		System.out.println("Robot num:" + db.rIdx.map().size());
		
	}
		
}
