package model;

import java.io.IOException;
import java.io.PrintWriter;
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
 * Encompasses:
 * 	- Name and path of specified directory
 *  - Datasets in this directory
 *  - Distance files and other metadata in this directory
 *  - Administration of processes revolving around the mentioned items
 *  
 * @author RM
 * @date 2015-04-14
 */
public class Workspace
{
	private String directory;
	
	public Workspace(String directory)
	{
		this.directory = directory;
	}
	
	// @todo Un-statify.
	public static double calculateDatasetDistance(final Dataset dataset1, final Dataset dataset2, DatasetDistance distanceType)
	{
		double distance = 0;
		
		switch (distanceType) {
			case MinimalDistance:
				distance = (Workspace.calculateMinimalDatasetDistance(dataset1, dataset2) + Workspace.calculateMinimalDatasetDistance(dataset2, dataset1)) / 2;
			break;
			
			case HausdorffDistance:
				distance = (Workspace.calculateHausdorffDatasetDistance(dataset1, dataset2) + Workspace.calculateHausdorffDatasetDistance(dataset2, dataset1)) / 2;
			break;
			
			default:
				System.out.println("Invalid dataset distance type specified: " + distanceType.toString() + " is unknown.");
		}
		
		return distance;
	}
	
	/**
	 * @todo Add relaxed/strict enum parameter to distuingish whether the same topic may be used for
	 * multiple other topics.
	 * Calculates distance between two datasets using the average of all minimal distances between
	 * one topic of one dataset and all topics of the other Workspace. 
	 * @param dataset1
	 * @param dataset2
	 * @return
	 */
	private static double calculateMinimalDatasetDistance(final Dataset dataset1, final Dataset dataset2)
	{
		System.out.println("Calculating dataset distance");
		
		double minDistance	= Double.MAX_VALUE;
		Topic currentTopic	= null;
		
		ArrayList<Topic> topics1 = dataset1.getTopics();
		ArrayList<Topic> topics2 = dataset2.getTopics();
		
		// Iterate through all topics of one dataset, compare each of them with all topics of the other dataset, pick minimal distance, sum up distances.
		for (int i = 0; i < topics1.size(); i++) {
			currentTopic = topics1.get(i);
			
			// Unrolled loop to avoid calculation of distance i to i without using an if.
			for (int j = 0; j < topics2.size(); j++) {
				double distance = currentTopic.calculateBhattacharyyaDistance(topics2.get(j)); 
				minDistance = minDistance > distance ? distance : minDistance;
			}	
		}
		
		// Return normalized distance.
		return minDistance / (topics1.size() * topics2.size());
	}
	
	/**
	 * @todo Add relaxed/strict enum parameter to distuingish whether the same topic may be used for
	 * multiple other topics.
	 * Calculates distance between two datasets using the Hausdorff distance.
	 * @param dataset1
	 * @param dataset2
	 * @return
	 */
	private static double calculateHausdorffDatasetDistance(final Dataset dataset1, final Dataset dataset2)
	{
		System.out.println("Calculating dataset distance");
		
		double minDistance		= Double.MAX_VALUE;
		double maxMinDistance	= 0;
		Topic currentTopic		= null;
		
		ArrayList<Topic> topics1 = dataset1.getTopics();
		ArrayList<Topic> topics2 = dataset2.getTopics();
		
		// Iterate through all topics of one dataset, compare each of them with all topics of the other dataset, pick minimal distance, sum up distances.
		for (int i = 0; i < topics1.size(); i++) {
			currentTopic 	= topics1.get(i);
			minDistance		= Double.MAX_VALUE;
			
			// Unrolled loop to avoid calculation of distance i to i without using an if.
			for (int j = 0; j < topics2.size(); j++) {
				double distance = currentTopic.calculateBhattacharyyaDistance(topics2.get(j)); 
				minDistance = minDistance > distance ? distance : minDistance;
			}
			
			maxMinDistance = maxMinDistance < minDistance ? minDistance : maxMinDistance;
		}
		
		// Return normalized distance.
		return maxMinDistance;
	}
	
	public double[][] test_sampleData(boolean useExistingData)
	{
		// @todo Get number of datasets dynamically.
		final int numberOfDatasets							= 25;
		Map<LDAConfiguration, Dataset> datasetMap			= new HashMap<LDAConfiguration, Dataset>(numberOfDatasets);
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
					
					ArrayList<Topic> topicList = Topic.generateTopicsFromFile(directory, "LDATopics_eta" + etaString + "_alpha" + alphaString + ".csv", TopicKeywordAlignment.HORIZONTAL);
					datasetMap.put(paramSetList.get(paramSetList.size() - 1), new Dataset(paramSetList.get(paramSetList.size() - 1), topicList));
					
	//				datasetList.add(new Dataset(new LDAConfiguration(20, alpha, eta), topicList));
	//				ArrayList<Topic> topics = Topic.generateTopicsFromFile("D:\\Workspace\\LDA\\core\\data\\sampling", "LDATopics_eta" + etaString + "_alpha" + alphaString + ".csv", TopicKeywordAlignment.HORIZONTAL);
	//				Dataset dataset = new Dataset(new LDAConfiguration(20, alpha, eta));
	//				Workspace.setTopics(topics);
				}
			}
			
		
			double distances[][] = new double[paramSetList.size()][paramSetList.size()];
			// Compare all datasets with each other.
			for (int i = 0; i < paramSetList.size(); i++) {
				for (int j = i + 1; j < paramSetList.size(); j++) {
					System.out.println(i + " to " + j);
					
					// Assume symmetric distance calculations.
					distances[i][j] = Workspace.calculateDatasetDistance( datasetMap.get(paramSetList.get(i)), datasetMap.get(paramSetList.get(j)), DatasetDistance.HausdorffDistance );
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
		    double max = Double.MIN_VALUE;
		    double min = Double.MAX_VALUE;
		    
			try {
				List<String> lines	= Files.readAllLines(path, charset);
				
				for (String line : lines) {
					String[] coordinates = line.split(" ");
					
					if (output == null)
						output = new double[2][coordinates.length];
					
					for (String coordinate : coordinates) {
						output[lineCount][coordinateCount] = Double.parseDouble(coordinate);
						
						max = output[lineCount][coordinateCount] > max ? output[lineCount][coordinateCount] : max;
						min = output[lineCount][coordinateCount] < min ? output[lineCount][coordinateCount] : min;
						
						coordinateCount++;
					}
					
					System.out.println("max = " + max + ", min = " + min);
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

	// ------------------------------
	// From here: Getter and setter.
	// ------------------------------
	
	public String getDirectory()
	{
		return directory;
	}

	public void setDirectory(String directory)
	{
		this.directory = directory;
	}
}
