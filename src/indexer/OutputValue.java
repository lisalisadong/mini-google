package indexer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.WritableComparable;

public class OutputValue implements WritableComparable<OutputValue> {

	private String docID;
	private double tf;
	private double idf;
	private List<Integer> titlePositions;
	private List<Integer> contentPositions;
	
	public OutputValue() { }
	
	public OutputValue(String docID, double tf, double idf, List<Integer> tPos, List<Integer> cPos) {
		this.docID = docID;
		this.tf = tf;
		this.idf = idf;
		this.titlePositions = tPos;
		this.contentPositions = cPos;
	}
	
	public String getDocID() {
		return docID;
	}
	
	public double getTf() {
		return tf;
	}
	
	public List<Integer> getTitlePositions() {
		return titlePositions;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.docID = in.readUTF();
		this.tf = in.readDouble();
		this.idf = in.readDouble();
		int tSize = in.readInt();
		this.titlePositions = new ArrayList<Integer>();
		for (int i = 0; i < tSize; i++) {
			titlePositions.add(in.readInt());
		}
		int cSize = in.readInt();
		this.contentPositions = new ArrayList<Integer>();
		for (int i = 0; i < cSize; i++) {
			contentPositions.add(in.readInt());
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(docID);
		out.writeDouble(tf);
		out.writeDouble(idf);
		int tSize = titlePositions.size();
		out.writeInt(tSize);
		for (int i = 0; i < tSize; i++) {
			out.writeInt(titlePositions.get(i));
		}
		int cSize = contentPositions.size();
		out.writeInt(cSize);
		for (int i = 0; i < cSize; i++) {
			out.writeInt(contentPositions.get(i));
		}
	}

	@Override
	public int hashCode() {
		return docID.hashCode();
	}

	@Override
	public int compareTo(OutputValue o) {
		int result = docID.compareTo(o.getDocID());
		if (result == 0) {
			result = (tf < o.getTf() ? -1 : (tf == o.getTf() ? 0 : 1));
		}
		return result;
	}
	
	@Override
	public String toString() {
		return docID + "\t" + tf + "\t" + idf + "\t" + titlePositions.toString() + "\t" + contentPositions.toString();
	}

}
