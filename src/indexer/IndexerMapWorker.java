package indexer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import utils.Stemmer;

public class IndexerMapWorker {

	public static final String[] STOP_SET_VALUES = new String[] { "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any",
			"are", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can't", "cannot",
			"could", "couldn't", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from",
			"further", "had", "hadn't", "has", "have","having", "he", "her", "here", "hers",
			"herself", "him", "himself", "his", "how", "how's", "i", "if", "in", "into", "is", "isn't", "it", "it's",
			"its", "itself", "let's", "me", "more", "most", "mustn", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or",
			"other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "she", "should", "shouldn't", "without",
			"so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these",
			"they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was",
			"wasn't", "we", "were", "will", "what", "when", "won't", "would", "wouldn't", "you", "v", "via", "w", "which", "who", "why", "with",
			"you'll", "you're", "you've", "your", "yours", "yourself", "yourselves", "ll", "s", "m", "ve", "d", "t", "us" };
	public static final Set<String> STOP_LIST = new HashSet<String>(Arrays.asList(STOP_SET_VALUES));

	private String contentType;
	private InputStream in;
	private Stemmer stemmer;
	private Map<String, Integer> wordFreq; // word : # of the word
	private Map<String, List<Integer>> titlePos; // word : List<word position>
	private Map<String, List<Integer>> contentPos;
	private int position;
	
	public IndexerMapWorker(InputStream in, String contentType) {
		this.contentType = contentType;
        this.in = in;
        this.stemmer = new Stemmer();
        this.wordFreq = new HashMap<String, Integer>();
        this.titlePos = new HashMap<String, List<Integer>>();
        this.contentPos = new HashMap<String, List<Integer>>();
        this.position = 0;
	}
	
	public void parse() throws IOException {
		String content;
		switch (contentType) {
		case "text/html":
			try {
				Document doc = Jsoup.parse(in, Charset.defaultCharset().name(), "");
				parseElement(doc.title(), true);
				parseElement(doc.body().text(), false);
			} catch (Exception e) {
				
			}
			break;
		case "pdf":
			PDDocument pdoc = PDDocument.load(in);
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setStartPage(1);
	        stripper.setEndPage(Integer.MAX_VALUE);
	        content = stripper.getText(pdoc);
	        pdoc.close();
	        parseElement(content, false);
			break;
		// xml or plain/text
		default:
			StringBuilder sb = new StringBuilder();
	        int ch;
	        while ((ch = in.read()) != -1) {
	            sb.append((char) ch);
	        }
	        content = sb.toString();
	        parseElement(content, false);
			break;
		}
		
	}
	
	public void parseElement(String content, boolean isTitle) {
		WordTokenizer tokenizer = new WordTokenizer(content);
		while (tokenizer.hasMoreTokens()) {
			String word = tokenizer.nextToken().toLowerCase();
			if (STOP_LIST.contains(word)) {
				continue;
			}
			word = lemmatize(word);
			position++;
			
			//update wordFreq
			if(wordFreq.containsKey(word)){
				wordFreq.put(word, wordFreq.get(word) + 1);
			}
			else{
				wordFreq.put(word, 1);
			}
			
			//update word positions
			if (isTitle) {
				updatePos(word, titlePos);
			} else {
				updatePos(word, contentPos);
			}
		}
	}
	
	private void updatePos(String word, Map<String, List<Integer>> wordPos) {
		List<Integer> pos = wordPos.get(word);
		if (pos == null) {
			pos = new ArrayList<Integer>();
			wordPos.put(word, pos);
		}
		pos.add(position);
	}
	
	public Map<String, Integer> getWordFreq() {
		return wordFreq;
	}
	
	public Map<String, List<Integer>> getTitlePos() {
		return titlePos;
	}
	
	public Map<String, List<Integer>> getContentPos() {
		return contentPos;
	}
	
	private String lemmatize(String word) {
		return stemmer.stem(word);
	}
	
//	public static void main(String[] args) throws IOException {
//		InputStream is = new FileInputStream("/Users/liujue/Desktop/input/text.txt");
//		IndexerMapWorker w = new IndexerMapWorker(is, "html");
//		w.parse();
//		Map<String, Integer> map = w.getWordFreq();
//		Map<String, List<Integer>> tp = w.getTitlePos();
//		Map<String, List<Integer>> cp = w.getContentPos();
//		for (String key : map.keySet()) {
//		    System.out.println(key + ": " + map.get(key));
//		    System.out.println(tp.get(key));
//		    System.out.println(cp.get(key));
//		}
//	}

}
