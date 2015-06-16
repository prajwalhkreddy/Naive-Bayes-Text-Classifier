

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class CalculateAccuracy {

	/*The program takes 2 input parameters, the output file generated and the dev_label.txt
	 * The output files and the dev_labels file are placed in the root directory*/
	
	static String outputFile = "";  
	static String devLabelsFile = "";
	static HashMap<String, Integer> outputFileHashMap = new HashMap<String, Integer>();
	static HashMap<String, Integer> devLabelsHashMap = new HashMap<String, Integer>();

	public static void main(String[] args) {
		CalculateAccuracy ca = new CalculateAccuracy();
		outputFile = args[0];
		devLabelsFile = args[1];

		ca.readFiles();
		ca.calc();

	}

	
	/*This method reads both the input files and puts it to a hashmap*/
	void readFiles() {
		try {
			Scanner in1 = new Scanner(new FileReader(outputFile));
			Scanner in2 = new Scanner(new FileReader(devLabelsFile));

			while (in1.hasNextLine()) {
				String temp2[] = in1.nextLine().split(" ");
				//Reads from the output file and puts it to a Hashmap
				outputFileHashMap.put(temp2[0], Integer.parseInt(temp2[1])); 
				// outputFileHashMap.put(Integer.parseInt(temp2[1]), myList1);

			}
			//System.out.println(outputFileHashMap);

			while (in2.hasNextLine()) {

				// System.out.println(in1.nextLine());
				String temp2[] = in2.nextLine().split(" ");
				
				//Reads from the dev_labels file and puts it to a Hashmap
				devLabelsHashMap.put(temp2[0], Integer.parseInt(temp2[1])); 
			}

			//System.out.println(devLabelsHashMap);
			in1.close();
			in2.close();

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

	}

	public void calc() {
		
		double accuracy = 0;
		int count = 0;
		for (HashMap.Entry<String, Integer> entry : outputFileHashMap.entrySet()) {
			String key = entry.getKey();
			int value = entry.getValue();
			
			if (devLabelsHashMap.get(key).equals(value)) {
				count++;
			}

		}
		if(count!=0)
		accuracy= ((double) count/(double) outputFileHashMap.size())*100;
		System.out.println(count);
		System.out.println("Accuracy:"+accuracy+"%");

	}
}
