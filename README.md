# CATS: A Tool for Customised Alignment of Text Simplification Corpora

This project includes several lexical and semantic  **text similarity methods** and **alignment strategies** for the **simplified text alignment** task. It is also able to **align at different text representation levels**: **paragraph**, **sentence**, and **sentence with paragraph pre-alignment**. We provide classes to align the **[Newsela dataset](https://newsela.com/data/)** and also a **custom** one. This work has been published in [1].

The **lexical text similarity strategy** implements the Character *N*-Gram (**CNG**) [3] text similarity model. We use the log TF-IDF weighting and compare vectors with the cosine similarity. Since this approach do **not uses any language-specific resource**, it works for any language. 

There are two possible **semantic text similarity strategies**. The first one (**WAVG**), is based on representing each text by averaging its word embeddings and comparing them with the cosine similarity. The second approach implements the Continuous Word Alignment-based Similarity Analysis (**CWASA**) [4] model. It is based on the use of directed edge word embedding alignments. Note that these two **embedding-based approaches require the corresponding embedding collection** as input.

We have two **alignment strategies**. The first one **aligns texts using the closest (most similar) text**. The second one **aligns to the closest** texts **but** employs a post-processing to **force the target aligned text offsets to be in increasing value**. Basically, it extracts the longest increassing (or equal) subsequence of aligned target offsets. Next, it restricts the searching space of the texts not included in that sequence to the indexes of its previous and next aligned texts. 

# How to use CATS-Align for simplified text alignment

CATS-Align makes it possible to automatically sentence-align of original and manually simplified text.

We provide classes to align the **[Newsela dataset](https://newsela.com/data/)** and also a **custom** one. We also provide with the precompiled executable jars in the [jars/](jars/) folder.

To align the Newsela dataset execute the following class:

[src/main/java/simplifiedTextAlignment/DatasetAlignment/AlignNewselaDataset.java](src/main/java/simplifiedTextAlignment/DatasetAlignment/AlignNewselaDataset.java)

## Usage

```
java -jar AlignNewselaDataset.jar -i inFolder -o outFolder  -l language -s similarityStrategy -a alignmentLevel -t alignmentStrategy {-u SubLevelalignmentStrategy} {-e embeddingsTxtFile}

```

### Description of the arguments:

*inFolder*: the folder with the original newsela texts.

*outFolder*: the folder where the alignments will be stored.

*language*: the language of the newsela texts. It can be *es* or *en*.

*similarityStrategy*: is the strategy employed to compare the texts. It can be *CNG*, *WAVG*, or *CWASA*, where the *N* in *CNG* should be changed for the desired *n*-gram size, e.g. *C3G*. Default: *C3G*

*alignmentLevel*: is the text alignment level, i.e. the representation of each to align. It can be *paragraphSeparatedByEmptyLine*, *sentence*, or *sentenceWithParagraphSeparatedByEmptyLinePreAlignment*. Default: *sentence*.

*alignmentStrategy*: is the strategy to align the compared texts. It can be *closestSimStrategy* or *closestSimKeepingSequenceStrategy*. Default: *closestSimStrategy*.

*subLvAlignmentStrategy*: this is only employed with the *alignmentLevel* option set to *sentenceWithParagraphSeparatedByEmptyLinePreAlignment*. It is the strategy to align the compared second level texts, e.g. the sentences inside the aligned paragraphs. It can be *closestSimStrategy* or *closestSimKeepingSequenceStrategy*. Default: *closestSimStrategy*.
		
*embeddingsTxtFile*: the embeddings using the classical word2vec txt format with a first line with the number of embeddings and embedding length and the next lines containing the embeddings. This file is only required with *WAVG* and *CWASA*.

# How to use CATS-Measure to compute similarity between texts

CATS-Measure provides similarity measures for three different sentence/paragraph similarity methods.

To compute similarity of a custom dataset of text pairs use the following class:

[src/main/java/simplifiedTextAlignment/DatasetAlignment/ComputeSimilarityBetweenTexts.java](src/main/java/simplifiedTextAlignment/DatasetAlignment/ComputeSimilarityBetweenTexts.java)

*We note that this class also works if you remove the first and last column of the annotations.txt file containing the Standard Wikipedia to Simple Wikipedia alignments made by [Hwang et al.](http://ssli.ee.washington.edu/tial/projects/simplification/)*

## Usage

```
java -jar ComputeSimilarityBetweenTexts.jar -i inFile -o outFile -s similarityStrategy {-e embeddingsTxtFile}

```

### Description of the arguments:

*inFile*: it is a file with two tab-separated texts per line. The program will output a similarity score for each one of these text pairs.

*outFile*: the name of the output file. It contains the original tab-separated texts plus their similarity score.

*similarityStrategy*: is the strategy employed to compare the texts. It can be *CNG*, *WAVG*, or *CWASA*, where the *N* in *CNG* should be replaced for the desired *n*-gram size, e.g. *C3G*. Default: *C3G*.	

*embeddingsTxtFile*: the embeddings using the classical word2vec txt format with a first line with the number of embeddings and embedding length and the next lines containing the embeddings. This file is only required with *WAVG* and *CWASA*.

# How to cite

**Please cite [1] if you use this code to align English corpora.**

**Please cite [2] paper if you use this code to align Spanish corpora or use CATS-Measure.**

# References

[1] Sanja Štajner, Marc Franco-Salvador, Simone Paolo Ponzetto, Paolo Rosso, and Heiner Stuckenschmidt. Sentence Alignment Methods for Improving Text Simplification Systems. In Proceedings of the 55th Annual Meeting of the Association for Computational Linguistics (ACL 2017), pages 97-102.

```
@inproceedings{StajnerACL17,
  author    = {Sanja Stajner and
               Marc Franco{-}Salvador and
               Simone Paolo Ponzetto and
               Paolo Rosso and
               Heiner Stuckenschmidt},
  title     = {Sentence Alignment Methods for Improving Text Simplification Systems},
  booktitle = {Proceedings of the 55th Annual Meeting of the Association for Computational
               Linguistics, {ACL} 2017, Vancouver, Canada, July 30 - August 4, Volume
               2: Short Papers},
  pages     = {97--102},
  year      = {2017},
  url       = {https://doi.org/10.18653/v1/P17-2016},
  doi       = {10.18653/v1/P17-2016},
  timestamp = {Fri, 04 Aug 2017 16:38:24 +0200},
  biburl    = {https://dblp.org/rec/bib/conf/acl/StajnerFPRS17}
}
```

[2] Sanja Štajner, Marc Franco-Salvador, Simone Paolo Ponzetto and Paolo Rosso. CATS: A Tool for Customised Alignment of Text Simplification Corpora. To appear in the proceedings of the 11th Language Resources and Evaluation Conference (LREC 2018), Miyazaki, Japan.

```
@inproceedings{StajnerLREC18,
  author    = {Sanja Stajner and
               Marc Franco{-}Salvador and
               Simone Paolo Ponzetto and
               Paolo Rosso},
  title     = {CATS: A Tool for Customised Alignment of Text Simplification Corpora},
  booktitle = {Proceedings of the 11th Language Resources and Evaluation Conference,
               {LREC} 2018, Miyazaki, Japan, May 7-12},
  year      = {2018}
}
```

[3] Paul Mcnamee and James Mayfield. 2004. Character n-gram tokenization for European language text retrieval. Information Retrieval, 7(1):73–97.

[4] Marc Franco-Salvador, Parth Gupta, Paolo Rosso, and Rafael E. Banchs. 2016. Cross-language plagiarism detection over continuous-space- and knowledge graph-based representations of language. Knowledge-Based Systems, 111:87–99.

