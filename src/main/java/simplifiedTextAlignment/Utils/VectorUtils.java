package simplifiedTextAlignment.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import simplifiedTextAlignment.Representations.EmbeddingModel;
import simplifiedTextAlignment.Representations.ModelContainer;
import simplifiedTextAlignment.Representations.Text2abstractRepresentation;
import simplifiedTextAlignment.Representations.TextAlignment;

public class VectorUtils {
	

	public static void precomputeW2VcosDist(Map<String, double[]> w2v) {
		for(double[] v : w2v.values())
			getCosDistPartialResultVector(v);
	}
	
	public static void getCosDistPartialResultVector(double[] v) {
        double a = 0;
        for(int i = 0; i < v.length; i++)
            a += Math.pow(v[i], 2);
        if(a > 0){
            a = (double) Math.sqrt(a);
            for (int i = 0; i < v.length; i++)
                v[i] /= a;
        }
    }
	
	public static void getCosDistPartialResultVector(Map<Integer,Double> v) {
        double a = 0;
        for(double value : v.values())
            a += Math.pow(value, 2);
        if(a > 0){
            a = (double) Math.sqrt(a);
            for (int key : v.keySet())
                v.put(key, v.get(key) / a);
        }
    }
    
    public static double getCosSimUsingPartialResults(double v1[], double v2[]) {
        double sim = 0.0;
        for(int i = 0; i < v1.length; i++)
            sim += v1[i]*v2[i];                
        return sim;
	}
    
    public static double getCosSimUsingPartialResults(Map<Integer,Double> v1, Map<Integer,Double> v2) {
        double sim = 0.0;
        Double v2w;
        for(int key : v1.keySet())
        	if((v2w=v2.get(key))!=null)
        		sim += v1.get(key)*v2w;                
        return sim;
	}
    
	public static List<TextAlignment> getSubLevelAlignments(List<TextAlignment> alignments, List<Text2abstractRepresentation> subtexts1,
			List<Text2abstractRepresentation> subtexts2, String similarityStrategy, String alignmentStrategy, ModelContainer model) {
		List<TextAlignment> subLvAlignments = new LinkedList<TextAlignment>();
		int total1 = 0, total2 = 0;
		for(TextAlignment alignment : alignments){
			List<Text2abstractRepresentation> rep1;
			List<Text2abstractRepresentation> rep2;
			List<TextAlignment> subLvAlignmentsAux = VectorUtils.alignUsingStrategy(
					rep1=subtexts1.get(alignment.getTargetIndex()).getSubLevelRepresentation(),
					rep2=subtexts2.get(alignment.getSourceIndex()).getSubLevelRepresentation(),
					similarityStrategy, alignmentStrategy, model);
			for(TextAlignment subLvAlignment : subLvAlignmentsAux){
				subLvAlignment.setSourceIndex(total2+subLvAlignment.getSourceIndex());
				subLvAlignment.setTargetIndex(total1+subLvAlignment.getTargetIndex());
			}
			subLvAlignments.addAll(subLvAlignmentsAux);
			total1+=rep1.size(); 
			total2+=rep2.size();
		}
		return subLvAlignments;
	}

	public static List<TextAlignment> alignUsingStrategy(List<Text2abstractRepresentation> cleanSubtexts1,
			List<Text2abstractRepresentation> cleanSubtexts2, String similarityStrategy, String alignmentStrategy, ModelContainer model) {
		double[][] sims = null;
		if(similarityStrategy.equals(DefinedConstants.WAVGstrategy))
			sims = alignUsingWAVG(cleanSubtexts1,cleanSubtexts2);
		else if(similarityStrategy.equals(DefinedConstants.CWASAstrategy))
			sims = alignUsingCWASA(cleanSubtexts1,cleanSubtexts2, model.em);
		else if(similarityStrategy.equals(DefinedConstants.CNGstrategy))
			sims = alignUsingNgrams(cleanSubtexts1,cleanSubtexts2);
		else{
			System.out.println("Error: similarity strategy not recognized.");
			System.exit(1);
		}
		List<TextAlignment> alignments = null;
		if(alignmentStrategy.equals(DefinedConstants.closestSimStrategy))
			alignments = getAlignmentsUsingClosestCosSim(cleanSubtexts1,cleanSubtexts2, sims);
		else if(alignmentStrategy.equals(DefinedConstants.closestSimKeepingSeqStrategy))
			alignments = getAlignmentsUsingClosestCosSimKeepingSeq(cleanSubtexts1,cleanSubtexts2, sims);
		else{
			System.out.println("Error: alignment strategy not recognized.");
			System.exit(1);
		}
		return alignments;
	}

	//s1 and s2 are the original and the summarized texts, respectively. 
	// In "sims", rows and columns iterate over s1 and s2, respectively. 
	private static List<TextAlignment> getAlignmentsUsingClosestCosSim(List<Text2abstractRepresentation> cleanSubtexts1,
			List<Text2abstractRepresentation> cleanSubtexts2, double[][] sims) {
		List<TextAlignment> alignments = new ArrayList<TextAlignment>();		
		int i = 0;
		for(Text2abstractRepresentation subtext2 : cleanSubtexts2){
			int closestIndex = getIndexOfClosestSample(sims,i);
			alignments.add(new TextAlignment(subtext2.getText(), cleanSubtexts1.get(closestIndex).getText(),sims[closestIndex][i], i, closestIndex));
			i++;
		}
		return alignments;
	}
	
	private static List<TextAlignment> getAlignmentsUsingClosestCosSimKeepingSeq(List<Text2abstractRepresentation> cleanSubtexts1,
			List<Text2abstractRepresentation> cleanSubtexts2, double[][] sims) {
		List<TextAlignment> alignments = getAlignmentsUsingClosestCosSim(cleanSubtexts1,cleanSubtexts2, sims);
		Set<Integer> validIndexes = getLongestIncreassingTargetSequenceIndexes(alignments);
		int prevValid = 0;
		for(int i = 0; i < alignments.size(); i++){
			TextAlignment alignment2fix = alignments.get(i);
			if(!validIndexes.contains(i)){
				int nextValid = cleanSubtexts1.size()-1;
				for(int j = i+1; j < alignments.size(); j++)
					if(validIndexes.contains(j)){
						nextValid = alignments.get(j).getTargetIndex();
						break;
					}
				double bestSim = sims[prevValid][alignment2fix.getSourceIndex()];
				int bestIndex = prevValid;
				for(int j = prevValid+1; j <= nextValid; j++)
					if(sims[j][alignment2fix.getSourceIndex()] > bestSim){
						bestSim = sims[j][alignment2fix.getSourceIndex()];
						bestIndex = j;
					}
				alignment2fix.setTarget(cleanSubtexts1.get(bestIndex).getText(), bestIndex, bestSim);
			}
			prevValid = alignment2fix.getTargetIndex();
		}
		return alignments;
	}
	
	private static Set<Integer>  getLongestIncreassingTargetSequenceIndexes(List<TextAlignment> alignments) {
		int[] lengthLongestIncreassing = new int[alignments.size()];
		int globalMax = -1, globalMaxIndex = -1;
		for (int i = 0; i < alignments.size(); i++) {
			int max = 1;
			for (int j = 0; j < i; j++) 
				if (alignments.get(i).getTargetIndex() > alignments.get(j).getTargetIndex()
						&& (max == 1 || max < lengthLongestIncreassing[j] + 1))
						max = 1 + lengthLongestIncreassing[j];
			lengthLongestIncreassing[i] = max;
			if (globalMax < max) {
				globalMax = lengthLongestIncreassing[i];
				globalMaxIndex = i;
			}
		}
		Set<Integer> pos = new HashSet<Integer>();
		pos.add(globalMaxIndex);
		int currentMax = globalMax-1, lastAdded = -1;
		for (int i = globalMaxIndex-1; i >= 0; i--) {
			int target = alignments.get(i).getTargetIndex();
			if(lengthLongestIncreassing[i]==currentMax){
				pos.add(i);
				lastAdded = target;
				currentMax--;
			}		
			else if(target == lastAdded)
				pos.add(i);
		}
		return pos;
}

	private static int getIndexOfClosestSample(double[][] sims, int i) {
		double closest = -1;
		int index = -1;
		for(int j = 0; j < sims.length; j++)
			if(sims[j][i] > closest){
				closest = sims[j][i];
				index = j;
			}
		return index;
	}
	
	private static double[][] alignUsingNgrams(List<Text2abstractRepresentation> subtexts1,
			List<Text2abstractRepresentation> subtexts2) {
		double[][] sims = new double[subtexts1.size()][subtexts2.size()];
		
		int i = 0;
		for(Text2abstractRepresentation subtext1 : subtexts1){
			Map<Integer,Double> weighting1 = subtext1.getTokenWeighting();
			int j = 0;
			for(Text2abstractRepresentation subtext2 : subtexts2){
				sims[i][j] = getCosSimUsingPartialResults(weighting1,subtext2.getTokenWeighting());
				if(Double.isNaN(sims[i][j]))
					sims[i][j] = 0;
				j++;
			}
			i++;
		}

		return sims;
	}

	private static double[][] alignUsingCWASA(List<Text2abstractRepresentation> subtexts1,
			List<Text2abstractRepresentation> subtexts2, EmbeddingModel em) {
		double[][] sims = new double[subtexts1.size()][subtexts2.size()];
		
		int i = 0;
		for(Text2abstractRepresentation subtext1 : subtexts1){
			List<Integer> indices1 = subtext1.getTokenIndices();
			int j = 0;
			for(Text2abstractRepresentation subtext2 : subtexts2){
				sims[i][j] = getCWASAsimilarity(indices1,subtext2.getTokenIndices(), em);
				if(Double.isNaN(sims[i][j]))
					sims[i][j] = 0;
				j++;
			}
			i++;
		}

		return sims;
	}
	
	private static Double getCWASAsimilarity(List<Integer> words1, List<Integer> words2, EmbeddingModel em) {
		if(words1.size() == 0 || words2.size() == 0)
			return 0.0;
		double[] sourceSims = new double[words2.size()];
		boolean[] isNotDoubleDirAligned = new boolean[words2.size()];
		for(int i = 0; i < sourceSims.length; i++)
			sourceSims[i] = -1;
		Double total = 0.0, sim;
		for(Integer susp : words1){
			double max = -1;
			int i = 0;
			for(Integer source : words2){
				sim=em.getSimilatity(susp,source);
				if(sim > max){
					max = sim;	
					if(sim > sourceSims[i]){
						sourceSims[i] = sim;
						isNotDoubleDirAligned[i] = false;
					}
				}
				else if(sim > sourceSims[i]){
					sourceSims[i] = sim;
					isNotDoubleDirAligned[i] = true;
				}
				i++;
			}
			total += max;
		}
		int n = words1.size();
		for(int i = 0; i < sourceSims.length; i++){
			if(isNotDoubleDirAligned[i]){
				total += sourceSims[i];
				n++;
			}
		}
		if(n > 0)
			return total / n;
		return total;
	}

	private static double[][] alignUsingWAVG(List<Text2abstractRepresentation> subtexts1,
			List<Text2abstractRepresentation> subtexts2) {
		double[][] sims = new double[subtexts1.size()][subtexts2.size()];
		
		int i = 0;
		for(Text2abstractRepresentation subtext1 : subtexts1){
			double[] v1 = subtext1.getWAVG();
			int j = 0;
			for(Text2abstractRepresentation subtext2 : subtexts2){
				sims[i][j] = getCosSimUsingPartialResults(v1,subtext2.getWAVG());
				if(Double.isNaN(sims[i][j]))
					sims[i][j] = 0;
				j++;
			}
			i++;
		}

		return sims;
	}
	
	public static void calculateWAVGs(List<Text2abstractRepresentation> cleanSubtexts, EmbeddingModel em) {
		for(Text2abstractRepresentation subtext : cleanSubtexts){
			subtext.calculateWAVG(em);
			getCosDistPartialResultVector(subtext.getWAVG());
		}
	}

	public static Set<String> getValidEmbeddingTokens(Set<String> vocab, Map<String, double[]> w2v) {
		Set<String> valid = new HashSet<String>();
		for(String token : vocab)
			if(w2v.containsKey(token))
				valid.add(token);
		return valid;
	}
}
