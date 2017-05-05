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
	private List<Integer> positions;
	
	public InterValue() { }
	
	public InterValue(String docID, double tf, List<Integer> pos) { 
		this.docID = docID;
		this.tf = tf;
		this.positions = pos;
	}
	
	public String getDocID() {
		return docID;
	}
	
	public double getTf() {
		return tf;
	}
	
	public List<Integer> getPositions() {
		return positions;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.docID = in.readUTF();
		this.tf = in.readDouble();
		int size = in.readInt();
		this.positions = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			positions.add(in.readInt());
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(docID);
		out.writeDouble(tf);
		int size = positions.size();
		out.writeInt(size);
		for (int i = 0; i < size; i++) {
			out.writeInt(positions.get(i));
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
