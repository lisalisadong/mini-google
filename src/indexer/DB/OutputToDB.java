package indexer.DB;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static indexer.DB.DBWrapper.INDEXER_DB_DIR;

/**
 * Created by QingxiaoDong on 5/8/17.
 */
public class OutputToDB {

    static DBWrapper db = new DBWrapper(DBWrapper.INDEXER_DB_DIR);
    static String inputDir;
    static BlockingQueue<File> queue = new LinkedBlockingQueue<>();

    public static void handle(File file) {
        int ii = 0;
        try {
//			BufferedReader reader = new BufferedReader(new FileReader("/Users/liujue/Desktop/output/part-r-00000"));
//			BufferedReader reader = new BufferedReader(new FileReader("9output/part-r-00000"));
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            String last = null;
            Word word = null;
            while ((line = reader.readLine()) != null) {
                // System.out.println(line);
                if (ii % 100 == 0) {
                    System.out.println(ii);
                }
                ii += 1;
                String[] parts = line.split("\t");
                // System.out.println(Arrays.toString(parts));
                String w = parts[0];
                if (!w.equals(last)) {
                    if (word != null) {
                        db.putWord(word);
                    }
                    word = db.addWord(w);
                    last = w;
                    double idf = Double.parseDouble(parts[3]);
                    word.addIdf(idf);
                }
                String docID = parts[1];
                double tf = Double.parseDouble(parts[2]);
                word.addTf(docID, tf);
                String[] titles = parts[4].split("\\D+");
                String[] contents = parts[5].split("\\D+");
                // System.out.println("titles: " + Arrays.toString(titles));
                // System.out.println("contents: " + Arrays.toString(contents));
                if (titles.length > 0) {
                    List<Integer> titlePos = new ArrayList<Integer>();
                    for (int i = 1; i < titles.length; i++) {
                        titlePos.add(Integer.parseInt(titles[i]));
                        // System.out.println("title: " + titlePos.get(i - 1));
                    }
                    word.addTitlePos(docID, titlePos);
                }
                if (contents.length > 0) {
                    List<Integer> contentPos = new ArrayList<Integer>();
                    for (int i = 1; i < contents.length; i++) {
                        contentPos.add(Integer.parseInt(contents[i]));
                        // System.out.println("content: " + contentPos.get(i -
                        // 1));
                    }
                    word.addContentPos(docID, contentPos);
                }
//				db.putWord(word);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("input: [input_directory]");
            System.exit(1);
        }
        inputDir = args[0];
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
                    while (!queue.isEmpty()) {
                        File file = queue.poll();
                        handle(file);
                    }
                }
            });
            pool[i].start();
        }
        for (Thread t : pool) {
            try {
                t.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        db.sync();
        db.close();
    }
}
