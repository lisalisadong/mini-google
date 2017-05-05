package crawler.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import crawler.storage.DBWrapper;

public class ShowHosts {

	private static final String FILENAME = "../hosts.txt";
		
	public static void main(String[] args) {
		if(args.length != 2 && args.length != 1) {
			System.out.println("Usage: [DB Path] [(optional)if this param exists, print msg in console]");
			return;
		}
		
		boolean v = (args.length == 2);
		
		DBWrapper db = new DBWrapper(args[0]);
		db.setup();
		
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {

			fw = new FileWriter(FILENAME);
			bw = new BufferedWriter(fw);
			String str = db.rIdx.map().size() + " hosts in total:\n";
			bw.write(str);
			if(v) System.out.println(str);
			for(String host: db.rIdx.map().keySet()) {
				bw.write(host + "\n");
				if(v) System.out.println(host);
			}
			bw.flush();
			db.sync();
				
		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}

	}
		
}
