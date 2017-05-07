package indexer.DB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class DBWrapper {

	public static final String INDEXER_DB_DIR = "IndexerDB";
	private static final String STORE_NAME = "Indexer";
	private static String envDirectory = null;
	private static Environment myEnv;
	private static EntityStore store;

	public PrimaryIndex<String, Word> wordIndex;

	public DBWrapper(String envDir) {
		this.envDirectory = envDir;
		// if the directory doesn't exist, create a new one
		File directory = new File(envDirectory);
		if (!directory.exists()) {
			directory.mkdir();
		}
		setup();
	}

	/**
	 * call by constructor when the class is instantiated
	 * set up the db environment
	 */
	public void setup() {
		try {
			// Open the environment. Create it if it does not already exist.
			EnvironmentConfig envConfig = new EnvironmentConfig();
			StoreConfig storeConfig = new StoreConfig();
			envConfig.setAllowCreate(true);
			envConfig.setCacheSize(1000000);
			storeConfig.setAllowCreate(true);
			myEnv = new Environment(new File(envDirectory), envConfig);
			store = new EntityStore(myEnv, STORE_NAME, storeConfig);

			wordIndex = store.getPrimaryIndex(String.class, Word.class);

		} catch (DatabaseException dbe) {
			dbe.printStackTrace();
		}
	}

	public void close() {
		store.close();
		myEnv.close();
	}

	public void sync() {
		store.sync();
		myEnv.sync();
	}

//	public void addWord(Word word) {
//		wordIndex.put(word);
//	}
	
	public Word getWord(String word) {
		return wordIndex.get(word);
	}
	
	public Word addWord(String word) {
		Word w = new Word(word);
		wordIndex.put(w);
		sync();
		return w;
	}
	
	public void putWord(Word word) {
		wordIndex.put(word);
	}
	
	public Set<String> getWordDocs(String word) {
		return wordIndex.get(word).getDocs();
	}

	public static void main(String[] args) {
		DBWrapper db = new DBWrapper(INDEXER_DB_DIR);
		try {
			BufferedReader reader = new BufferedReader(new FileReader("/Users/liujue/Desktop/output/part-r-00000"));
			String line = reader.readLine();
			int lineNum = 1;
			while (line != null) {
				String[] parts = line.split("\\t");
				System.out.println("linePer: " + lineNum/1795090.0 + "num: " + lineNum++ );
//				System.out.println(Arrays.toString(parts));
				String w = parts[0];
				Word word = db.getWord(w);
				if (word == null) {
					word = db.addWord(w);
				}
				String docID = parts[1];
				double tf = Double.parseDouble(parts[2]);
				double idf = Double.parseDouble(parts[3]);
				word.addInfo(docID, tf, idf);
				String[] titles = parts[4].split("\\D+");
				String[] contents = parts[5].split("\\D+");
//				System.out.println("titles: " + Arrays.toString(titles));
//				System.out.println("contents: " + Arrays.toString(contents));
				if (titles.length > 0) {
					List<Integer> titlePos = new ArrayList<Integer>();
					for (int i = 1; i < titles.length; i++) {
						titlePos.add(Integer.parseInt(titles[i]));
//						System.out.println("title: " + titlePos.get(i - 1));
					}
					word.addTitlePos(docID, titlePos);
				}
				if (contents.length > 0) {
					List<Integer> contentPos = new ArrayList<Integer>();
					for (int i = 1; i < contents.length; i++) {
						contentPos.add(Integer.parseInt(contents[i]));
//						System.out.println("content: " + contentPos.get(i - 1));
					}
					word.addContentPos(docID, contentPos);
				}
				db.putWord(word);
				
				line = reader.readLine();
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		db.sync();
		db.close();
		
		// test
//		System.out.println(db.wordIndex.count());
//		Word w = db.getWord("research");
//		Set<String> docs = w.getDocs();
//		for (String id : docs) {
//			System.out.println("doc: " + id + " tf: " + w.getTf(id));
//			List<Integer> cont = w.getContentPos(id);
//			for (int i : cont) {
//				System.out.println("content pos: " + i);
//			}
//			
//		}
	}
	
}
