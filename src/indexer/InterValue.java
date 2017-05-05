package indexer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.WritableComparable;

public class InterValue implements WritableComparable<InterValue> {

	private String docID;
	private double tf;
	private List<Integer> titlePos;
	private List<Integer> contentPos;
	
	
	public InterValue() { }
	
	public InterValue(String docID, double tf, List<Integer> titlePos, List<Integer> contentPos) { 
		this.docID = docID;
		this.tf = tf;
		this.titlePos = titlePos;
		this.contentPos = contentPos;
	}
	
	public String getDocID() {
		return docID;
	}
	
	public double getTf() {
		return tf;
	}
	
	public List<Integer> getTitlePositions() {
		return titlePos;
	}
	
	public List<Integer> getContentPositions() {
		return contentPos;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.docID = in.readUTF();
		this.tf = in.readDouble();
		int tSize = in.readInt();
		this.titlePos = new ArrayList<Integer>();
		for (int i = 0; i < tSize; i++) {
			titlePos.add(in.readInt());
		}
		int cSize = in.readInt();
		this.contentPos = new ArrayList<Integer>();
		for (int i = 0; i < cSize; i++) {
			contentPos.add(in.readInt());
		}
		
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(docID);
		out.writeDouble(tf);
		int tSize = titlePos.size();
		out.writeInt(tSize);
		for (int i = 0; i < tSize; i++) {
			out.writeInt(titlePos.get(i));
		}
		int cSize = contentPos.size();
		out.writeInt(cSize);
		for (int i = 0; i < cSize; i++) {
			out.writeInt(contentPos.get(i));
		}
	}

	@Override
	public int compareTo(InterValue o) {
		int result = docID.compareTo(o.getDocID());
		if (result == 0) {
			result = (tf < o.getTf() ? -1 : (tf == o.getTf() ? 0 : 1));
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		return docID.hashCode();
	}

}
