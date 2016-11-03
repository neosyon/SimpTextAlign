package simplifiedTextAlignment.Representations;

import java.util.List;
import java.util.Map;

public class Text2abstractRepresentation {

	String text;
	List<Integer> tokenIndices;
	Map<Integer, Double> tokenWeighting;
	double[] wavg = null;
	List<Text2abstractRepresentation> sublevel = null;
	
	public Text2abstractRepresentation(String subtext, List<Integer> cleanTokenIndices) {
		text = subtext;
		tokenIndices = cleanTokenIndices;
	}

	public Text2abstractRepresentation(String subtext, Map<Integer, Double> cleanTokenIndices) {
		text = subtext;
		tokenWeighting = cleanTokenIndices;
	}

	@Override
	public int hashCode() {
		return text.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Text2abstractRepresentation))
			return false;
		if (obj == this)
			return true;

		return text.equals(obj);
	}
	
    public int compareTo(Text2abstractRepresentation o) {
        return text.compareTo(o.text);
    }   

	public String toString() { 
	    return text;
	}

	public String getText() {
		return text;
	}
	
	public void calculateWAVG(EmbeddingModel em) {
		wavg = new double[em.getVectorLength()];
		double v[];
		int n = 0;
		for(int index : tokenIndices){
			if((v=em.get(index))!=null){
				for(int i = 0; i < wavg.length; i++)
					wavg[i] += v[i];
				n++;
			}
		}
		for(int i = 0; i < wavg.length; i++)
			wavg[i] /= n;
	} 

	public double[] getWAVG() {
		return wavg;
	}
	
	public List<Integer> getTokenIndices() {
		return tokenIndices;
	}
	
	public Map<Integer,Double> getTokenWeighting(){
		return tokenWeighting;
	}

	public void setSubLevelRepresentations(List<Text2abstractRepresentation> cleanText) {
		sublevel = cleanText;
	}
	
	public List<Text2abstractRepresentation> getSubLevelRepresentation(){
		return sublevel;
	}
}
