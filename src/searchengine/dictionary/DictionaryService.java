package searchengine.dictionary;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import searchengine.dictionary.trie.Trie;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
            reader.close();
            reader = new BufferedReader(new FileReader(backup));
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase();
                trie.insert(word);
                freq.put(word, 0);
            }
            reader.close();
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
     * Correct possible typos in query string.
     * @param query query string
     * @return corrected string or null
     */
    public static String correct(String query) {
        if (query == null) return null;
        String[] words = query.split("\\s+");
        if (words.length == 0) return null;
        String[] correction = new String[words.length];
        boolean corrected = false;
        for (int i = 0; i < words.length; i++) {
            if (freq.containsKey(words[i])) {
                correction[i] = words[i];
                continue;
            }
            int distance = words[i].length();
            String corrWord = words[i];
            for (String word : freq.keySet()) {
                int dist = editDistance(word, words[i]);
                if (dist < distance) {
                    distance = dist;
                    corrWord = word;
                } else if (dist == distance) {
                    if (!freq.containsKey(corrWord) || freq.get(word) > freq.get(corrWord)) {
                        corrWord = word;
                    }
                }
            }
            if (distance > 0) {
                correction[i] = corrWord;
                corrected = true;
            }
        }
        if (corrected) {
            StringBuilder sb = new StringBuilder(correction[0]);
            for (int i = 1; i < correction.length; i++) {
                sb.append(" ").append(correction[i]);
            }
            return sb.toString();
        } else {
            return null;
        }
    }

    /**
     * Write cache to file.
     * @param cache cache file
     */
    public static void destroy(String cache) {
        try {
            new File(cache).createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(cache));
            for (Map.Entry<String, Integer> entry : freq.entrySet()) {
                if (entry.getValue() > 0) {
                    writer.println(entry.getKey());
                    writer.println(entry.getValue());
                }
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Search a query string and get top ten matches in a JSON format.
     * @param query query string
     * @return top ten matches in a JSON format
     */
    public static String search(String query) {
        ArrayList<String> predictions = trie.getWordsStartsWith(query, 1000);
        if (predictions.size() < 20) {
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
        for (int i = 0; i < 20 && i < predictions.size(); i++) {
            ((JSONArray) jsonObject.get("predictions")).add(predictions.get(i));
        }
        return jsonObject.toJSONString();
    }

    /**
     * Compute edit distance of two words.
     * @param word1 word 1
     * @param word2 word 2
     * @return edit distance of two words
     */
    private static int editDistance(String word1, String word2) {
        int[][] dp = new int[word1.length() + 1][word2.length() + 1];
        for (int i = 0; i <= word1.length(); i++) {
            dp[i][0] = i;
        }
        for (int i = 0; i <= word2.length(); i++) {
            dp[0][i] = i;
        }
        for (int i = 1; i <= word1.length(); i++) {
            for (int j = 1; j <= word2.length(); j++) {
                int k = 1;
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    k = 0;
                }
                dp[i][j] = Math.min(dp[i - 1][j - 1] + k, Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
            }
        }
        return dp[word1.length()][word2.length()];
    }

    public static void main(String[] args) {
        DictionaryService.init("queries", "words.txt");
        System.out.println(DictionaryService.correct("priority queue"));
    }
}
