package simplifiedTextAlignment.DatasetAlignment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
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

public class AlignWikiSimpleWiki {

	public static void main(String args[]) throws IOException{
		//BEGINNING OF CONFIG PARAMETERS
		
		String baseDir = "/home/mfranco/nlp/corpora/";
		String inFile = baseDir+"SimplifiedTextAlignment/WikiSimpleWiki/annotations.txt";

		String language = DefinedConstants.EnglishLanguage;

		String alignmentLevel = DefinedConstants.SentenceLevel;
		
		int nGramSize = 3;
		
//		String similarityStrategy = DefinedConstants.WAVGstrategy;
		String similarityStrategy = DefinedConstants.CWASAstrategy;
//		String similarityStrategy = DefinedConstants.CNGstrategy;
		
		String alignmentStrategy = DefinedConstants.closestSimStrategy;
		
		String outFile = inFile+"_"+alignmentLevel+
				"_"+(similarityStrategy.equals(DefinedConstants.CNGstrategy) ? similarityStrategy.replace("N", nGramSize+"") : similarityStrategy);
		String embeddingsFile = null;
		
		if(language.equals(DefinedConstants.EnglishLanguage))
			embeddingsFile = baseDir+"w2v_collections/Wikipedia/vectors/EN_Wikipedia_w2v_input_format.txtUTF8.vec";
		else if(language.equals(DefinedConstants.SpanishLanguage))
			embeddingsFile = baseDir+"w2v_collections/SBW-vectors-300-min5.txt";
		
		//END CONFIG PARAMETERS
		
		boolean isCWASA = false;
		ModelContainer model = null;
		if((isCWASA=similarityStrategy.equals(DefinedConstants.CWASAstrategy)) || similarityStrategy.equals(DefinedConstants.WAVGstrategy)){
			System.out.println("Reading embeddings...");
			Set<String> vocab = MyIOutils.readWikiSimpleWikiEmbeddingVocabulary(inFile);
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
			aux.buildWikiSimpleWikiModel(inFile,language, alignmentLevel);
		}
		
		System.out.println("Aligning...");
		long ini = System.currentTimeMillis();
		calculateWikiSimpleWikiSimilarities(inFile,language,outFile, similarityStrategy, alignmentStrategy, alignmentLevel, model);
		long end = System.currentTimeMillis();
		System.out.println("Alignment done in " + ((double) ((end-ini)/ 1000) / 60) + " minutes.");
	}

	private static void calculateWikiSimpleWikiSimilarities(String inFile, String language, String outFile, String similarityStrategy, 
			String alignmentStrategy, String alignmentLevel, ModelContainer model) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(inFile));
		BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
		String line;
		int i = 0;
		while((line=in.readLine())!=null){
			if(i%1000==0)
				System.out.println(i);
			String ar[] = line.split("\t");
			List<Text2abstractRepresentation> cleanSubtexts1 = TextProcessingUtils.getCleanText(ar[1],alignmentLevel, similarityStrategy,model);
			List<Text2abstractRepresentation> cleanSubtexts2 = TextProcessingUtils.getCleanText(ar[2],alignmentLevel, similarityStrategy,model);
			List<TextAlignment> alignments = VectorUtils.alignUsingStrategy(cleanSubtexts1, cleanSubtexts2,similarityStrategy, alignmentStrategy, model);
			out.write(ar[0] +"\t"+ar[1]+"\t"+ar[2]+"\t"+alignments.get(0).getSimilarity()+"\n");
			i++;
		}
		in.close();
		out.close();
	}
}
