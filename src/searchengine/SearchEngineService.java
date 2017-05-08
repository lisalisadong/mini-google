package searchengine;

import indexer.DB.DBWrapper;
import indexer.DB.DocInfo;
import indexer.DB.Word;
import indexer.WordTokenizer;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import utils.Logger;
import utils.Stemmer;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import static indexer.DB.DBWrapper.INDEXER_DB_DIR;
import static indexer.IndexerMapWorker.STOP_LIST;

/**
 * Created by QingxiaoDong on 4/29/17.
 */
public class SearchEngineService {

    private static Logger log = new Logger(SearchEngineService.class.getSimpleName());

    private static Stemmer stemmer = new Stemmer();

    private static LRUCache<ResultEntry[][]> cache = new LRUCache<>(100); // LRU with 100 cached queries

    private static final DBWrapper INDEXER = new DBWrapper(INDEXER_DB_DIR);

    private static Map<String, Double> pageRank = new HashMap<>();


    private static Set<String> urls = new HashSet<>();

    private static Map<String, DocInfo> docs = INDEXER.docInfoIndex.map();


    public static void init(String pageRankFile) {
//        loadPageRank(pageRankFile);
        loadSeedUrls("seed_urls");
    }

    /**
     * Returns the number of results that are expected.
     * @param query query string
     * @return total number of results that are expected.
     */
    public static int preSearch(String query) {
        // get {word:occurrences} map from query
        Map<String, Integer> wordsOccur = decomposeQuery(query);
        log.warn("Number of words in query " + wordsOccur.size());

        if (cache.get(query) != null) {
            log.warn("The result is already cached");
            return cache.get(query)[0].length;
        } else {
            log.warn("Getting new result entries");
            ResultEntry[][] entries = new ResultEntry[2][];
            entries[0] = getAllEntries(wordsOccur);
            entries[1] = new ResultEntry[entries[0].length];
            cache.put(query, entries);
            return entries[0].length;
        }
    }

    /**
     * Search with a query. Return NUM results starting from RANK-th.
     * The rank is 0-indexed.
     * For example rank = 0, num = 10 will return first 10 results.
     *
     * @param query search query
     * @param rank rank of the first result to be returned
     * @param num number of results to be returned
     * @return sorted result entries
     */
    public static ResultEntry[] search(String query, int rank, int num) {

        // get {word:occurrences} map from query
        Map<String, Integer> wordsOccur = decomposeQuery(query);

        // in case this is a new search query and has not been pre-searched, should not happen
        if (cache.get(query) == null) {
            log.warn("Get new query search");
            ResultEntry[][] entries = new ResultEntry[2][];
            entries[0] = getAllEntries(wordsOccur);
            entries[1] = new ResultEntry[entries[0].length];
            cache.put(query, entries);
        }

        // out of bound, should not happen
        if (rank >= cache.get(query)[0].length) {
            return null;
        }
        num = Math.min(num, cache.get(query)[0].length - rank);

        // cache sorted result
        if (!sorted(query, rank, rank + num - 1)) {
            long time = System.nanoTime();
            sortAndCache(query, rank, rank + num - 1);
//            for (int i = rank; i < rank + num; i++) {
//                queryDocumentDetail(cache.get(query)[1][i]);
//            }
            log.warn("number of seconds used in sorting " + 1.0 * (System.nanoTime() - time) / 1000000000);
        }

        // get result
        ResultEntry[] resultEntries = new ResultEntry[num];
        System.arraycopy(cache.get(query)[1], rank, resultEntries, 0, num);
        return resultEntries;
    }

    /**
     * Decompose a query string:
     *  - split the string to words
     *  - use porter stemmer to extract the stem of the word
     *  - increment the number of occurrences of the word
     * Returns the stemmed words and their number of occurrences in a map.
     * @param query query string
     * @return the stemmed words and their number of occurrences in a map
     */
    private static Map<String, Integer> decomposeQuery(String query) {
        Map<String, Integer> res = new HashMap<>();
        String[] words = query.split("\\s+");
        for (String word : words) {
            String stemmed = stemmer.stem(word);
            if (!res.containsKey(stemmed)) {
                res.put(stemmed, 1);
            } else {
                res.put(stemmed, res.get(stemmed) + 1);
            }
        }
        return res;
    }

    /**
     * Get all the candidate result entries associated with the word(s) in the input collection.
     * @param words words to be searched
     * @return all candidate result entries
     */
    private static ResultEntry[] getAllEntries(Map<String, Integer> words) {
        HashSet<String> documentIds = new HashSet<>();
        HashMap<Word, Integer> wordMap = new HashMap<>();
        for (String word : words.keySet()) {
            // get all document ids related to the queried words
            Word w = INDEXER.getWord(word);
            if (w != null) {
                wordMap.put(w, words.get(word));
                ArrayList<String> ids = queryInvertedIndex(w);
                documentIds.addAll(ids);
            }
        }
        ResultEntry[] entries = new ResultEntry[documentIds.size()];
        int i = 0;
        long time = System.nanoTime();
        for (String id : documentIds) {
            entries[i] = new ResultEntry(id, words.size());
            queryPageRank(entries[i]);
            queryTfIdf(entries[i], wordMap);
            queryDocumentDetail(entries[i]);
            processScore(entries[i]);
            i++;
        }
        log.warn("number of seconds used in getting page scores " + 1.0 * (System.nanoTime() - time) / 1000000000);
        return entries;
    }

    /**
     * Scoring algorithm. Use page rank, TF, IDF, and so on...
     * @param entry result entry
     */
    private static void processScore(ResultEntry entry) {
        // TODO: TUNE THE ALGORITHM!!!
        entry.score = entry.pageRank * entry.tfidf;
//        for (String url : urls) {
//            if (entry.location.startsWith(url)) {
//                entry.score *= 1.0 * 100 / entry.location.length();
//            }
//        }

    }

    /******************************************
     * Integration with PageRank and Indexing *
     ******************************************/

    /**
     * Query inverted index database to get all documentIds of the documents that contains
     * the word.
     * @param w word
     * @return documentId of the documents that contains the word
     */
    private static ArrayList<String> queryInvertedIndex(Word w) {
        // DONE: query inverted index database
        ArrayList<String> res = new ArrayList<>();
        for (String id : w.getDocs()) {
            if (docs.containsKey(id) && docs.get(id).getTitle().length() > 1) {
                res.add(id);
            }
        }
        return res;
    }

    /**
     * Query inverted index database to get TF/IDF scores.
     * @param words words to be searched
     * @param entry the result entry where the scores will be stored
     */
    private static void queryTfIdf(ResultEntry entry, Map<Word, Integer> words) {
        // DONE: query inverted index database (TF/IDF..)
        entry.tfidf = 0;
        entry.numWordsMatched = 0;
        entry.numWordsTitle = 0;
        for (Word w : words.keySet()) {
            if (w != null && w.inDoc(entry.documentId)) {
                entry.numWordsMatched++;
                List<Integer> posTitle = w.getTitlePos(entry.documentId);
                if (posTitle != null) {
                    entry.numWordsTitle += posTitle.size(); // TODO: USED THIS SOMEWHERE
                }
                entry.tfidf += w.getTf(entry.documentId) * w.getIdf() * w.getIdf() * words.get(w);
//                System.out.println(entry.documentId + " tf score:" + w.getTf(entry.documentId));
//                System.out.println(entry.documentId + " idf score:" + w.getIdf(entry.documentId));
            }
        }
    }

    /**
     * Query page rank database to get page rank score.
     * @param entry the result entry where the score will be stored
     */
    private static void queryPageRank(ResultEntry entry) {
        // TODO: query page rank database
        String documentId = entry.documentId;
        // fake data!!!
        if (pageRank.containsKey(documentId)) {
            entry.pageRank = pageRank.get(documentId);
        } else {
            entry.pageRank = 1.5;
        }
    }

    /**
     * Query database by documentID. Store details about the document in result entry.
     * @param entry result entry that is going to store the details about the document
     */
    private static void queryDocumentDetail(ResultEntry entry) {
        // DONE: query database: get details about a document by documentID
        String[] info = docs.get(entry.documentId).getInfo();
        entry.title = info[1];
        entry.location = info[0];
//        entry.digest = info[2] + " This is a fake digest. The page rank is [" + entry.pageRank + "]. " +
//                "The TF-IDF score is [" + entry.tfidf + "]. " +
//                "The total score is [" + entry.score + "].";
        entry.digest = info[2];
//        if (entry.digest == null || entry.digest.length() < 50) {
//            queryPreview(entry);
//        }
    }

    /**
     * Get a preview of the document.
     * @param entry result entry
     */
    private static void queryPreview(ResultEntry entry) {
        Connection connection = Jsoup.connect(entry.location);
        Document doc = null;
        try {
            doc = connection.get();
            String text = doc.body().text();
            entry.digest = text.substring(0, Math.min(100, text.length())) + "...";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /******************************************
     * Helper functions                       *
     ******************************************/

    /**
     * Sort and cache entries from rankStart-th to rankEnd-th.
     * The rank is 0-indexed.
     * @param query query string
     * @param rankStart starting rank
     * @param rankEnd ending rank
     */
    private static void sortAndCache(String query, int rankStart, int rankEnd) {
        ResultEntry[] entries = cache.get(query)[0];
        ResultEntry higherBound = findKthLargest(entries, rankStart);
        ResultEntry lowerBound = findKthLargest(entries, rankEnd);
        ResultEntry[] toSort = new ResultEntry[(rankEnd - rankStart + 1)];
        int i = 0;
        for (ResultEntry entry : entries) {
            if (entry.compareTo(higherBound) <= 0 && entry.compareTo(lowerBound) >= 0) {
                toSort[i++] = entry;
            }
        }
        Arrays.sort(toSort, (o1, o2) -> o2.compareTo(o1));
        System.arraycopy(toSort, 0, cache.get(query)[1], rankStart, rankEnd - rankStart + 1);
    }

    /**
     * Quick select K.
     * @param entries result entries
     * @param k kth element in a descending order
     * @return index of kth element.
     */
    private static ResultEntry findKthLargest(ResultEntry[] entries, int k) {
        int start = 0, end = entries.length - 1;
        while (start < end) {
            int i = partition(entries, start, end);
            if (i < k) start = i + 1;
            else if (i > k) end = i - 1;
            else return entries[i];
        }
        return entries[start];
    }


    private static int partition(ResultEntry[] entries, int start, int end) {
        ResultEntry pivot = entries[start];
        int i = start, j = end + 1;

        while (true) {
            while (entries[++i].compareTo(pivot) > 0) {
                if (i >= end) break;
            }

            while (entries[--j].compareTo(pivot) < 0) {
                if (j <= start) break;
            }

            if (i >= j) break;

            swap(entries, i, j);
        }

        swap(entries, start, j);

        return j;
    }

    private static void swap(ResultEntry[] entries, int i, int j) {
        ResultEntry tmp = entries[i];
        entries[i] = entries[j];
        entries[j] = tmp;
    }

    /**
     * Return whether entries from rankStart-th to rankEnd-th are already sorted and cached.
     * The rank is 0-indexed.
     * @param query query string
     * @param rankStart starting rank
     * @param rankEnd ending rank
     * @return true if range is sorted and cached
     */
    private static boolean sorted(String query, int rankStart, int rankEnd) {
        ResultEntry[] entries = cache.get(query)[1];
        for (int i = rankStart; i <= rankEnd; i++) {
            if (entries[i] == null) return false;
        }
        return true;
    }

    /**
     * Load page rank.
     * @param filename
     */
    private static void loadPageRank(String filename) {
        String line;
        try {
            log.warn("filename is " + filename);
            new File(filename).createNewFile();
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            while ((line = reader.readLine()) != null) {
                String[] toks = line.split("\t");
                pageRank.put(toks[0], Double.parseDouble(toks[1]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load seed urls
     * @param filename
     */
    private static void loadSeedUrls(String filename) {
        String line;
        try {
            log.warn("filename is " + filename);
            new File(filename).createNewFile();
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            while ((line = reader.readLine()) != null) {
                urls.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
