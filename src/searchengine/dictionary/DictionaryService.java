package searchengine.dictionary;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import searchengine.dictionary.trie.Trie;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by QingxiaoDong on 5/6/17.
 */
public class DictionaryService {

    private static Trie trie = new Trie();
    private static HashMap<String, Integer> freq = new HashMap<>();

    /**
     * Init dictionary with cached queries and backup dictionary.
     * @param cache cached queries
     * @param backup backup dictionary
     */
    public static void init(String cache, String backup) {
        try {
            String line;
            new File(cache).createNewFile();
            BufferedReader reader = new BufferedReader(new FileReader(cache));
            while ((line = reader.readLine()) != null) {
                trie.insert(line);
                freq.put(line, Integer.parseInt(reader.readLine()));
            }
            new File(backup).createNewFile();
            reader = new BufferedReader(new FileReader(backup));
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase();
                trie.insert(word);
                freq.put(word, 0);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Put a query into the dictionary.
     * @param query search query
     */
    public static void put(String query) {
        trie.insert(query);
        if (!freq.containsKey(query)) {
            freq.put(query, 1);
        } else {
            freq.put(query, freq.get(query) + 1);
        }
    }

    /**
     * Search a query string and get top ten matches in a JSON format.
     * @param query query string
     * @return top ten matches in a JSON format
     */
    public static String search(String query) {
        ArrayList<String> predictions = trie.getWordsStartsWith(query, 1000);
        if (predictions.size() < 10) {
            int i = query.lastIndexOf(" ");
            if (i != -1) {
                String lastWord = query.substring(i + 1);
                ArrayList<String> advPred = trie.getWordsStartsWith(lastWord, 1000);
                for (String s : advPred) {
                    String completed = query.substring(0, i + 1) + s;
                    if (!predictions.contains(completed)) {
                        predictions.add(completed);
                    }
                }
            }
        }
        Collections.sort(predictions, (o1, o2) -> {
            int freq1 = 0;
            if (freq.containsKey(o1)) {
                freq1 = freq.get(o1);
            }
            int freq2 = 0;
            if (freq.containsKey(o2)) {
                freq2 = freq.get(o2);
            }
            return freq2 - freq1;
        });
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("predictions", new JSONArray());
        for (int i = 0; i < 10; i++) {
            ((JSONArray) jsonObject.get("predictions")).add(predictions.get(i));
        }
        return jsonObject.toJSONString();
    }
}
