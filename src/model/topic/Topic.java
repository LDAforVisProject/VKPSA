package model.topic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.LDAConfiguration;
import javafx.util.Pair;

public class Topic
{
	private int topicNumber;
	private Map<String, Double> keywordProbabilityMap;
	private double log2;
	
	public Topic(int topicNumber)
	{
		this.topicNumber			= topicNumber;
		this.keywordProbabilityMap	= new HashMap<String, Double>(4056);
		this.log2					= Math.log(2);
	}
	
	public Topic(Topic source)
	{
		this.topicNumber			= source.topicNumber;
		this.keywordProbabilityMap	= new HashMap<String, Double>(source.keywordProbabilityMap);
		this.log2					= source.log2;
	}
	
	/**
	 * Add keyword data set to map.
	 * @param keywordDataset
	 * @return 1 if keyword dataset contains resonable content.
	 */
	public boolean addKeywordDataset(String keywordDataset)
	{
		// @todo: Add removal of quotation marks in preprocessing of data.
		// @todo: Add removal of commas in preprocessing of data.
		
		keywordDataset		= keywordDataset.replace("\"", "");
		int separatorIndex	= keywordDataset.indexOf('|');
		
		if (separatorIndex > -1) {
			String keyword		= keywordDataset.substring(0, separatorIndex);
			double probability	= Double.parseDouble( keywordDataset.substring(separatorIndex + 1, keywordDataset.length()) );
			keywordProbabilityMap.put(keyword, probability);
			
			return true;
		}
		
//		System.out.println("error: " + keywordDataset);
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		return false;
	}
	
	@Override
	public String toString()
	{
		String result = "Topic #" + topicNumber + "\n";
		        
		for (Map.Entry<String, Double> item : keywordProbabilityMap.entrySet()) { 
			result += result + item.getKey() + "|" + item.getValue() + "\n";
		}
				
		return result + "\n";
	}
	
	public int getNumberOfItems()
	{
		return keywordProbabilityMap.size();
	}

	public double calculateL2Distance(Topic topicToCompare)
	{
		// System.out.println("Calculating L2 distance/norm. Using topics #" + topicNumber + " and #" + topicToCompare.topicNumber + ".");
		
	    double result = 0;
	    // Assume all words are present in self._keywordProbabilityMap as well as topicToCompare._keywordProbabilityMap.
		for (Map.Entry<String, Double> item : keywordProbabilityMap.entrySet()) { 
			double diff	=  item.getValue() - topicToCompare.keywordProbabilityMap.get(item.getKey());
			result		+= result + diff * diff;
		}
		
	    return Math.sqrt(result);
	}

	public double calculateHellingerDistance(Topic topicToCompare)
	{
		// System.out.println("Calculating Hellinger distance/norm. Using topics #" + topicNumber + " and #" + topicToCompare.topicNumber + ".");
		
		
		double result = 0;
        // Assume all words are present in self._keywordProbabilityMap as well as objectToCompare._keywordProbabilityMap.
		for (Map.Entry<String, Double> item : keywordProbabilityMap.entrySet()) { 
			double temp	=  Math.sqrt(item.getValue()) - Math.sqrt(topicToCompare.keywordProbabilityMap.get(item.getKey()));
			result 		+= temp * temp;
		}
        
		return result / Math.sqrt(2);
	}

	public double calculateBhattacharyyaDistance(Topic topicToCompare)
	{
		// System.out.println("Calculating Bhattacharyya distance/norm. Using topics #" + topicNumber + " and #" + topicToCompare.topicNumber + ".");
		
		double result = 0;
        // Assume all words are present in self._keywordProbabilityMap as well as objectToCompare._keywordProbabilityMap.
		for (Map.Entry<String, Double> item : keywordProbabilityMap.entrySet()) {
            result += Math.sqrt(item.getValue() * topicToCompare.keywordProbabilityMap.get(item.getKey()));
		}
		
        return (Math.log(result) / log2) * (-1);
	}

	public double calculateKullbackLeiblerDistance(Topic topicToCompare)
	{
		// System.out.println("Calculating Kullback-Leibler distance/norm. Using topics #" + topicNumber + " and #" + topicToCompare.topicNumber + ".");
		
		double result = 0;

		// Assume all words are present in self._keywordProbabilityMap as well as objectToCompare._keywordProbabilityMap.
		for (Map.Entry<String, Double> item : keywordProbabilityMap.entrySet()) {
			double probability = item.getValue();
            result += probability * ( Math.log( probability / topicToCompare.keywordProbabilityMap.get(item.getKey()) ) / log2 );
		}
		
		return result;
	}

	public double calculateJensenShannonDivergence(Topic topicToCompare)
	{
		// System.out.println("Calculating Jensen-Shannon distance/norm. Using topics #" + topicNumber + " and #" + topicToCompare.topicNumber + ".");
		
        double tempSum_P = 0;
        double tempSum_Q = 0;
        
        // Assume all words are present in self._keywordProbabilityMap as well as objectToCompare._keywordProbabilityMap.
        for (Map.Entry<String, Double> item : keywordProbabilityMap.entrySet()) {
            // Value for "distribution" P (~ self) for this keyword
            double currentValue_P = item.getValue();
            // Value for "distribution" Q (~ objectToCompare) for this keyword
            double currentValue_Q = topicToCompare.keywordProbabilityMap.get(item.getKey());
            // Value for mixture "distribution" M for this keyword
            double currentValue_M = (currentValue_P + currentValue_Q) / 2;
            
            double log2_currentValue_M 	=  Math.log(currentValue_M) / log2;
            tempSum_P					+= currentValue_P * ( (Math.log(currentValue_P) / log2) - log2_currentValue_M );
            tempSum_Q 					+= currentValue_Q * ( (Math.log(currentValue_Q) / log2) - log2_currentValue_M );
        }
        
        // Calculate and return final results
        return 0.5 * (tempSum_P + tempSum_Q);
	}
	
	public Map<String, Double> getKeywordProbabilityMap()
	{
		return keywordProbabilityMap;
	}
	
	/**
	 * Reads one file / dataset and generates a list topics from it.
	 * @param directory
	 * @param filename
	 * @param topicKeywordAlignment
	 * @return Pair of (1) the LDA configuration of this file and (2) all topics
	 * (with the corresponding probability) contained in this file.
	 * @throws Exception 
	 */
	public static Pair<LDAConfiguration, ArrayList<Topic>> generateTopicsFromFile(String directory, String filename, TopicKeywordAlignment topicKeywordAlignment) throws Exception
	{
		// Init container for k topics
		ArrayList<Topic> topics = new ArrayList<Topic>();
		// i denotes number of rows in csv (up to number of features).
		int i					= 0;
		
		// Store indices of parameter value locations.
		Map<String, Integer> paramValueIndexMap	= new HashMap<String, Integer>();
		// Store parameter values.
		Map<String, Double> paramValueMap		= new HashMap<String, Double>();
		
		// Select path and charset.
	    Path path				= Paths.get(directory, filename);
	    Charset charset			= Charset.forName("UTF-8");
	    
//	    System.out.println("\n\nFILE = " + filename + "\n");
	    try {
			List<String> lines = Files.readAllLines(path, charset);
			
			// Get start parameter's start positions in configuration line (#0) in dataset.
			// Add new parameters to be processed here. Has to be in the form of "param1=x|param2=y|blub=z".
			paramValueIndexMap.put("k", lines.get(0).indexOf("k="));
			paramValueIndexMap.put("eta", lines.get(0).indexOf("eta="));
			paramValueIndexMap.put("alpha", lines.get(0).indexOf("alpha="));
			
			// Check if all parameters are available.
			for (String key : paramValueIndexMap.keySet()) {
				if (paramValueIndexMap.get(key) < 0) {
					throw new Exception("### ERROR ### No metadata found in dataset " + filename + "; @" + key);
				}
			}
			
			// ----------------------------------------------------------------
			// Read parameter values first, then keyword/probability pairs.
			// ----------------------------------------------------------------
			if (topicKeywordAlignment == TopicKeywordAlignment.HORIZONTAL) {
				// Length for respectively the identifier ("k", "alpha", ...) and the value ("33.4", "0.5", "20", ...).
				int substringLength = -1;
				int valueLength		= -1;
				int startIndex		= -1;
				int valueIndex		= -1;
				
				// Read parameter values.
				for (String key : paramValueIndexMap.keySet()) {
					substringLength = key.length();
					startIndex		= paramValueIndexMap.get(key);
					valueIndex		= startIndex + substringLength + 1;
					valueLength		= lines.get(0).indexOf("|", startIndex) > -1 ? lines.get(0).indexOf("|", startIndex) - valueIndex : lines.get(0).length() - valueIndex;
					
//					System.out.println("key = " + key + ", startIndex = " + startIndex + ", valueIndex = " + valueIndex + ", next | = " + lines.get(0).indexOf("|", startIndex) + ", valueLength = " + valueLength);
					paramValueMap.put(key, Double.parseDouble(lines.get(0).substring(valueIndex, valueIndex + valueLength)));
				}
				
				// Read keyword/probability pairs.
				for (String line : lines.subList(1, lines.size())) {
					topics.add(new Topic(i++));

					// Process all keyword/probability pairings in current line/topic.
					String[] keywordDatasets	= line.split(",");
					Topic currentTopic			= topics.get(topics.size() - 1);
					for (String keywordDataset : keywordDatasets) {
						currentTopic.addKeywordDataset(keywordDataset);
					}
				}

			}
		}
	    
	    // IOException? Check path.
	    catch (IOException e) {
	      System.out.println(e);
	    }
         
	    // Create LDA configuration.
	    LDAConfiguration ldaConfiguration = new LDAConfiguration( 	paramValueMap.get("k").intValue(), 
	    															paramValueMap.get("alpha"),
	    															paramValueMap.get("eta"));
//        System.out.println("@" + filename + "; LDAConfiguration: k = " + ldaConfiguration.getK() + ", alpha = " + ldaConfiguration.getAlpha() + ", eta = " + ldaConfiguration.getEta());
	    
        return new Pair<LDAConfiguration, ArrayList<Topic>>(ldaConfiguration, topics);
	}
}