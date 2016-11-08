package simplifiedTextAlignment.Representations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.DirectoryScanner;

import simplifiedTextAlignment.Utils.DefinedConstants;
import simplifiedTextAlignment.Utils.MyIOutils;
import simplifiedTextAlignment.Utils.TextProcessingUtils;
import simplifiedTextAlignment.Utils.VectorUtils;

public class NgramModel {

	Map<String,Integer> n2i;
	List<Double> i2IDF;
	int nSize;
	boolean isCNGmodel;
	double nDocs;
	
	public NgramModel(boolean isCNG, int n) throws IOException {
		nSize = n;
		isCNGmodel = isCNG;
		n2i = new HashMap<String,Integer>();
		i2IDF = new ArrayList<Double>();
		nDocs = 0;
	}
	
	public void buildNewselaNgramModel(String inFolder, String language, String alignmentLevel) throws IOException {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(new String[]{"*."+language+".0.txt"});
		scanner.setBasedir(inFolder);
		scanner.setCaseSensitive(false);
		scanner.scan();
		String[] files = scanner.getIncludedFiles();
	
		//CARE, THIS LOOP IS DEPENDENT OF THE NEWSELA DATASET FORMAT
		for(String file : files){
			String text = MyIOutils.readTextFile(inFolder+file);
			processAndCountTextNgrams(text,alignmentLevel);
			for (int i = 1; i <= 5; i++) {
				file = file.replace("." + language + ".0.txt","." + language + "." + i + ".txt");
				text = MyIOutils.readTextFile(inFolder+file);
				if (text != null)
					processAndCountTextNgrams(text,alignmentLevel);
			}
		}	
		calculateIDF();
	}
	

	public void buildWikiSimpleWikiModel(String inFile, String language, String alignmentLevel) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		String line;
		while((line=in.readLine())!=null){
			String ar[] = line.split("\t");
			processAndCountTextNgrams(ar[1],alignmentLevel);
			processAndCountTextNgrams(ar[2],alignmentLevel);
		}
		in.close();
		calculateIDF();		
	}

	private void processAndCountTextNgrams(String text, String alignmentLevel) {
		List<String> subtexts = null;
		if(!alignmentLevel.equals(DefinedConstants.ParagraphSepEmptyLineAndSentenceLevel))
			subtexts = TextProcessingUtils.getSubtexts(text,alignmentLevel);
		else{
			//if it is done at sentence and paragraph level, since this is to calculate the IDF, we can concatenate both levels. This is done because the sentence splitter may introduce some new ngrams, as a result of the tokenization
			subtexts = new LinkedList<String>();
			List<String> subtexts1 = TextProcessingUtils.getSubtexts(text,DefinedConstants.ParagraphSepEmptyLineLevel);
			for(String subtext1 : subtexts1){
				List<String> subtexts2 = TextProcessingUtils.getSubtexts(subtext1,DefinedConstants.SentenceLevel);
				StringBuilder builder = new StringBuilder();
				for(String subtext2 : subtexts2)
					builder.append(subtext2+" ");
				subtexts.add(subtext1 + " " + builder.toString());
			}
		}	
		countSubtextsIDFngrams(subtexts);
	}

	private void calculateIDF() {
		int i = 0;
		for(double freq : i2IDF){
			i2IDF.set(i, Math.log(1+(nDocs/freq)));
			i++;
		}
	}

	private void countSubtextsIDFngrams(List<String> subtexts) {
		for(String text : subtexts)
			countTextIDFngrams(text);
	}

	private void countTextIDFngrams(String text) {
		if(isCNGmodel){
			countTextNonRepCharNgrams(text);
		}
		else{
			// TO IMPLEMENT AT WORD LEVEL
		}
	}

	private void countTextNonRepCharNgrams(String text) {
		char[] cng = new char[nSize];
		String str;
		Integer index = 0;
		Set<String> seen = new HashSet<String>();
		for(int i = 0; i < text.length()-nSize+1; i++){
			for(int j = 0; j < nSize; j++)
				cng[j] = text.charAt(i+j);
			str = new String(cng);
			if (!seen.contains(str)) {
				if ((index = n2i.get(str)) != null)
					i2IDF.set(index, i2IDF.get(index) + 1);
				else {
					i2IDF.add(1.0);
					n2i.put(str, n2i.size());
				}
				seen.add(str);
			}
		}
		nDocs++;
	}

	public Map<Integer,Double> getCharNgramTFIDFmap(String text) {
		Map<Integer,Double> tfidf = new HashMap<Integer,Double>();
		char[] cng = new char[nSize];
		String str;
		Integer index = null;
		for(int i = 0; i < text.length()-nSize+1; i++){
			for(int j = 0; j < nSize; j++)
				cng[j] = text.charAt(i+j);
			str = new String(cng);
			if((index=n2i.get(str))!=null)
				tfidf.put(index,tfidf.getOrDefault(index, 0.0)+1);
		}
		for(Integer id : tfidf.keySet())
			tfidf.put(id, (1+Math.log(tfidf.get(id)))*i2IDF.get(id));
		VectorUtils.getCosDistPartialResultVector(tfidf);
		return tfidf;
	}
}
