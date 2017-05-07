package crawler.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import crawler.storage.DBWrapper;
import crawler.storage.CrawledPage;

public class IndexerConvertor {

	private static final String ROOTDIR = "../indexer";
	
	public static void main(String[] args) {
		if(args.length != 2 && args.length != 1) {
			System.out.println("Usage: [DB Path] [(optional)if this param exists, print msg in console]");
			return;
		}
		File f = new File(ROOTDIR);
		if(!f.exists()) {
			f.mkdirs();
		}
		String file = "/file";
		int num = 0;
		
		DBWrapper db = new DBWrapper(args[0]);
		db.setup();
		
		for(String url: db.pIdx.map().keySet()) {

			try {
				++num;
				FileWriter fw = new FileWriter(ROOTDIR + "/" + num);
				BufferedWriter bw = new BufferedWriter(fw);
				
				CrawledPage p = db.getPage(url);
				String s =  url + "\t"  + p.getContentType() 
					+ "\t" + new String(p.getContent()) + "\n";
				
				bw.write(s);

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();
				
				System.out.println("finished writing " + num);

//				if(num == 100) break;
			} catch (Exception e) {

			}
		}
		
	}
}
