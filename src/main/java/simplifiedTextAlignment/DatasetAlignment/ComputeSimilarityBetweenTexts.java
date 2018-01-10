package simplifiedTextAlignment.DatasetAlignment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import simplifiedTextAlignment.Representations.ModelContainer;
import simplifiedTextAlignment.Representations.EmbeddingModel;
import simplifiedTextAlignment.Representations.NgramModel;
import simplifiedTextAlignment.Representations.Text2abstractRepresentation;
import simplifiedTextAlignment.Representations.TextAlignment;
import simplifiedTextAlignment.Utils.DefinedConstants;
import simplifiedTextAlignment.Utils.MyIOutils;
import simplifiedTextAlignment.Utils.TextProcessingUtils;
import simplifiedTextAlignment.Utils.VectorUtils;

public class ComputeSimilarityBetweenTexts {

	public static void main(String args[]) throws IOException{
		//BEGINNING OF CONFIG PARAMETERS
		
		String baseDir = "/path/to/your/dataset/parent/folder/";
		String inFile = baseDir+"SimplifiedTextAlignment/WikiSimpleWiki/annotations.txt";

		int firstSentIndex = 1;
		int secondSentIndex = 2;
		
		String language = DefinedConstants.EnglishLanguage;

		String alignmentLevel = DefinedConstants.SentenceLevel;
		
		int nGramSize = 3;
		
//		String similarityStrategy = DefinedConstants.WAVGstrategy;
//		String similarityStrategy = DefinedConstants.CWASAstrategy;
		String similarityStrategy = DefinedConstants.CNGstrategy;
		
		String alignmentStrategy = DefinedConstants.closestSimStrategy;
		
		String outFile = inFile+"_"+alignmentLevel+
				"_"+(similarityStrategy.equals(DefinedConstants.CNGstrategy) ? similarityStrategy.replace("N", nGramSize+"") : similarityStrategy);
		
		String embeddingsFile = null;
		if(language.equals(DefinedConstants.EnglishLanguage))
			embeddingsFile = baseDir+"w2v_collections/Wikipedia/vectors/EN_Wikipedia_w2v_input_format.txtUTF8.vec";
		else if(language.equals(DefinedConstants.SpanishLanguage))
			embeddingsFile = baseDir+"w2v_collections/SBW-vectors-300-min5.txt";
		
		if (args.length > 0) {
			inFile = outFile = null;
			nGramSize = 0;
			firstSentIndex = 0;
			secondSentIndex = 1;
			Map<String, String> param2value = MyIOutils.parseOptions(args);
			if (param2value == null) {
				System.out.println("Error: invalid input options. ");
				MyIOutils.showCustomModelUsageMessage();
				System.exit(1);
			}
			inFile = param2value.get("input");
			outFile = param2value.get("output");
			similarityStrategy = param2value.get("similarity");
			embeddingsFile = param2value.get("emb");
			if (similarityStrategy != null && similarityStrategy.length() == 3 && similarityStrategy.charAt(0) == 'C'	&& similarityStrategy.charAt(2) == 'G') {
				nGramSize = Integer.parseInt(similarityStrategy.charAt(1) + "");
				similarityStrategy = DefinedConstants.CNGstrategy;
			}
		} else {
			System.out.println("Using parameters by default. ");
			MyIOutils.showCustomModelUsageMessage();
		}
		
		//END CONFIG PARAMETERS
		
		boolean isCWASA = false;
		ModelContainer model = null;
		if((isCWASA=similarityStrategy.equals(DefinedConstants.CWASAstrategy)) || similarityStrategy.equals(DefinedConstants.WAVGstrategy)){
			System.out.println("Reading embeddings...");
			Set<String> vocab = MyIOutils.readTwoTextPerLineFileEmbeddingVocabulary(inFile, firstSentIndex, secondSentIndex);
			model = new ModelContainer(new EmbeddingModel(embeddingsFile,vocab));
			if(isCWASA){
				model.em.precomputeW2VcosDist();
				model.em.createSimilarityMatrix();
			}
		}
		else if(similarityStrategy.equals(DefinedConstants.CNGstrategy)){
			System.out.println("Calculating IDF...");
			NgramModel aux;
			model = new ModelContainer(aux = new NgramModel(true, nGramSize));
			aux.buildTwoTextPerLineFileModel(inFile, alignmentLevel, firstSentIndex, secondSentIndex);
		}
		
		System.out.println("Aligning...");
		long ini = System.currentTimeMillis();
		calculateTwoTextPerLineFileSimilarities(inFile,outFile, similarityStrategy, alignmentStrategy, alignmentLevel, model, firstSentIndex, secondSentIndex);
		long end = System.currentTimeMillis();
		System.out.println("Alignment done in " + ((double) ((end-ini)/ 1000) / 60) + " minutes.");
	}

	private static void calculateTwoTextPerLineFileSimilarities(String inFile, String outFile, String similarityStrategy, 
			String alignmentStrategy, String alignmentLevel, ModelContainer model, int firstSentIndex, int secondSentIndex) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
		String line;
		int i = 0;
		while((line=in.readLine())!=null){
			if(i%1000==0)
				System.out.println(i);
			String ar[] = line.split("\t");
			List<Text2abstractRepresentation> cleanSubtexts1 = TextProcessingUtils.getCleanText(ar[firstSentIndex],alignmentLevel, similarityStrategy,model);
			List<Text2abstractRepresentation> cleanSubtexts2 = TextProcessingUtils.getCleanText(ar[secondSentIndex],alignmentLevel, similarityStrategy,model);
			List<TextAlignment> alignments = VectorUtils.alignUsingStrategy(cleanSubtexts1, cleanSubtexts2,similarityStrategy, alignmentStrategy, model);
			if(ar.length == 3 || ar.length == 4) // this is only for the WikiSimpleWiki dataset
				out.write(ar[0] +"\t"+ar[firstSentIndex]+"\t"+ar[secondSentIndex]+"\t"+alignments.get(0).getSimilarity()+"\n");
			else if(ar.length == 2)
				out.write(ar[firstSentIndex]+"\t"+ar[secondSentIndex]+"\t"+alignments.get(0).getSimilarity()+"\n");
			else{
				System.out.println("Error: the format of the input file is the following (use tab separator):\ntext1\ttext2");
				System.exit(1);
			}
			i++;
		}
		in.close();
		out.close();
	}
}
