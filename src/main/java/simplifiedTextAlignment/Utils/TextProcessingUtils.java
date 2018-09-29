package simplifiedTextAlignment.Utils;

import java.io.Reader;
import java.io.StringReader;
import java.text.BreakIterator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import simplifiedTextAlignment.Representations.EmbeddingModel;
import simplifiedTextAlignment.Representations.ModelContainer;
import simplifiedTextAlignment.Representations.NgramModel;
import simplifiedTextAlignment.Representations.Text2abstractRepresentation;
import simplifiedTextAlignment.Utils.DefinedConstants;

public class TextProcessingUtils {
	
	public static List<Text2abstractRepresentation> getCleanText(String text, String alignmentStrategy,
			String similarityStrategy, ModelContainer model) {
		List<String> subtexts = getSubtexts(text,alignmentStrategy);
		List<Text2abstractRepresentation> cleanSubtexts = cleanSubtexts(subtexts, similarityStrategy, model);
		if(similarityStrategy.equals(DefinedConstants.WAVGstrategy))
			VectorUtils.calculateWAVGs(cleanSubtexts,model.em);		
		if(alignmentStrategy.equals(DefinedConstants.ParagraphSepEmptyLineAndSentenceLevel))
			getCleanSublevelText(cleanSubtexts, DefinedConstants.SentenceLevel, similarityStrategy, model);
		return cleanSubtexts;
	}

	private static void getCleanSublevelText(List<Text2abstractRepresentation> cleanSubtexts, String alignmentStrategy,
			String similarityStrategy, ModelContainer model) {
		for(Text2abstractRepresentation cleanText : cleanSubtexts)
			cleanText.setSubLevelRepresentations(getCleanText(cleanText.getText(), alignmentStrategy, similarityStrategy, model));
	}

	private static List<Text2abstractRepresentation> cleanSubtexts(List<String> subtexts, String similarityStrategy, ModelContainer model) {
		List<Text2abstractRepresentation> cleanSubtexts = new LinkedList<Text2abstractRepresentation>();
		for(String subtext : subtexts)
			if(similarityStrategy.equals(DefinedConstants.WAVGstrategy) || similarityStrategy.equals(DefinedConstants.CWASAstrategy))
				cleanSubtexts.add(cleanSubtextForEmbeddingModel(subtext, model.em));
			else if(similarityStrategy.equals(DefinedConstants.CNGstrategy))
				cleanSubtexts.add(cleanSubtextForCNGmodel(subtext, model.nm));
		return cleanSubtexts;
	}

	private static Text2abstractRepresentation cleanSubtextForCNGmodel(String subtext, NgramModel nm) {
		Map<Integer,Double> cleanTokenIndices = nm.getCharNgramTFIDFmap(subtext);
		return new Text2abstractRepresentation(subtext,	cleanTokenIndices);
	}

	public static Text2abstractRepresentation cleanSubtextForEmbeddingModel(String subtext, EmbeddingModel em) {
		StringTokenizer tokenizer = new StringTokenizer(subtext, " _&%=;.,-!?¡¿:;*/\\\"`''");
		List<Integer> cleanTokenIndices = new LinkedList<Integer>();
		while(tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().toLowerCase();
			Integer index = null;
			if(isValidTokenForEmbeddingModel(token) && (index=em.getIndex(token)) != null)
				cleanTokenIndices.add(index);
		}
		return new Text2abstractRepresentation(subtext,	cleanTokenIndices);
	}
	
	public static Collection<? extends String> getCleanEmbeddingModelTokens(String text) {
		StringTokenizer tokenizer = new StringTokenizer(text, " _&%=;.,-!?¡¿:;*/\\\"`''");
		List<String> cleanTokens = new LinkedList<String>();
		while(tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().toLowerCase();
			if(isValidTokenForEmbeddingModel(token))
				cleanTokens.add(token);
		}
		return cleanTokens;
	}

	private static boolean isValidTokenForEmbeddingModel(String token) {
		return (token.length() > 1 && hasNoNumbers(token));
	}

	private static boolean hasNoNumbers(String token) {
		char c;
		for(int i = 0; i < token.length(); i++)
			if(!((c=token.charAt(i)) >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
				return false;
		return true;
	}

	public static List<String> getSubtexts(String text, String alignemntStrategy) {
		List<String> subtexts = new LinkedList<String>();
		if(alignemntStrategy.equals(DefinedConstants.ParagraphSepEmptyLineLevel) || alignemntStrategy.equals(DefinedConstants.ParagraphSepEmptyLineAndSentenceLevel)){
			String ar[] = text.split("\n\n");
			for(String subtext : ar) subtexts.add(subtext);
		}
		else if(alignemntStrategy.equals(DefinedConstants.SentenceLevel)){
			BreakIterator bi = BreakIterator.getSentenceInstance(Locale.US);
	        bi.setText(text);
	        int start = bi.first();
	        int end = bi.next();
	        int tempStart = start;
	        while (end != BreakIterator.DONE) {
	            String sentence = text.substring(start, end);
	            if (!endsWithAbbreviation(sentence) || end == text.length()) {
	                sentence = text.substring(tempStart, end);
	                tempStart = end;
	                for(String auxSentence: sentence.split("\n"))
	                	if(auxSentence.length() > 0)
	                		subtexts.add(auxSentence);
	            }	            	
	            start = end; 
	            end = bi.next();
	        }
		}
		else{
			System.out.println("Error: alignment level not recognized.");
			System.exit(1);
		}
		
		return subtexts;
	}
	
//    private static boolean hasAbbreviation(String sentence) {
//        if (sentence == null || sentence.isEmpty()) {
//            return false;
//        }
//        for (String w : DefinedConstants.ABBREVIATIONS) {
//            if (sentence.contains(w)) {
//                return true;
//            }
//        }
//        return false;
//    }
    
    private static boolean endsWithAbbreviation(String sentence) {
        if (sentence == null || sentence.isEmpty()) {
            return false;
        }
        for (String w : DefinedConstants.ABBREVIATIONS) {
            if (sentence.endsWith(w)) {
                return true;
            }
        }
        return false;
    }
}
