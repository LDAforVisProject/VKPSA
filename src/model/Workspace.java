package model;

import java.io.File;
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

import org.apache.commons.collections4.MultiMap;

import javafx.util.Pair;
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
	/**
	 * Current directory.
	 */
	private String directory;
	/**
	 * Holds all (loaded) datasets from the specified
	 * directory.
	 */
	private Map<LDAConfiguration, Dataset> datasetMap;
	/**
	 * Contains all LDA configurations (alias datasets) that
	 * are assigned this MDS / global scatterplot coordinate.
	 */
	private MultiMap<Pair<Double, Double>, LDAConfiguration> reverseMDSCoordinateLookup;
	
	
	
	public Workspace(String directory)
	{
		this.directory = directory;
		loadDatasets();
	}
	
	/**
	 * Loads datasets from specified directory.
	 * @return The number of found datasets.
	 */
	private int loadDatasets()
	{
		// @todo CURRENT: Load files dynamically. Modify python script so that
		// generated files contain metadata in first line.
		String[] filenames		= new File(directory).list();
		int numberOfDatasets	= 0;
		
		for (String filename : filenames) {
			if (filename.endsWith(".csv")) {
				numberOfDatasets++;
				
				
			}
		}
		
		return numberOfDatasets;
	}
	
	public double[][] test_sampleData(boolean useExistingData)
	{
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
					distances[i][j] = datasetMap.get(paramSetList.get(i)).calculateDatasetDistance(datasetMap.get(paramSetList.get(j)), DatasetDistance.HausdorffDistance);
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
