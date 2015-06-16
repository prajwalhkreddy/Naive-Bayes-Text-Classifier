

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;


/*Prajwal Halasahally KeshavaReddy
 * Naive Bayes Classifier Implementation
 * This program takes 2 input parameters, the test folder path and the path the output file is to be generated
 */


public class Classifier {
	static String trainFolder = "train";
	static String testFolder = "";
	static String classNamesFile = "class_name.txt";
	static ArrayList<String> classes = new ArrayList<String>();
	static ArrayList<String> trainFiles;
	static ArrayList<String> testFiles;
	static HashMap<String, ArrayList<String>> classFolder = new HashMap<String, ArrayList<String>>();
	static HashMap<String, Integer> countMap;
	static HashMap<String, Integer> vocabCountMap = new HashMap<String, Integer>();;
	static HashMap<String, Integer> testCountMap;
	static HashMap<String, HashMap<String, Integer>> wordMap = new HashMap<String, HashMap<String, Integer>>();
	static HashMap<String, Integer> docCount = new HashMap<String, Integer>();
	static HashMap<String, Double> priorProbability = new HashMap<String, Double>();
	static HashMap<String, Double> conditionalProbability = new HashMap<String, Double>();
	static HashMap<String, String> finalResult = new HashMap<String, String>();
	static HashMap<String, Double> finalProbability;
	static HashMap<String, Double> tempProbability;
	static HashMap<String, Integer> wordsPerClass = new HashMap<String, Integer>();
	static HashMap<String, Integer> classNamesHashMap = new HashMap<String, Integer>();
	static Set<String> stopWords = new HashSet<>();
	static int totalDocCount = 0;
	static int classDocCount;
	static int vocabCount = 0;
	static int totalWordsClass = 0;
	static PrintWriter writer;
	static String outputPath="";

	public static void main(String[] args) {
		
		if(args.length!=2){
			System.out.println("Please provide 2 input arguments. The test folder path and the output folder path");
			System.exit(0);
		}
		testFolder = args[0];
		outputPath=args[1];
		Classifier cl = new Classifier();

		try {
			writer = new PrintWriter(outputPath+"\\output.txt", "UTF-8");
			cl.parseStopWords("stopwords");
			cl.readClass();
			cl.calculatePrior();
			cl.readClassNames();
			cl.readTestData();
			writer.close();
			System.out.println("Classification Complete \n Output File Written");

		} catch (Exception e) {
			System.err.println("Error!");
		}

	}
	
//This method reads the training data and stores the words in a HashMap
	public void readClass() throws IOException {
		File file = new File(trainFolder);
		String[] classNames = file.list();

		for (String name : classNames) {
			classDocCount = 0;
			if (new File(trainFolder + "\\" + name).isDirectory()) {
				countMap = new HashMap<String, Integer>();
				// System.out.println(trainFolder + "\\" + name);
				classes.add(name);

				File tempfile = new File(trainFolder + "\\" + name);
				String[] fileNames = tempfile.list();
				trainFiles = new ArrayList<String>();
				for (String fileName : fileNames) {
					String tempPath = trainFolder + "\\" + name + "\\"
							+ fileName;
					if (new File(tempPath).isFile()) {
						totalDocCount++;
						classDocCount++;
						trainFiles.add(fileName);
						// System.out.println(tempPath);
						Scanner readFile = new Scanner(new File(tempPath))
								.useDelimiter("[^a-zA-Z]+");

						while (readFile.hasNext()) {
							String word = readFile.next().toLowerCase();
							//Check for stopWords. stopWords is an HashMap whci hahas been read from a file
							if (stopWords.contains(word)) { // removing all stop
															// words
								continue;
							}
							
							//Stemming Operations
							String stemmedWord = null;
							String temp = word;
							Stemmer myStemmer = new Stemmer();
							// add the word to the stemmer
							myStemmer.add(temp.toCharArray(), temp.length());
							myStemmer.stem();
							stemmedWord = myStemmer.toString();
							//Check if word already exists, then increment counter accordingly
							if (countMap.containsKey(stemmedWord)) {
								countMap.put(stemmedWord,
										countMap.get(stemmedWord) + 1);
								totalWordsClass++;

							} else {
								countMap.put(stemmedWord, 1);
								totalWordsClass++;
								if (!vocabCountMap.containsKey(stemmedWord)) {
									vocabCountMap.put(stemmedWord, 1);
									vocabCount++;
								}

							}
						}
					}

				}
				docCount.put(name, classDocCount);
				classFolder.put(name, trainFiles);
				wordMap.put(name, countMap);
				wordsPerClass.put(name, totalWordsClass);
				totalWordsClass = 0;

			}
		}
		//System.out.println(wordMap);
	}

	
	//Calculating the Prior Probability for each class type
	public void calculatePrior() {
		Iterator it = docCount.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry) it.next();
			priorProbability.put(pair.getKey().toString(),
					Double.parseDouble(pair.getValue().toString())
							/ totalDocCount);

			// System.out.println(pair.getKey() + " = " + pair.getValue());

		}
	}

	//This method reads the training data and stores the words in a HashMap
	public void readTestData() throws IOException {
		File tempfile = new File(testFolder);

		String[] fileNames = tempfile.list();
		testFiles = new ArrayList<String>();
		for (String fileName : fileNames) {
			testCountMap = new HashMap<String, Integer>();
			String tempPath = testFolder + "\\" + fileName;

			if (new File(tempPath).isFile()) {
				testFiles.add(fileName);
				// System.out.println("Test doc list" + testFiles);

				Scanner readFile = new Scanner(new File(tempPath))
						.useDelimiter("[^a-zA-Z]+");

				while (readFile.hasNext()) {
					String word = readFile.next().toLowerCase();
					//Check for stopWords. stopWords is an HashMap whci hahas been read from a file
					if (stopWords.contains(word)) {
						continue;
					}
					//Stemming operations
					String stemmedWord = null;
					String temp = word;
					Stemmer myStemmer = new Stemmer();
					// add the word to the stemmer
					myStemmer.add(temp.toCharArray(), temp.length());
					myStemmer.stem();
					stemmedWord = myStemmer.toString();
					if (testCountMap.containsKey(stemmedWord)) {
						testCountMap.put(stemmedWord,
								testCountMap.get(stemmedWord) + 1);
					} else {
						testCountMap.put(stemmedWord, 1);
					}
				}
			}
			calculatePosterior();
			chooseAClass(fileName);
		}
		// writer.println(testFiles);
		// System.out.println("Test doc list" + testFiles);
		// System.out.println(testCountMap);

	}

	
	//We calculate the conditional probability of each word in the train document and store it in another HashMap
	@SuppressWarnings("unchecked")
	public void calculatePosterior() {
		HashMap<String, Integer> temp = null;
		// System.out.println(wordMap);

		Iterator it1 = wordMap.entrySet().iterator();
		while (it1.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry) it1.next();
			Iterator it2 = testCountMap.entrySet().iterator();
			while (it2.hasNext()) {
				HashMap.Entry distinct = (HashMap.Entry) it2.next();

				temp = new HashMap<String, Integer>();
				temp = (HashMap<String, Integer>) pair.getValue();
				Iterator it3 = temp.entrySet().iterator();
				double value = 0.0;
				int x1 = 0;
				int y1 = 0;
				if (temp.containsKey(distinct.getKey().toString())) {
					x1 = Integer.parseInt(temp
							.get(distinct.getKey().toString()).toString());
					// System.out.println(x1);
				} else {
					x1 = 0; //If the word doesn't occur in the test document
				}
				y1 = wordsPerClass.get(pair.getKey().toString());
				// System.out.println(x1+"+ 1"+"/"+y1+"+"+vocabCount);
				value = (double) (x1 + 1) / (double) (y1 + vocabCount);
				conditionalProbability.put(distinct.getKey().toString() + "|"
						+ pair.getKey().toString(), value);
			}

			// System.out.println(pair.getKey() + " = " + pair.getValue());

		}

	}

	//Method to read the class_name.txt and store it in a HashMap
	public void readClassNames() {
		Scanner in1;
		try {
			in1 = new Scanner(new FileReader(classNamesFile));

			while (in1.hasNextLine()) {
				// System.out.println(in1.nextLine());
				String temp[] = in1.nextLine().split(" ");
				classNamesHashMap.put(temp[1], Integer.parseInt(temp[0]));
			}
			// System.out.println(classNamesHashMap);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	
	//Calculating the Max Log Likelihood using the prior and the conditional probabilities and store it another hashmap
	public void chooseAClass(String fileName) {
		finalProbability = new HashMap<String, Double>();
		Iterator it1 = priorProbability.entrySet().iterator();
		double value = 0;
		double x2 = 0.0, x3 = 0.0;
		int x1;
		while (it1.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry) it1.next();
			Iterator it2 = testCountMap.entrySet().iterator();
			tempProbability = new HashMap<String, Double>();
			value = 0;

			while (it2.hasNext()) {
				x2 = 0.0;
				x3 = 0.0;
				x1 = 0;
				HashMap.Entry testCount = (HashMap.Entry) it2.next();
				String str = testCount.getKey().toString() + "|"
						+ pair.getKey().toString();
				tempProbability.put(str,
						Double.parseDouble(conditionalProbability.get(str)
								.toString()));
				x1 = testCountMap.get(testCount.getKey().toString());
				x2 = Double.parseDouble(conditionalProbability.get(str)
						.toString());
				x3 = Double.parseDouble(pair.getValue().toString());
				// System.out.println(x3+" "+x1+" "+x2);
				value = value + (x1 * Math.log(x2));

			}
			// System.out.println(x3*value);
			// System.out.println(Math.log(x3)*Math.log(value));
			Double maxProbability = Double.NEGATIVE_INFINITY;
			// System.out.println("x3:"+Math.log(x3)+"Value:"+value);
			maxProbability = Math.log(x3) + value;
			finalProbability.put(pair.getKey().toString() + "|" + fileName,
					maxProbability);

		}
		// System.out.println(finalProbability);
		
		//We obtain the max value in the hashmap
		double maxValueInMap = (Collections.max(finalProbability.values()));
		//Iterate throught the hashmap and obtain the class name
		for (Entry<String, Double> entry : finalProbability.entrySet()) { 
			if (entry.getValue() == maxValueInMap) {
				// System.out.println(entry.getKey()); // Print the key with max
				// value
				// writer.println(entry.getKey());
				String tempWriter[] = entry.getKey().toString().split("\\|");
				writer.println(tempWriter[1] + " "
						+ classNamesHashMap.get(tempWriter[0]));
				// finalResult.put(tempWriter[1],
				// classNamesHashMap.get(tempWriter[0]));
			}
		}
		// System.out.println(finalResult);
	}

	
	//Method reads the file stopwords and stores it in a HashMap
	public Set<String> parseStopWords(String filename)
			throws FileNotFoundException {

		Scanner scanner = new Scanner(new File(filename));
		while (scanner.hasNext()) {
			stopWords.add(scanner.next());
		}
		scanner.close();
		return stopWords;
	}

}
