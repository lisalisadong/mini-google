package indexer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class IndexerMapWorker {

	public static final String[] STOP_SET_VALUES = new String[] { "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any",
			"are", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can't", "cannot",
			"could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from",
			"further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers",
			"herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's",
			"its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or",
			"other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "she", "she'd", "she'll", "she's", "should", "shouldn't",
			"so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these",
			"they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was",
			"wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "won't", "would", "wouldn't", "you", "you'd",
			"you'll", "you're", "you've", "your", "yours", "yourself", "yourselves" };
	public static final Set<String> STOP_LIST = new HashSet<String>(Arrays.asList(STOP_SET_VALUES));

	private String docID;
	private String contentType;
	private InputStream in;
	private Stemmer stemmer;
	private int docWordsNum;
	private Map<String, Map<String, Integer>> wordDocMap; // word : (docID, count)
	
	public IndexerMapWorker(String docID, InputStream in, String contentType) {
		this.docID = docID;
		this.contentType = contentType;
        this.in = in;
        this.stemmer = new Stemmer();
	}
	
	public void parse() throws IOException {
		String content;
		switch (contentType) {
		case "html":
			Document doc = Jsoup.parse(in, Charset.defaultCharset().name(), "");
			// TODO: might need to seperate title ...
			Elements eles = doc.select("*");
			for (Element ele : eles) {
				parseElement(ele.text());
			}
			break;
		case "pdf":
			PDDocument pdoc = PDDocument.load(in);
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setStartPage(1);
	        stripper.setEndPage(Integer.MAX_VALUE);
	        content = stripper.getText(pdoc);
	        pdoc.close();
	        parseElement(content);
			break;
		// xml or plain/text
		default:
			StringBuilder sb = new StringBuilder();
	        int ch;
	        while ((ch = in.read()) != -1) {
	            sb.append((char) ch);
	        }
	        content = sb.toString();
	        parseElement(content);
			break;
		}
		
	}
	
	public void parseElement(String content) {
		// TODO: extract word and stem
	}

}
