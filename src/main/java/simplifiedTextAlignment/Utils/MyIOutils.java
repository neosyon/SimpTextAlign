package simplifiedTextAlignment.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.DirectoryScanner;

import simplifiedTextAlignment.Representations.TextAlignment;

public class MyIOutils {

	public static Map<String, double[]> readEmbeddingsFromTxtFile(String inFile) throws IOException {
		Map<String,double[]> w2v = new HashMap<String,double[]>();
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		in.readLine();
		String line;
		while((line=in.readLine())!=null){
			String ar[] = line.split(" ");
			double v[] = new double[ar.length-1];
			for(int i = 1; i < ar.length; i++)
				v[i-1] = Double.parseDouble(ar[i]);
			w2v.put(ar[0], v);
		}
		in.close();
		return w2v;
	}
	

	public static Map<String, double[]> readEmbeddingsFromTxtFileUsingVocab(String inFile, Set<String> vocab) throws IOException {
		Map<String,double[]> w2v = new HashMap<String,double[]>();
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		in.readLine();
		String line;
		while((line=in.readLine())!=null){
			String ar[] = line.split(" ");
			if(vocab.contains(ar[0])){
				double v[] = new double[ar.length-1];
				for(int i = 1; i < ar.length; i++)
					v[i-1] = Double.parseDouble(ar[i]);
				w2v.put(ar[0], v);
			}
		}
		in.close();
		return w2v;
	}

	public static String readTextFile(String inFile) throws IOException {
		StringBuilder builder = new StringBuilder();
		BufferedReader in = null;
		try{
			in = new BufferedReader(new FileReader(inFile));
		}
		catch(FileNotFoundException e){
			return null;
		}
		String line;
		while((line=in.readLine())!=null)
			builder.append(line+"\n");
		in.close();
		return builder.toString();
	}

	public static void saveAlignments(List<TextAlignment> alingments, String outFile) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
		for(TextAlignment alingment : alingments)
			out.write(alingment.toString()+"\n\n");
		out.close();
	}

	public static Set<String> readNewselaEmbeddingVocabulary(String inFolder, String language) throws IOException {
		Set<String> vocab = new HashSet<String>();
		
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(new String[]{"*."+language+".0.txt"});
		scanner.setBasedir(inFolder);
		scanner.setCaseSensitive(false);
		scanner.scan();
		String[] files = scanner.getIncludedFiles();
	
		for(String file : files){
			String text = MyIOutils.readTextFile(inFolder+file);
			vocab.addAll(TextProcessingUtils.getCleanEmbeddingModelTokens(text));
			for (int i = 1; i <= 5; i++) {
				file = file.replace("." + language + ".0.txt","." + language + "." + i + ".txt");
				text = MyIOutils.readTextFile(inFolder+file);
				if (text != null) 		
					vocab.addAll(TextProcessingUtils.getCleanEmbeddingModelTokens(text));
			}
		}
		return vocab;
	}


	public static void displayAlignments(List<TextAlignment> alignments, boolean detailed) {
		System.out.println("Alignments: \n");
		for(TextAlignment alignment : alignments)
			if(detailed)
				System.out.println(alignment.toString());
			else
				System.out.println(alignment.getIndexAlignmentString());
		System.out.println("");
	}

	public static Set<String> readTwoTextPerLineFileEmbeddingVocabulary(String inFile, int fistSentIndex, int secondSentIndex) throws IOException {
		Set<String> vocab = new HashSet<String>();
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		String line;
		while((line=in.readLine())!=null){
			String ar[] = line.split("\t");
			vocab.addAll(TextProcessingUtils.getCleanEmbeddingModelTokens(ar[fistSentIndex]));			
			vocab.addAll(TextProcessingUtils.getCleanEmbeddingModelTokens(ar[secondSentIndex]));	
		}
		in.close();
		return vocab;
	}
}
