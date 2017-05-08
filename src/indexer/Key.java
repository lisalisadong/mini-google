package indexer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.io.WritableComparable;

public class Key implements WritableComparable<Key>{
	
	public String docID;

	@Override
	public void readFields(DataInput in) throws IOException {
		this.docID = in.readLine();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		Text.writeString(out, docID);
	}

	@Override
	public int compareTo(Key o) {
		return docID.compareTo(o.docID);
	}
	
	@Override
    public int hashCode() {
        return docID.hashCode();
    }
	
	@Override
    public boolean equals(Object obj) {
        if (obj instanceof Key)
            return this.compareTo((Key) obj) == 0;
        return false;
    }

}
