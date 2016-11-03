package simplifiedTextAlignment.Representations;

public class TextAlignment {

	String source;
	String target;
	double similarity;
	String id;
	int index1;
	int index2;
	
	public TextAlignment(String text1, String text2, double sim, int i1, int i2) {
		source = text1;
		target = text2;
		similarity = sim;
		index1 = i1;
		index2 = i2;
		id = toString();
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TextAlignment))
			return false;
		if (obj == this)
			return true;

		return id.equals(obj);
	}
	
    public int compareTo(TextAlignment o) {
        return id.compareTo(o.id);
    }   

	public String toString() { 
	    return index1+":\t"+source + "\t---("+similarity+")--->\t"+index2+":\t"+target;
	}

	public String getSource() {
		return source;
	}
	
	public String getTarget() {
		return target;
	}
	
	public double getSimilarity() {
		return similarity;
	}

	public String getIndexAlignmentString() {
		return index1 + " --> " + index2 + " ("+similarity+")";
	}
	
	public int getSourceIndex() {
		return index1;
	}
	
	public int getTargetIndex() {
		return index2;
	}

	public void setTarget(String text, int newIndex, double newSim) {
		target = text;
		index2 = newIndex;
		similarity = newSim;
		id = toString();
	}

	public void setTargetIndex(int newIndex) {
		index2 = newIndex;
		
	}
	
	public void setSourceIndex(int newIndex) {
		index1 = newIndex;
		
	}
	
	public void recalcID(){
		id = toString();
	}
}
