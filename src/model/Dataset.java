package model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mdsj.MDSJ;

/**
 * One instance of dataset corresponds to the output of a execution of the LDA
 * algorithm with one specific set of parameters.
 * @author RM
 *
 */
public class Dataset
{
	private LDAConfiguration parametrization;
	private ArrayList<Topic> topics;
	
	public Dataset(final LDAConfiguration parametrization)
	{
		this.parametrization	= parametrization;
		this.topics				= new ArrayList<Topic>();
	}
	
	public Dataset(final LDAConfiguration parametrization, final ArrayList<Topic> topics)
	{
		this.parametrization	= parametrization;
		this.topics				= new ArrayList<Topic>(topics);
	}
	
	public void setTopics(final ArrayList<Topic> topics)
	{
		this.topics.clear();
		this.topics.addAll(topics);
	}
	
	public ArrayList<Topic> getTopics()
	{
		return topics;
	}
	
	// @todo Un-statify.
	public static double calculateDatasetDistance(final ArrayList<Topic> dataset1, final ArrayList<Topic> dataset2, DatasetDistance distanceType)
	{
		double distance = 0;
		
		switch (distanceType) {
			case MinimalDistance:
				distance = (Dataset.calculateMinimalDatasetDistance(dataset1, dataset2) + Dataset.calculateMinimalDatasetDistance(dataset2, dataset1)) / 2;
			break;
			
			case HausdorffDistance:
				distance = (Dataset.calculateHausdorffDatasetDistance(dataset1, dataset2) + Dataset.calculateHausdorffDatasetDistance(dataset2, dataset1)) / 2;
			break;
			
			default:
				System.out.println("Invalid dataset distance type specified: " + distanceType.toString() + " is unknown.");
		}
		
		return distance;
	}
	
	private static double calculateMinimalDatasetDistance(final ArrayList<Topic> dataset1, final ArrayList<Topic> dataset2)
	{
		System.out.println("Calculating dataset distance");
		
		double minDistance	= Double.MAX_VALUE;
		Topic currentTopic	= null;
		
		// Iterate through all topics of one dataset, compare each of them with all topics of the other dataset, pick minimal distance, sum up distances.
		for (int i = 0; i < dataset1.size(); i++) {
			currentTopic = dataset1.get(i);
			
			// Unrolled loop to avoid calculation of distance i to i without using an if.
			for (int j = 0; j < dataset2.size(); j++) {
				double distance = currentTopic.calculateBhattacharyyaDistance(dataset2.get(j)); 
				minDistance = minDistance > distance ? distance : minDistance;
			}	
		}
		
		// Return normalized distance.
		return minDistance / (dataset1.size() * dataset2.size());
	}
	
	private static double calculateHausdorffDatasetDistance(final ArrayList<Topic> dataset1, final ArrayList<Topic> dataset2)
	{
		System.out.println("Calculating dataset distance");
		
		double minDistance		= Double.MAX_VALUE;
		double maxMinDistance	= 0;
		Topic currentTopic		= null;
		
		// Iterate through all topics of one dataset, compare each of them with all topics of the other dataset, pick minimal distance, sum up distances.
		for (int i = 0; i < dataset1.size(); i++) {
			currentTopic 	= dataset1.get(i);
			minDistance		= Double.MAX_VALUE;
			
			// Unrolled loop to avoid calculation of distance i to i without using an if.
			for (int j = 0; j < dataset2.size(); j++) {
				double distance = currentTopic.calculateBhattacharyyaDistance(dataset2.get(j)); 
				minDistance = minDistance > distance ? distance : minDistance;
			}
			
			maxMinDistance = maxMinDistance < minDistance ? minDistance : maxMinDistance;
		}
		
		// Return normalized distance.
		return maxMinDistance;
	}
	
	public static double[][] sampleTestData(boolean useExistingData)
	{
		// @todo Get number of datasets dynamically.
		final int numberOfDatasets							= 25;
		Map<LDAConfiguration, ArrayList<Topic>> datasetMap	= new HashMap<LDAConfiguration, ArrayList<Topic>>(numberOfDatasets);
		ArrayList<LDAConfiguration> paramSetList			= new ArrayList<LDAConfiguration>(numberOfDatasets);
		double[][] output									= null;
//		ArrayList<Dataset> datasetList						= new ArrayList<Dataset>();
//		datasetList.ensureCapacity(25);
		
		if (!useExistingData) {
			final double[] etaValues	= {0.1, 0.5, 1, 5, 10};
			final double[] alphaValues	= {0.1, 0.5, 1, 5, 10};
			for (double eta : etaValues) {
				for (double alpha : alphaValues) {
					System.out.println("Reading file with alpha = " + alpha + ", eta = " + eta);
					
					paramSetList.add(new LDAConfiguration(20, alpha, eta));
					String etaString	= eta >= 1		? String.valueOf( (int) eta )	: String.valueOf(eta);
					String alphaString	= alpha >= 1	? String.valueOf( (int) alpha)	: String.valueOf(alpha);
					
					ArrayList<Topic> topicList = Topic.generateTopicsFromFile("D:\\Workspace\\LDA\\core\\data\\sampling", "LDATopics_eta" + etaString + "_alpha" + alphaString + ".csv", TopicKeywordAlignment.HORIZONTAL);
					datasetMap.put(paramSetList.get(paramSetList.size() - 1), topicList);
					
	//				datasetList.add(new Dataset(new LDAConfiguration(20, alpha, eta), topicList));
	//				ArrayList<Topic> topics = Topic.generateTopicsFromFile("D:\\Workspace\\LDA\\core\\data\\sampling", "LDATopics_eta" + etaString + "_alpha" + alphaString + ".csv", TopicKeywordAlignment.HORIZONTAL);
	//				Dataset dataset = new Dataset(new LDAConfiguration(20, alpha, eta));
	//				dataset.setTopics(topics);
				}
			}
			
		
			double distances[][] = new double[paramSetList.size()][paramSetList.size()];
			// Compare all datasets with each other.
			for (int i = 0; i < paramSetList.size(); i++) {
				for (int j = i + 1; j < paramSetList.size(); j++) {
					System.out.println(i + " to " + j);
					
					// Assume symmetric distance calculations.
					//distances[i][j] = Dataset.calculateHausdorffDatasetDistance(datasetList.get(i).getTopics(), datasetList.get(j).getTopics());
					//distances[i][j] = Dataset.calculateHausdorffDatasetDistance(datasetMap.get(paramSetList.get(i)), datasetMap.get(paramSetList.get(j)));
					distances[i][j] = Dataset.calculateDatasetDistance( datasetMap.get(paramSetList.get(i)), datasetMap.get(paramSetList.get(j)), DatasetDistance.HausdorffDistance );
					distances[j][i] = distances[i][j];
				}	
			}
			
			// Execute MDS on topic distance matrix.
			PrintWriter writer;
			try {
				writer = new PrintWriter("src/data/testdata.txt", "UTF-8");
				
				output = MDSJ.classicalScaling(distances);
				System.out.println("1: " + output.length);
				System.out.println("2: " + output[0].length);
				for (int i = 0; i < output.length; i++) {
					for (int j = 0; j < output[i].length; j++) {
						writer.print(output[i][j] + " ");
					}
					
					if (i < output.length - 1)
						writer.println("");
				}
				
				writer.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		else {
			Path path			= Paths.get("src/data", "testdata.txt");
		    Charset charset		= Charset.forName("UTF-8");
		    int lineCount		= 0;
		    int coordinateCount	= 0;
		    
			try {
				List<String> lines	= Files.readAllLines(path, charset);
				
				for (String line : lines) {
					String[] coordinates = line.split(" ");
					
					if (output == null)
						output = new double[2][coordinates.length];
					
					for (String coordinate : coordinates) {
						output[lineCount][coordinateCount] = Double.parseDouble(coordinate); 
						coordinateCount++;
					}
					
					lineCount++;
					coordinateCount	= 0;
				}
			} 
			
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return output;
	}
}
