package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Created by QingxiaoDong on 5/7/17.
 */
public class Combiner {

    private static final String INPUT_DIR = "indexer_input/original";
    private static final String OUTPUT_DIR = "indexer_input/combined";

    public static void main(String[] args) throws IOException {
        new File("indexer_input").mkdir();
        File inputDir = new File(INPUT_DIR);
        new File(OUTPUT_DIR).mkdir();
        File[] files = inputDir.listFiles();
        if (files == null) return;
        String outputFile = OUTPUT_DIR + "/file.";
        int fileIdx = 0;
        int numFiles = 0;
        int threshold = 300;
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile + fileIdx), StandardCharsets.UTF_8);
        for (File f : files) {
            if (f.isFile()) {
                if (numFiles == threshold) {
                    numFiles = 0;
                    writer.close();
                    fileIdx++;
                    writer = new OutputStreamWriter(new FileOutputStream(outputFile + fileIdx), StandardCharsets.UTF_8);
                }
                BufferedReader br = new BufferedReader(new FileReader(f));
                while (br.ready()) {
                    writer.write(br.readLine().replace("\n", " "));
                }
                br.close();
                writer.write("\n");
                numFiles++;
            }
        }
        writer.close();
    }
}
