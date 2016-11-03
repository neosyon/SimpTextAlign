package simplifiedTextAlignment.Representations;

public class ModelContainer {
	public EmbeddingModel em = null;
	public NgramModel nm = null;
	
	public ModelContainer(EmbeddingModel embeddingModel) {
		em = embeddingModel;
	}

	public ModelContainer(NgramModel ngramModel) {
		nm = ngramModel;
	}

}
