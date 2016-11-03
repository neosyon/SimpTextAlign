package simplifiedTextAlignment.Representations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.matrix.AbstractMatrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import simplifiedTextAlignment.Utils.VectorUtils;

public class EmbeddingModel {
	Map<String,Integer> w2i =  null;
	List<double[]> i2v = null;
	int vectorLen;
	AbstractMatrix similarityM = null;
	
	public EmbeddingModel(String embeddingsFile, Set<String> vocab) throws IOException{
		i2v = new ArrayList<double[]>();
		w2i = new HashMap<String,Integer>();
		BufferedReader in = new BufferedReader(new FileReader(embeddingsFile));
		in.readLine();
		String line;
		while((line=in.readLine())!=null){
			String ar[] = line.split(" ");
			if(vocab.contains(ar[0])){
				double v[] = new double[ar.length-1];
				for(int i = 1; i < ar.length; i++)
					v[i-1] = Double.parseDouble(ar[i]);
				i2v.add(v);
				w2i.put(ar[0], w2i.size());
			}
		}
		in.close();
		vectorLen = i2v.iterator().next().length;
	}

	public void precomputeW2VcosDist() {
		for(double[] v : i2v)
			VectorUtils.getCosDistPartialResultVector(v);
	}
	
	public int getVectorLength(){
		return vectorLen;
	}

	public double[] get(int index) {
		return i2v.get(index);
	}

	public Integer getIndex(String token) {
		return w2i.get(token);
	}

	public void createSimilarityMatrix() {
		similarityM = new FlexCompRowMatrix(i2v.size(),i2v.size());
	}

	public double getSimilatity(Integer susp, Integer source) {
		double sim = similarityM.get(susp, source);
		if(sim != 0)
			return sim;
		if(susp == source)
			return 1;
		sim=VectorUtils.getCosSimUsingPartialResults(i2v.get(susp),i2v.get(source));
		similarityM.set(susp, source, sim);
		similarityM.set(source, susp, sim);
		return sim;
	}
}
