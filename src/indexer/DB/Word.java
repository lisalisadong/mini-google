package indexer.DB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Word {
	
	@PrimaryKey
	public String word;
	private Map<String, Double> tfs; // docID : tf
	private double idf; // docID : idf
	private Map<String, List<Integer>> titlePos; 
	private Map<String, List<Integer>> contentPos;

	public Word() { }
	
	public Word(String word) {
		this.word = word;
		this.tfs = new HashMap<String, Double>();
		this.titlePos = new HashMap<String, List<Integer>>();
		this.contentPos = new HashMap<String, List<Integer>>();
	}

	public boolean inDoc(String id) {
		return tfs.containsKey(id);
	}
	
	public Set<String> getDocs() {
		return tfs.keySet();
	}
	
	public void addTf(String id, double tf) {
		tfs.put(id, tf);
	}
	
	public void addIdf(double idf) {
		this.idf = idf;
	}
	
	public void addTitlePos(String id, List<Integer> pos) {
		titlePos.put(id, pos);
	}
	
	public void addContentPos(String id, List<Integer> pos) {
		contentPos.put(id, pos);
	}
	
	public double getTf(String id) {
		return tfs.get(id);
	}

	public double getIdf() {
		return idf;
	}
	
	public List<Integer> getTitlePos(String id) {
		return titlePos.get(id);
	}
	
	public List<Integer> getContentPos(String id) {
		return contentPos.get(id);
	}
	
}
