package searchengine;

import utils.Stemmer;

import java.util.*;

/**
 * Created by QingxiaoDong on 4/29/17.
 */
public class SearchEngineService {

    private static Stemmer stemmer = new Stemmer();

    private static HashMap<String, ResultEntry[]> cache;

    /**
     * Search with a query. Return NUM results starting from RANK-th.
     * For example rank = 1, num = 10 will return first 10 results.
     *
     * @param query search query
     * @param rank rank of the first result to be returned
     * @param num number of results to be returned
     * @return sorted result entries
     */
    public static ResultEntry[] search(String query, int rank, int num) {

        // get {word:occurrences} map from query
        Map<String, Integer> wordsOccur = decomposeQuery(query);

        // get all candidate results
        // TODO: get results; check cache first;
        ResultEntry[] entries;
        if (cache.containsKey(query)) {
            entries = cache.get(query);
        } else {
            entries = getAllEntries(Collections.enumeration(wordsOccur.keySet()));
        }
        // out of bound
        if (rank >= entries.length) {
            return null;
        }

        return null;
    }

    /**
     * Asynchronously search with a query. Return immediately. Must use
     * joinAsyncSearch() to join the thread.
     *
     * @param query search query
     */
    private static void asyncSearch(String query, int rank, int num) {
        // TODO: start new thread to search database and sort
    }

    /**
     * Join the async searching thread.
     *
     * @return sorted result entries
     */
    private static void joinAsyncSearch() {
        // TODO: join the search thread

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
            ArrayList<String> ids = queryInvertedIndex(words.nextElement());
            documentIds.addAll(ids);
        }
        ResultEntry[] entries = new ResultEntry[documentIds.size()];
        int i = 0;
        for (String id : documentIds) {
            queryDocumentDetail(id, entries[i++]);
        }
        return entries;
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
        return null;
    }

    /**
     * Query database by documentID. Store details about the document in result entry.
     * @param documentId document id
     * @param entry result entry that is going to store the details about the document
     */
    private static void queryDocumentDetail(String documentId, ResultEntry entry) {
        // TODO: query database: get details about a document by documentID
    }


    /******************************************
     * Helper functions                       *
     ******************************************/

    /**
     * Quick select K.
     * @param entries result entries
     * @param k kth element in a descending order
     * @return index of kth element.
     */
    private int findKthLargest(ResultEntry[] entries, int k) {
        int lo = 0, hi = entries.length - 1;
        while (lo < hi) {
            int i = partition(entries, lo, hi);
            if (i < k) lo = i + 1;
            else if (i > k) hi = i - 1;
            else return i;
        }
        return lo;
    }


    private int partition(ResultEntry[] entries, int lo, int hi) {
        double pivot = entries[lo].getScore();
        int i = lo, j = hi + 1;

        while (true) {
            while (entries[++i].getScore() > pivot) {
                if (i >= hi) break;
            }

            while (entries[--j].getScore() < pivot) {
                if (j <= lo) break;
            }

            if (i >= j) break;

            swap(entries, i, j);
        }

        swap(entries, lo, j);

        return j;
    }

    private void swap(ResultEntry[] entries, int i, int j) {
        ResultEntry tmp = entries[i];
        entries[i] = entries[j];
        entries[j] = tmp;
    }

    /**
     * Return whether entries[start:end] is already sorted in descending order .
     * @param entries result entries
     * @param start start index
     * @param end end index
     * @return true if range is sorted in descending order.
     */
    private boolean sorted(ResultEntry[] entries, int start, int end) {
        // invalid input
        if (start > end || start < 0) {
            throw new IllegalStateException("start: " + start + "; end: " + end);
        }
        // edge case
        if (start >= entries.length) {
            return true;
        }
        // end is larger than length
        end = Math.min(end, entries.length - 1);

        double prev = entries[start].getScore();
        for (int i = start + 1; i <= end; i++) {
            if (entries[i].getScore() > prev) {
                return false;
            }
        }
        return true;
    }
}
