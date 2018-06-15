package simplifiedTextAlignment.DatasetAlignment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.DirectoryScanner;

import simplifiedTextAlignment.Representations.ModelContainer;
import simplifiedTextAlignment.Representations.EmbeddingModel;
import simplifiedTextAlignment.Representations.NgramModel;
import simplifiedTextAlignment.Representations.Text2abstractRepresentation;
import simplifiedTextAlignment.Representations.TextAlignment;
import simplifiedTextAlignment.Utils.DefinedConstants;
import simplifiedTextAlignment.Utils.MyIOutils;
import simplifiedTextAlignment.Utils.TextProcessingUtils;
import simplifiedTextAlignment.Utils.VectorUtils;

public class AlignNewselaDataset {

	public static void main(String args[]) throws IOException{
		//BEGINNING OF CONFIG PARAMETERS
		
		String baseDir = "/path/to/your/newsela/parent/folder/";

//		String inFolder = baseDir+"newsela/sample/";
		String inFolder = baseDir+"SimplifiedTextAlignment/newsela_article_corpus_2016-01-29/articles/";
//		String inFolder = baseDir+"SimplifiedTextAlignment/newsela_article_corpus_2016-01-29/testArticles/";

		String language = DefinedConstants.EnglishLanguage;
//		String language = DefinedConstants.SpanishLanguage;

//		String alignmentLevel = DefinedConstants.ParagraphSepEmptyLineLevel;
		String alignmentLevel = DefinedConstants.SentenceLevel;
//		String alignmentLevel = DefinedConstants.ParagraphSepEmptyLineAndSentenceLevel;
		
		int nGramSize = 3;
		
//		String similarityStrategy = DefinedConstants.WAVGstrategy;
//		String similarityStrategy = DefinedConstants.CWASAstrategy;
		String similarityStrategy = DefinedConstants.CNGstrategy;

		String alignmentStrategy = DefinedConstants.closestSimStrategy;
//		String alignmentStrategy = DefinedConstants.closestSimKeepingSeqStrategy;

		String subLvAlignmentStrategy = DefinedConstants.closestSimStrategy;
//		String subLvAlignmentStrategy = DefinedConstants.closestSimKeepingSeqStrategy;
		
//		String outFolder = baseDir+"newsela/output/"+language+"/"+alignmentLevel+
		String outFolder = baseDir+"newsela_article_corpus_2016-01-29/output/"+language+"/"+alignmentLevel+
				"_"+(!alignmentLevel.equals(DefinedConstants.ParagraphSepEmptyLineAndSentenceLevel) ? alignmentStrategy : alignmentStrategy+"_"+subLvAlignmentStrategy)
				+"_"+(similarityStrategy.equals(DefinedConstants.CNGstrategy) ? similarityStrategy.replace("N", nGramSize+"") : similarityStrategy)+"/";
		String embeddingsFile = null;
		
		if(language.equals(DefinedConstants.EnglishLanguage))
			embeddingsFile = baseDir+"w2v_collections/Wikipedia/vectors/EN_Wikipedia_w2v_input_format.txtUTF8.vec";
		else if(language.equals(DefinedConstants.SpanishLanguage))
			embeddingsFile = baseDir+"w2v_collections/SBW-vectors-300-min5.txt";
		
		if (args.length > 0) {
			inFolder = outFolder = null;
			nGramSize = 0;
			Map<String, String> param2value = MyIOutils.parseOptions(args);
			if (param2value == null) {
				System.out.println("Error: invalid input options. ");
				MyIOutils.showNewselaUsageMessage();
				System.exit(1);
			}
			inFolder = param2value.get("input");
			outFolder = param2value.get("output");
			language = param2value.get("language");
			alignmentLevel = param2value.get("aLv");
			similarityStrategy = param2value.get("similarity");
			alignmentStrategy = param2value.get("aSt");
			subLvAlignmentStrategy = param2value.get("aSt2");
			embeddingsFile = param2value.get("emb");
			if (similarityStrategy != null && similarityStrategy.length() == 3 && similarityStrategy.charAt(0) == 'C'	&& similarityStrategy.charAt(2) == 'G') {
				nGramSize = Integer.parseInt(similarityStrategy.charAt(1) + "");
				similarityStrategy = DefinedConstants.CNGstrategy;
			}
		} else {
			System.out.println("Using parameters by default. ");
			MyIOutils.showNewselaUsageMessage();
		}
		//END CONFIG PARAMETERS
		
		boolean isCWASA = false;
		ModelContainer model = null;
		if((isCWASA=similarityStrategy.equals(DefinedConstants.CWASAstrategy)) || similarityStrategy.equals(DefinedConstants.WAVGstrategy)){
			System.out.println("Reading embeddings...");
			Set<String> vocab = MyIOutils.readNewselaEmbeddingVocabulary(inFolder,language);
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
			aux.buildNewselaNgramModel(inFolder,language, alignmentLevel);
		}
		
		// create output folder if it does not exists
		boolean success = (new File(outFolder)).mkdirs();
		if(!success) {
			if(!Files.exists(Paths.get(outFolder))){
				System.out.println("Failed at creating the output folder: " + outFolder);
				System.exit(0);
			}
		}
		else
			System.out.println("Output folder successfully created: " + outFolder);
		
		System.out.println("Aligning...");
		long ini = System.currentTimeMillis();
		alignNewselaDataset(inFolder,language,outFolder,alignmentLevel, similarityStrategy, alignmentStrategy, subLvAlignmentStrategy, model);
		long end = System.currentTimeMillis();
		System.out.println("Alignment done in " + ((double) ((end-ini)/ 1000) / 60) + " minutes.");
	}

	private static void alignNewselaDataset(String inFolder, String language, String outFolder,
			String alignmentLevel, String similarityStrategy, String alignmentStrategy, String subLvAlignmentStrategy, ModelContainer model) throws IOException {	
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(new String[]{"*."+language+".0.txt"});
		scanner.setBasedir(inFolder);
		scanner.setCaseSensitive(false);
		scanner.scan();
		String[] files = scanner.getIncludedFiles();
	
		int k = 0;
		for(String fileProto : files){		
			Map<String, List<Text2abstractRepresentation>> file2clean = new HashMap<String, List<Text2abstractRepresentation>>();
			for (int i = 0; i <= 5; i++) {
				String file1 = fileProto.replace("." + language + ".0.txt","." + language + "." + i + ".txt");
				String text1 = MyIOutils.readTextFile(inFolder+file1);
				if (text1 != null) 
					file2clean.put(file1, TextProcessingUtils.getCleanText(text1,alignmentLevel, similarityStrategy, model));
			}
			
			List<Text2abstractRepresentation> cleanSubtexts1;
			List<Text2abstractRepresentation> cleanSubtexts2;
			for (int i = 0; i < 5; i++) {
				String file1 = fileProto.replace("." + language + ".0.txt", "." + language + "." + i + ".txt");
				if ((cleanSubtexts1 = file2clean.get(file1)) != null) {
					for (int j = i + 1; j <= 5; j++) {
						String file2 = fileProto.replace("." + language + ".0.txt", "." + language + "." + j + ".txt");
						if ((cleanSubtexts2 = file2clean.get(file2)) != null) {
							List<TextAlignment> alignments = VectorUtils.alignUsingStrategy(cleanSubtexts1,	cleanSubtexts2, similarityStrategy, alignmentStrategy, model);
//							MyIOutils.displayAlignments(alignments,false);
//							System.in.read();
							if (alignmentLevel.equals(DefinedConstants.ParagraphSepEmptyLineAndSentenceLevel))
								alignments = VectorUtils.getSubLevelAlignments(alignments, cleanSubtexts1, cleanSubtexts2, similarityStrategy, subLvAlignmentStrategy, model);
							MyIOutils.saveAlignments(alignments, outFolder + file2 + "_ALIGNED_WITH_" + file1);
						}
					}
				}
			}
			
//			String text1 = MyIOutils.readTextFile(inFolder+file1);
//			List<Text2abstractRepresentation> cleanSubtexts1 = TextProcessingUtils.getCleanText(text1,alignmentLevel, similarityStrategy, model);
//			String fileAux = null;
//			List<Text2abstractRepresentation> cleanSubtextsAux = null;
			
//			for (int i = 1; i <= 5; i++) {
//				String file2 = file1.replace("." + language + ".0.txt","." + language + "." + i + ".txt");
//				String text2 = MyIOutils.readTextFile(inFolder + file2);
//				if (text2 != null) {
//					List<Text2abstractRepresentation> cleanSubtexts2 = processCompareAndSave(cleanSubtexts1, text2, alignmentLevel, alignmentStrategy, subLvAlignmentStrategy, similarityStrategy, model, outFolder + file2 + "_ALIGNED_WITH_" + file1);
//					if(i == 1){
//						fileAux = file2;
//						cleanSubtextsAux = cleanSubtexts2;
//					}
//				}
//			}
//			file1 = fileAux;
//			cleanSubtexts1 = cleanSubtextsAux;
//			if(file1 != null)
//			for (int i = 2; i <= 5; i++) {
//				String file2 = file1.replace("." + language + "." + (i - 1) + ".txt","." + language + "." + i + ".txt");
//				String text2 = MyIOutils.readTextFile(inFolder + file2);
//				if (text2 != null) {
//					List<Text2abstractRepresentation> cleanSubtexts2 = processCompareAndSave(cleanSubtexts1, text2, alignmentLevel, alignmentStrategy, subLvAlignmentStrategy, similarityStrategy, model, outFolder + file2 + "_ALIGNED_WITH_" + file1);
//					file1 = file2;
//					cleanSubtexts1 = cleanSubtexts2;
//				}
//			}
			
			k++;
			if(k%10 == 0)
				System.out.println(k + "/" + files.length);
		}
	}

	private static List<Text2abstractRepresentation> processCompareAndSave(List<Text2abstractRepresentation> cleanSubtexts1, String text2, 
			String alignmentLevel, String alignmentStrategy, String subLvAlignmentStrategy, String similarityStrategy, ModelContainer model,
			String outFile) throws IOException {
		List<Text2abstractRepresentation> cleanSubtexts2 = TextProcessingUtils.getCleanText(text2,alignmentLevel, similarityStrategy,model);
		List<TextAlignment> alignments = VectorUtils.alignUsingStrategy(cleanSubtexts1, cleanSubtexts2,similarityStrategy, alignmentStrategy, model);
//		MyIOutils.displayAlignments(alignments,false);
//		System.in.read();	
		if(alignmentLevel.equals(DefinedConstants.ParagraphSepEmptyLineAndSentenceLevel))
			alignments = VectorUtils.getSubLevelAlignments(alignments, cleanSubtexts1, cleanSubtexts2, similarityStrategy, subLvAlignmentStrategy, model);
		MyIOutils.saveAlignments(alignments, outFile);
//		MyIOutils.displayAlignments(alignments,false);
//		System.in.read();			
		return cleanSubtexts2;
	}
}
