This project includes several lexical and semantic  **text similarity methods** and **alignment strategies** for the **simplified text alignment** task. It is also able to align at different levels: paragraph, sentence, and sentence with paragraph pre-alignment. 

**_TO DO_** describe alignment and similarity lexical (language-independent) and semantic (word embedding collection-dependent) strategies joint with alignment level. 

# How to use

We provide classes to align the Newsela dataset and also a custom one.

## Newsela dataset

To align the Newsela dataset execute the following class:

[src/main/java/simplifiedTextAlignment/DatasetAlignment/AlignNewselaDataset.java](src/main/java/simplifiedTextAlignment/DatasetAlignment/AlignNewselaDataset.java)

### Usage

```
AlignNewselaDataset -i inFolder -o outFolder  -l language -s similarityStrategy -a alignmentLevel -t alignmentStrategy {-u SubLevelalignmentStrategy} {-e embeddingsTxtFile}

```

#### Description of the arguments:

*inFolder*: the folder with the original newsela texts.

*outFolder*: the folder where the alignments will be stored.

*language*: the language of the newsela texts. It can be *es* or *en*.

*similarityStrategy*: is the strategy employed to compare the texts. It can be *CNG*, *WAVG*, or *CWASA*, where the *N* in *CNG* should be changed for the desired *n*-gram size, e.g. *C3G*. Default: *C3G*

*alignmentLevel*: is the text alignment level, i.e. the representation of each to align. It can be *paragraphSeparatedByEmptyLine*, *sentence*, or *sentenceWithParagraphSeparatedByEmptyLinePreAlignment*. Default: *sentence*.

*alignmentStrategy*: is the strategy to align the compared texts. It can be *closestSimStrategy* or *closestSimKeepingSequenceStrategy*. Default: *closestSimStrategy*.

*subLvAlignmentStrategy*: this is only employed with the *alignmentLevel* option set to **. It is the strategy to align the compared second level texts, e.g. the sentences inside the aligned paragraphs. It can be *closestSimStrategy* or *closestSimKeepingSequenceStrategy*. Default: *closestSimStrategy*.
		
*embeddingsTxtFile*: the embeddings using the classical word2vec txt format with a first line with the number of embeddings and embedding length and the next lines containing the embeddings. This file is only required with *WAVG* and *CWASA*.

## Custom dataset

To align a custom dataset the following class:

[src/main/java/simplifiedTextAlignment/DatasetAlignment/ComputeSimilarityBetweenTexts.java](src/main/java/simplifiedTextAlignment/DatasetAlignment/ComputeSimilarityBetweenTexts.java)

*We note that this class also works if you remove the first and last column of the annotations.txt file containing the Standard Wikipedia to Simple Wikipedia alignments made by [Hwang et al.](http://ssli.ee.washington.edu/tial/projects/simplification/)*

### Usage

```
ComputeSimilarityBetweenTexts -i inFile -o outFile -s similarityStrategy {-e embeddingsTxtFile}

```

#### Description of the arguments:

*inFile*: it is a file with two tab-separated texts per line. The program will output a similarity score for each one of these text pairs.

*outFile*: the name of the output file. It contains the original tab-separated texts plus their similarity score.

*similarityStrategy*: is the strategy employed to compare the texts. It can be *CNG*, *WAVG*, or *CWASA*, where the *N* in *CNG* should be replaced for the desired *n*-gram size, e.g. *C3G*. Default: *C3G*.	

*embeddingsTxtFile*: the embeddings using the classical word2vec txt format with a first line with the number of embeddings and embedding length and the next lines containing the embeddings. This file is only required with *WAVG* and *CWASA*.

