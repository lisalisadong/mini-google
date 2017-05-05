package searchengine;

import utils.Stemmer;

import java.util.*;

/**
 * Created by QingxiaoDong on 4/29/17.
 */
public class SearchEngineService {

    private static Stemmer stemmer = new Stemmer();

    private static LRUCache cache = new LRUCache(100); // LRU with 100 cached queries

    /**
     * Returns the number of results that are expected.
     * @param query query string
     * @return total number of results that are expected.
     */
    public static int preSearch(String query) {
        // get {word:occurrences} map from query
        Map<String, Integer> wordsOccur = decomposeQuery(query);

        if (cache.getOriginal(query) != null) {
            return cache.getOriginal(query).length;
        } else {
            ResultEntry[] entries = getAllEntries(Collections.enumeration(wordsOccur.keySet()));
            cache.put(query, entries);
            return entries.length;
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
        if (cache.getOriginal(query) == null) {
            ResultEntry[] entries = getAllEntries(Collections.enumeration(wordsOccur.keySet()));
            cache.put(query, entries);
        }

        // out of bound, should not happen
        if (rank >= cache.getOriginal(query).length) {
            return null;
        }
        num = Math.min(num, cache.getOriginal(query).length - rank);

        // cache sorted result
        if (!sorted(query, rank, rank + num - 1)) {
            sortAndCache(query, rank, rank + num - 1);
        }

        // get result
        ResultEntry[] resultEntries = new ResultEntry[num];
        System.arraycopy(cache.getSorted(query), rank, resultEntries, 0, num);
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
    private static ResultEntry[] getAllEntries(Enumeration<String> words) {
        HashSet<String> documentIds = new HashSet<>();
        while (words.hasMoreElements()) {
            // get all document ids related to the queried words
            ArrayList<String> ids = queryInvertedIndex(words.nextElement());
            documentIds.addAll(ids);
        }
        ResultEntry[] entries = new ResultEntry[documentIds.size()];
        int i = 0;
        for (String id : documentIds) {
            entries[i] = new ResultEntry(id);
            queryPageRank(entries[i]);
            queryTfIdf(entries[i], words);
            processScore(entries[i]);
            queryDocumentDetail(entries[i]);
            i++;
        }
        return entries;
    }

    /**
     * Scoring algorithm. Use page rank, TF, IDF, and so on...
     * @param entry result entry
     */
    private static void processScore(ResultEntry entry) {
        // TODO: TUNE THE ALGORITHM!!!
        entry.score = entry.pageRank * entry.tf * entry.idf;
    }

    /******************************************
     * Integration with PageRank and Indexing *
     ******************************************/

    /**
     * Query inverted index database to get all documentIds of the documents that contains
     * the word.
     * @param word word
     * @return documentId of the documents that contains the word
     */
    private static ArrayList<String> queryInvertedIndex(String word) {
        // TODO: query inverted index database
        // fake data!!!
        Random rand = new Random();
        ArrayList<String> res = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            res.add(String.valueOf(rand.nextInt(1000000)));
        }
        return res;
    }

    /**
     * Query inverted index database to get TF/IDF scores.
     * @param words words to be searched
     * @param entry the result entry where the scores will be stored
     */
    private static void queryTfIdf(ResultEntry entry, Enumeration<String> words) {
        // TODO: query inverted index database (TF/IDF..)
        // fake data!!!
        entry.tf = 1.0 * new Random().nextInt(2000) / 1000;
        entry.idf = 1.0 * new Random().nextInt(2000) / 1000;
    }

    /**
     * Query page rank database to get page rank score.
     * @param entry the result entry where the score will be stored
     */
    private static void queryPageRank(ResultEntry entry) {
        // TODO: query page rank database
        String documentId = entry.documentId;
        // fake data!!!
        entry.pageRank = 1.0 * new Random().nextInt(10000) / 1000;
    }

    /**
     * Query database by documentID. Store details about the document in result entry.
     * @param entry result entry that is going to store the details about the document
     */
    private static void queryDocumentDetail(ResultEntry entry) {
        // TODO: query database: get details about a document by documentID
        // fake data!!!
        entry.title = "This is a fake title";
        entry.location = "https://this/is/a/fake/location";
        entry.digest = "This is a fake digest. The page rank is [" + entry.pageRank + "]. " +
                "The TF score is [" + entry.tf + "]. " + "The IDF score is [" + entry.idf + "]. " +
                "The total score is [" + entry.tf + "].";
        entry.lastModified = "Apr 1, 2017";
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
        ResultEntry[] entries = cache.getOriginal(query);
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
        System.arraycopy(toSort, 0, cache.getSorted(query), rankStart, rankEnd - rankStart + 1);
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
        ResultEntry[] entries = cache.getSorted(query);
        for (int i = rankStart; i <= rankEnd; i++) {
            if (entries[i] == null) return false;
        }
        return true;
    }
}
