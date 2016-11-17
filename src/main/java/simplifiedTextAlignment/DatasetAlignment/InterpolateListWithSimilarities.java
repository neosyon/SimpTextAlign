package simplifiedTextAlignment.DatasetAlignment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import simplifiedTextAlignment.Utils.DefinedConstants;

public class InterpolateListWithSimilarities {

	public static void main(String args[]) throws IOException{
		//BEGINNING OF CONFIG PARAMETERS
		
		String baseDir = "/home/mfranco/nlp/corpora/";
		String inFile = baseDir+"SimplifiedTextAlignment/WikiSimpleWiki/annotations.txt";
		
		String alignmentLevel = DefinedConstants.SentenceLevel;
		
		int nGramSize1 = 3;
		
		String similarityStrategy1 = DefinedConstants.CNGstrategy;
//		String similarityStrategy1= DefinedConstants.WAVGstrategy;
//		String similarityStrategy1 = DefinedConstants.CWASAstrategy;
		
		int nGramSize2 = 3;
		
//		String similarityStrategy2 = DefinedConstants.CNGstrategy;
		String similarityStrategy2 = DefinedConstants.WAVGstrategy;
//		String similarityStrategy2 = DefinedConstants.CWASAstrategy;
		
//		String interMethod = "A";
		String interMethod = "B";

		String inFile1 = inFile+"_"+alignmentLevel+
				"_"+(similarityStrategy1.equals(DefinedConstants.CNGstrategy) ? similarityStrategy1.replace("N", nGramSize1+"") : similarityStrategy1);
		
		String inFile2 = inFile+"_"+alignmentLevel+
				"_"+(similarityStrategy2.equals(DefinedConstants.CNGstrategy) ? similarityStrategy2.replace("N", nGramSize2+"") : similarityStrategy2);
		
		String outFile = inFile+"_"+alignmentLevel+
				"_"+(similarityStrategy1.equals(DefinedConstants.CNGstrategy) ? similarityStrategy1.replace("N", nGramSize1+"") : similarityStrategy1) + "_inter_"+
				(similarityStrategy2.equals(DefinedConstants.CNGstrategy) ? similarityStrategy2.replace("N", nGramSize2+"") : similarityStrategy2)+"_"+interMethod;
		
		BufferedReader in1 = new BufferedReader(new FileReader(inFile1));
		BufferedReader in2 = new BufferedReader(new FileReader(inFile2));
		BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
		String line1, line2;
		while((line1=in1.readLine()) != null) {
			line2 = in2.readLine();
			String ar1[] = line1.split("\t");
			String ar2[] = line2.split("\t");
			double a = Double.parseDouble(ar1[ar1.length-1]);
			double b = Double.parseDouble(ar2[ar2.length-1]);
			double inter = 0;
			for(int i = 0; i < ar1.length-1; i++)
				out.write(ar1[i]+"\t");
			if(interMethod.equals("A"))
				inter = a*b;
			else if(interMethod.equals("B")){
				inter = Math.pow(a, 2) + (1-a)*b;
			}
			out.write(inter+"\n");
		}
		in1.close();
		in2.close();
		out.close();
	}
	
}
