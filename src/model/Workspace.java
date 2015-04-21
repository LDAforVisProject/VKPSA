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
import java.util.Set;

import org.apache.commons.collections4.MultiMap;

import javafx.util.Pair;
import mdsj.Data;
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
	 * Holds all LDA configurations found in workspace.
	 */
	private ArrayList<LDAConfiguration> ldaConfigurations;
	/**
	 * Contains all LDA configurations (alias datasets) that
	 * are assigned this MDS / global scatterplot coordinate.
	 */
	private MultiMap<Pair<Double, Double>, LDAConfiguration> reverseMDSCoordinateLookup;
	
	
	public Workspace(String directory)
	{
		this.directory			= directory;
		this.datasetMap			= new HashMap<LDAConfiguration, Dataset>();
		this.ldaConfigurations	= new ArrayList<LDAConfiguration>();
		
//		this.reverseMDSCoordinateLookup = new MultiMap<Pair<Double, Double>, LDAConfiguration>();
		
		// Load specified directory.
		double[][] output = null;
//		executeWorkspaceAction(WorkspaceAction.ALL);
	}
	
	/**
	 * Executes workspace actions (such as "load files in directory", "generate new files",
	 * "calculate distances" etc.).
	 * @param workspaceAction
	 * @return Boolean indicating whether action was successfully executed or not.
	 */
	public double[][] executeWorkspaceAction(WorkspaceAction workspaceAction)
	{
		int numberOfDatasets		= -1;
		double[][] distances		= null;
		double[][] mdsCoordinates	= null;
		double[][] result			= null;
		
		switch (workspaceAction) {
			case LOAD_RAW_DATA:
				// Load datasets.
				numberOfDatasets = loadDatasets();
			break;
			
			case LOAD_DISTANCES:
			break;
			
			case LOAD_MDS_COORDINATES:
				// @todo Other sections (besides "ALL"), test dynamic loading.
			break;	
			
			case ALL:
				// Load datasets.
				System.out.println("Loading");
				numberOfDatasets	= loadDatasets();
				// Calculate distances.
				System.out.println("Distances");
				distances			= calculateDistances(numberOfDatasets, false);
				// Calculate MDS coordinates.
				System.out.println("MDSCoordinates");
				mdsCoordinates		= calculateMDSCoordinates(distances, true, "src/data/testdata.txt");
				result				= mdsCoordinates;
			break;		
		}
		
		return result;
	}
	
	/**
	 * Loads datasets from specified directory. Uses the value
	 * specified in @Workspace#directory.
	 * @return The number of found datasets.
	 */
	private int loadDatasets()
	{
		// @todo CURRENT: Load files dynamically. Modify python script so that
		// generated files contain metadata in first line.
		String[] filenames		= new File(directory).list();
		int numberOfDatasets	= 0;
				
		// Iterate through files.
		for (String filename : filenames) {
			// If .csv: Process next file.
			try {
				if (filename.endsWith(".csv")) {
					numberOfDatasets++;
					
					Pair<LDAConfiguration, ArrayList<Topic>> topicsetTuple = Topic.generateTopicsFromFile(directory, filename, TopicKeywordAlignment.HORIZONTAL);
					ldaConfigurations.add(topicsetTuple.getKey());
					datasetMap.put( topicsetTuple.getKey(), new Dataset(topicsetTuple.getKey(), topicsetTuple.getValue()) );
				}
			}
			
			catch (Exception e) {
				System.out.println("Exception: ");
				e.printStackTrace();
			}
		}
		
		return numberOfDatasets;
	}
	
	private double[][] calculateDistances(int numberOfDatasets, boolean writeToFile)
	{
		double distances[][]						= new double[datasetMap.size()][datasetMap.size()];
		
		// Compare all datasets with each other.
		for (int i = 0; i < ldaConfigurations.size(); i++) {
			distances[i][i] = 0;
			
			for (int j = i + 1; j < ldaConfigurations.size(); j++) {
				System.out.println(i + " to " + j);
				
				// Assume symmetric distance calculations.
				distances[i][j] = datasetMap.get(ldaConfigurations.get(i)).calculateDatasetDistance(datasetMap.get(ldaConfigurations.get(j)), DatasetDistance.HausdorffDistance);
				distances[j][i] = distances[i][j];
				
				System.out.println(distances[i][j] + " " + distances[j][i]);
			}	
		}
		
		// @todo Implement mechanism for writing to file.
		if (writeToFile) {
		}
		
		return distances;
	}
	
	private double[][] calculateMDSCoordinates(double[][] distances, boolean writeToFile, String path)
	{
		// Storage location for MDS coordinates.
		double[][] output = new double[datasetMap.size()][datasetMap.size()];
		
		// Execute MDS on topic distance matrix.
		output = MDSJ.classicalScaling(distances, 2);
		System.out.println(Data.format(output));
		// Write to file.
		if (writeToFile) {
			try {
				PrintWriter writer = new PrintWriter(path, "UTF-8");
				
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
			}
			
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return output;
	}

	private double[][] loadMDSCoordinates(String directory, String filename)
	{
		double[][] output	= null;
		Path path			= Paths.get(directory, filename);
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
		
		return output;
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
			
			try {
				for (double eta : etaValues) {
					for (double alpha : alphaValues) {
						System.out.println("Reading file with alpha = " + alpha + ", eta = " + eta);
						
						paramSetList.add(new LDAConfiguration(20, alpha, eta));
						String etaString	= eta >= 1		? String.valueOf( (int) eta )	: String.valueOf(eta);
						String alphaString	= alpha >= 1	? String.valueOf( (int) alpha)	: String.valueOf(alpha);
						
						Pair<LDAConfiguration, ArrayList<Topic>> res	= Topic.generateTopicsFromFile(directory, "LDATopics_eta" + etaString + "_alpha" + alphaString + ".csv", TopicKeywordAlignment.HORIZONTAL);
						ArrayList<Topic> topicList						= res.getValue();
						
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
			
			catch (Exception e) {
				System.out.println(e);
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
