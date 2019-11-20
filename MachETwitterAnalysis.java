package mj.mache;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

public class MachETwitterAnalysis {

	/**
	 * @param args
	 */

	// This method classified the text/tweet into two classification - positive or negative
	// The categorize() returns 1 for positive text and 0 for negative.
	public static int classifyNewText(DoccatModel sentimentModel, String input)
			throws IOException {
		DocumentCategorizerME myCategorizer = new DocumentCategorizerME(
				sentimentModel);
		double[] outcomes = myCategorizer.categorize(input);
		return Integer.parseInt(myCategorizer.getBestCategory(outcomes));
	}

	// This method returns the list of all positive words.
	public static List<String> getPositiveWords() throws IOException {
		List<String> outputList = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(
				"C:\\Users\\MJAGRE\\Desktop\\mache\\positive-words.txt"));
		String line = null;
		while ((line = br.readLine()) != null) {
			outputList.add(line);
		}
		br.close();
		return outputList;
	}

	// This method returns the list of all negative words.
	public static List<String> getNegativeWords() throws IOException {
		List<String> outputList = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(
				"C:\\Users\\MJAGRE\\Desktop\\mache\\negative-words.txt"));
		String line = null;
		while ((line = br.readLine()) != null) {
			outputList.add(line);
		}
		br.close();
		return outputList;
	}

	// This method returns the list of all stop words in English language.
	public static List<String> getStopWords() throws IOException {
		List<String> outputList = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(
				"C:\\Users\\MJAGRE\\Desktop\\mache\\nlp_en_stop_words.txt"));
		String line = null;
		while ((line = br.readLine()) != null) {
			outputList.add(line);
		}
		br.close();
		return outputList;
	}

	// This method returns the value sorted hashmap of the input hashmap.
	// We use this method to calculate the most used positive, negative words.
	// This method is helpful to construct the word list for word cloud too.
	public static LinkedHashMap<String, Integer> sortHashMapByValues(
			Map<String, Integer> wordCountMap) {
		List<String> mapKeys = new ArrayList<String>(wordCountMap.keySet());
		List<Integer> mapValues = new ArrayList<Integer>(wordCountMap.values());
		Collections.sort(mapValues, Collections.reverseOrder());
		Collections.sort(mapKeys);
		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		Iterator<Integer> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Integer val = valueIt.next();
			Iterator<String> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				String key = keyIt.next();
				Integer comp1 = wordCountMap.get(key);
				Integer comp2 = val;

				if (comp1.equals(comp2)) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}

	// main() method
	public static void main(String[] args) throws IOException {
		Map<String, Integer> wordCountMap = new HashMap<String, Integer>();
		List<String> stopWordsList = getStopWords();
		List<String> globalPositiveWordsList = getPositiveWords();
		Map<String, Integer> positiveWordsMap = new HashMap<String, Integer>();
		List<String> globalNegativeWordsList = getNegativeWords();
		Map<String, Integer> negativeWordsMap = new HashMap<String, Integer>();
		InputStream dataIn = new FileInputStream(
				"C:\\Users\\MJAGRE\\Desktop\\mache\\tweets.txt");
		ObjectStream<String> lineStream = new PlainTextByLineStream(dataIn,
				"UTF-8");
		ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(
				lineStream);
		DoccatModel sentimentModel = DocumentCategorizerME.train("en",
				sampleStream);

		int positiveCounts = 0, negativeCounts = 0;
		BufferedReader br = new BufferedReader(new FileReader(
				"C:\\Users\\MJAGRE\\Desktop\\mache\\MustangMachE_Tweets.csv"));
		String line = null, mapKey = null;
		String[] lineSplitter = null;
		int i = 0;
		while ((line = br.readLine()) != null) {
			if (classifyNewText(sentimentModel, line) == 1)
				positiveCounts++;
			if (classifyNewText(sentimentModel, line) == 0)
				negativeCounts++;
			lineSplitter = line.split(" ");
			for (i = 0; i < lineSplitter.length; i++) {
				mapKey = lineSplitter[i].replaceAll("[\\.\\',\\?]", "");
				if (!(stopWordsList.contains(mapKey)) && mapKey.length() > 3) {
					if (wordCountMap.containsKey(mapKey))
						wordCountMap.put(mapKey, wordCountMap.get(mapKey) + 1);
					else
						wordCountMap.put(mapKey, 1);
				}

				if (globalPositiveWordsList.contains(mapKey)) {
					if (positiveWordsMap.containsKey(mapKey))
						positiveWordsMap.put(mapKey,
								positiveWordsMap.get(mapKey) + 1);
					else
						positiveWordsMap.put(mapKey, 1);
				}
				if (globalNegativeWordsList.contains(mapKey)) {
					if (negativeWordsMap.containsKey(mapKey))
						negativeWordsMap.put(mapKey,
								negativeWordsMap.get(mapKey) + 1);
					else
						negativeWordsMap.put(mapKey, 1);
				}
			}
		}
		br.close();

		LinkedHashMap<String, Integer> sortedWordCountMap = sortHashMapByValues(wordCountMap);
		System.out.println("***TOP 10 MOST USED WORDS***");
		int count = 0;
		for (Entry<String, Integer> entry : sortedWordCountMap.entrySet()) {
			if (count < 10)
				System.out.println(entry.getKey() + " - " + entry.getValue());
			count++;
		}
		System.out
				.println("\n----------------------------------------------------------------");

		System.out
				.println("\n----------------------------------------------------------------");
		System.out.println("***POSITIVE WORDS***");
		count = 0;
		LinkedHashMap<String, Integer> sortedPositiveWordsMap = sortHashMapByValues(positiveWordsMap);
		for (Entry<String, Integer> entry : sortedPositiveWordsMap.entrySet()) {
			if (count < 10)
				System.out.println(entry.getKey() + " - " + entry.getValue());
			count++;
		}
		System.out
				.println("\n----------------------------------------------------------------");
		System.out.println("***NEGATIVE WORDS***");
		count = 0;
		LinkedHashMap<String, Integer> sortedNegativeWordsMap = sortHashMapByValues(negativeWordsMap);
		for (Entry<String, Integer> entry : sortedNegativeWordsMap.entrySet()) {
			if (count < 10)
				System.out.println(entry.getKey() + " - " + entry.getValue());
			count++;
		}
		System.out
				.println("\n----------------------------------------------------------------");
		System.out.println("***POSITIVE SENTIMENT COUNT***");
		System.out.print(positiveCounts);
		System.out
				.println("\n----------------------------------------------------------------");
		System.out.println("***NEGATIVE SENTIMENT COUNT***");
		System.out.print(negativeCounts);

	}
}
