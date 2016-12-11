package Preprocessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import simplifiedTextAlignment.Utils.DefinedConstants;
import simplifiedTextAlignment.Utils.MyIOutils;
import simplifiedTextAlignment.Utils.TextProcessingUtils;

public class CreateSentencePerLineFiles {

	public static void main(String args[]) throws IOException{
		String baseDir = "/path/to/your/newsela/parent/folder/";
		String inFolder = baseDir+"newsela_article_corpus_2016-01-29/articles/";
		String outFolder = baseDir+"newsela_article_corpus_2016-01-29/articlesSentPerLine/";

		File folder = new File(inFolder);
		for(File file : folder.listFiles()){
			String text = MyIOutils.readTextFile(inFolder+file.getName());
			List<String> subtexts = TextProcessingUtils.getSubtexts(text,DefinedConstants.SentenceLevel);
			BufferedWriter out = new BufferedWriter(new FileWriter(outFolder+file.getName()));
			for(int i = 0; i < subtexts.size()-1; i++)
				out.write(subtexts.get(i)+"\n");
			out.write(subtexts.get(subtexts.size()-1));
			out.close();
		}
	}
}
