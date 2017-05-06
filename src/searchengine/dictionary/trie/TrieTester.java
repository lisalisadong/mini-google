package searchengine.dictionary.trie;

import java.util.ArrayList;

/**
 * The tester class for Trie tree.
 * @author Qingxiao Dong
 */
public class TrieTester {

    /**
     * The main class.
     * @param args runtime arguments
     */
    public static void main(String[] args) {
        Trie lexicon = new Trie();

        /** tests insert */
        lexicon.insert("brake");
        lexicon.insert("service");

        /** tests search */
        System.out.println(lexicon.search("brake"));
        System.out.println(lexicon.search("bra"));
        System.out.println(lexicon.search("serve"));

        /** tests startsWith */
        System.out.println(lexicon.startsWith("bra"));
        System.out.println(lexicon.startsWith("sea"));

        /** tests getWordsStartsWith */
        lexicon.insert("bra");
        lexicon.insert("brave");
        System.out.println(lexicon.search("bra"));
        ArrayList<String> words = lexicon.getWordsStartsWith("bra", 10);
        for (String w : words) {
            System.out.println(w);
        }
    }
}