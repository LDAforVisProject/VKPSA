package model.workspace;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MultiMap;

import javafx.beans.property.DoubleProperty;
import javafx.util.Pair;
import mdsj.Data;
import mdsj.MDSJ;
import model.LDAConfiguration;
import model.workspace.tasks.ITaskListener;
import model.workspace.tasks.Task_CalculateDistances;
import model.workspace.tasks.Task_CalculateMDSCoordinates;
import model.workspace.tasks.Task_LoadDistanceData;
import model.workspace.tasks.Task_LoadMDSCoordinates;
import model.workspace.tasks.Task_LoadRawData;
import model.workspace.tasks.Task_WorkspaceTask;

// -----------------------------------------------
// 		ROADMAP / Todos, in sequential order
// -----------------------------------------------

// @todox Write distance calculation task.
// @todox Then write raw data loading task.
// @todox Then write results to file.
// @todox Write distance data loading task.
// @todo CURRENT: Rewrite MDS coordinate calculation/outputting - output LDA configuration binding too (which coordinate belongs to which LDA configuration?).
// @todo Then: Adapt reading methods to new file structures.
// @todo Then: Complement workspace integrity check (are .dis and .mds and datasets consistent in terms of the number of datasets and which datasets they contain/index?). 
// @todo Then: Testdrive.
// @todo Test and augment program workflow.
// @todo Create data generation view.
// @todo Improve LDA python script (fixes, parameter output in files). Modify so that generated files contain metadata in first line.
	// @todo: Add removal of quotation marks in preprocessing of data.
	// @todo: Add removal of commas in preprocessing of data.
// @todo Integrate python script binding in VKPSA GUI.
// @todo Test data generation.
// @todo Formulate work items for analysis view / phase.

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
	// -----------------------------------------------
	// 				Path variables
	// -----------------------------------------------
	
	/**
	 * Current directory.
	 */
	private String directory;
	/**
	 * Name of file containing already calculated MDS coordinates.
	 */
	public static final String FILENAME_DISTANCES = "workspace.dis";	
	/**
	 * Name of file containing already calculated MDS coordinates.
	 */
	public static final String FILENAME_MDSCOORDINATES = "workspace.mds";
	
	
	// -----------------------------------------------
	// 		Actual (raw and (pre-)processed) data
	// -----------------------------------------------
	
	/**
	 * Contains pre-calculated MDS coordinates. Read from .FILENAME_MDSCOORDINATES.
	 */
	private double[][] mdsCoordinates;
	/**
	 * Contains pre-calculated distance values (between datasets). Read from .FILENAME_DISTANCES.
	 */
	private double[][] distances;
	
	/**
	 * Holds all (loaded) datasets from the specified directory.
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
	
	/**
	 * Number of MDS coordinates in in this workspace's .mds file. Used to check workspace integrity.
	 */
	private int numberOfDatasetsInMDSFile;
	/**
	 * Number of datasets in workspace. Used to check workspace integrity.
	 */
	private int numberOfDatasetsInWS;
	/**
	 * Number of datasets/LDA configurations in in this workspace's .dis file. Used to check workspace integrity.
	 */
	private int numberOfDatasetsInDISFile;
	
	// -----------------------------------------------
	// 		Auxiliary options and flags
	// -----------------------------------------------	
	
	/**
	 * Define whether or not calculated distances are appended to an existing
	 * distance matrix or if the entire matrix is to be calculated anew.
	 */
	private boolean appendToDistanceMatrix;
	/**
	 * Define whether or not calculated MDS coordinates are appended to an existing
	 * MDS coordinate matrix or if the entire matrix is to be calculated anew.
	 */
	private boolean appendToMDSCoordinateMatrix;

	/**
	 * Indicates whether raw topic data is loaded or not.
	 */
	private boolean isRawDataLoaded;
	/**
	 * Indicates whether distance data is loaded or not.
	 */
	private boolean isDistanceDataLoaded;
	/**
	 * Indicates whether MDS coordinates are loaded or not.
	 */
	private boolean isMDSDataLoaded;
	
	
	
	// -----------------------------------------------	
	// -----------------------------------------------
	// 					Methods
	// -----------------------------------------------
	// -----------------------------------------------	
	
	
	public Workspace(String directory)
	{
		this.directory			= directory;
		this.datasetMap			= new HashMap<LDAConfiguration, Dataset>();
		this.ldaConfigurations	= new ArrayList<LDAConfiguration>();
		
//		this.reverseMDSCoordinateLookup = new MultiMap<Pair<Double, Double>, LDAConfiguration>();
		
		// Set initial values for various flags.
		
		appendToDistanceMatrix			= false;
		appendToMDSCoordinateMatrix		= false;
		
		isRawDataLoaded					= false;
		isDistanceDataLoaded			= false;
		isMDSDataLoaded					= false;
	}
	
	/**
	 * Executes workspace actions (such as "load files in directory", "generate new files",
	 * "calculate distances" etc.).
	 * @param workspaceAction
	 * @param progressProperty Property to bind to task progress (used for ProgressIndicators and ProgressBars).
	 * @param listener IThreadCompleteListener to be notified when thread has finished. 
	 * @return Task processing the request.
	 */
	public Task_WorkspaceTask executeWorkspaceAction(WorkspaceAction workspaceAction, DoubleProperty progressProperty, ITaskListener listener)
	{
		Task_WorkspaceTask task	= null;
		
		switch (workspaceAction) {
			// -----------------------------------------------
			// 				Loading operations
			// -----------------------------------------------
			
			case LOAD_RAW_DATA:
				// Load datasets.
				isRawDataLoaded 		= false;
				task					= new Task_LoadRawData(this, WorkspaceAction.LOAD_RAW_DATA);
			break;
			
			case LOAD_DISTANCES:
				isDistanceDataLoaded	= false;
				task					= new Task_LoadDistanceData(this, WorkspaceAction.LOAD_DISTANCES);
			break;
			
			case LOAD_MDS_COORDINATES:
				// Load MDS coordinates from file.
				isMDSDataLoaded 		= false;
				task					= new Task_LoadMDSCoordinates(this, WorkspaceAction.LOAD_MDS_COORDINATES);
			break;
			
			// -----------------------------------------------
			// 		Calculation / writing operations
			// -----------------------------------------------
			
			case CALCULATE_DISTANCES:
				task = new Task_CalculateDistances(this, WorkspaceAction.CALCULATE_DISTANCES);
			break;
			
			case CALCULATE_MDS_COORDINATES:
				task = new Task_CalculateMDSCoordinates(this, WorkspaceAction.CALCULATE_MDS_COORDINATES);
			break;
			
			// -----------------------------------------------
			// 			Other operations
			// -----------------------------------------------
			
			case RESET:
				directory = "";
				datasetMap.clear();
				ldaConfigurations.clear();
//				reverseMDSCoordinateLookup.clear();
			break;
			
			case SWITCH_DIRECTORY:
				datasetMap.clear();
				ldaConfigurations.clear();
//				reverseMDSCoordinateLookup.clear();
				
				// Load distance data.
				// @todo Load distance data after choosing new directory.
				// Load MDS coordinates.
				executeWorkspaceAction(WorkspaceAction.LOAD_MDS_COORDINATES, progressProperty, listener);
			break;
			
			case ALL:
				/*
				// Load datasets.
				System.out.println("Loading");
				numberOfDatasets	= loadDatasets();
				// Calculate distances.
				System.out.println("Distances");
				distances			= calculateDistances(numberOfDatasets, false);
				// Calculate MDS coordinates.
				System.out.println("MDSCoordinates");
				mdsCoordinates		= calculateMDSCoordinates(distances, true, directory + "\\" + FILENAME_MDSCOORDINATES);
				*/
			break;
			
			case NONE:
			break;
			
			default:
				System.out.println("ERROR: Non-implemented workspace action " + workspaceAction);
		}
		
		// Add listener to task, then start it.
		if (task != null) {
			task.addListener(listener);
			if (progressProperty != null) {
				progressProperty.bind(task.progressProperty());
			}
			
			new Thread(task).start();
		}
		
		return task;
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
	
	/**
	 * Returns number of .csv files in this directory.
	 * Does not check whether this .csv is actually an appropriate file.
	 * @return Number of .csv files in this directory.
	 */
	public int readNumberOfDatasets()
	{
		numberOfDatasetsInWS = 0;
		
		for(File f : new File(directory).listFiles()) {
			String filePath			= f.getAbsolutePath();
			String fileExtension	= filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length());
			
			numberOfDatasetsInWS	= "csv".equals(fileExtension) ? numberOfDatasetsInWS + 1 : numberOfDatasetsInWS;
		}
		
		return numberOfDatasetsInWS;
	}
	
	/**
	 * Checks if .mds file exists.
	 * @return
	 */
	public boolean containsMDSFile()
	{
		return Files.exists(Paths.get(directory, Workspace.FILENAME_MDSCOORDINATES));
	}
	
	/**
	 * Checks if .dis file exists.
	 * @return
	 */
	public boolean containsDISFile()
	{
		return Files.exists(Paths.get(directory, Workspace.FILENAME_DISTANCES));
	}
	
	/**
	 * Checks whether or not the .dis and .mds files are consistent
	 * with the data files contained in the workspace.
	 * @return Flag indicating integrity of metadata/consistency of metadata and data in this workspace. True if workspace is consistent.
	 */
	public boolean checkMetadataIntegrity()
	{
		return 	(getNumberOfDatasetsInWS() == getNumberOfDatasetsInMDSFile()); //&& 
//				isDISFileConistentWithDatasets());
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
	
	public int getNumberOfDatasetsInMDSFile()
	{
		return numberOfDatasetsInMDSFile;
	}
	
	public int getNumberOfDatasetsInWS()
	{
		return readNumberOfDatasets();
	}
	
	public double[][] getMDSCoordinates()
	{
		return mdsCoordinates;
	}

	public void setMDSCoordinates(double[][] mdsCoordinates)
	{
		this.mdsCoordinates = mdsCoordinates;
	}

	public void setNumberOfDatasetsInMDSFile(int numberOfMDSCoordinatesInWS)
	{
		this.numberOfDatasetsInMDSFile = numberOfMDSCoordinatesInWS;
	}

	public void setNumberOfDatasetsInWS(int numberOfDatasetsInWS)
	{
		this.numberOfDatasetsInWS = numberOfDatasetsInWS;
	}

	public Map<LDAConfiguration, Dataset> getDatasetMap()
	{
		return datasetMap;
	}

	public void setDatasetMap(Map<LDAConfiguration, Dataset> datasetMap)
	{
		this.datasetMap = datasetMap;
	}

	public ArrayList<LDAConfiguration> getLDAConfigurations()
	{
		return ldaConfigurations;
	}

	public void setLDAConfigurations(ArrayList<LDAConfiguration> ldaConfigurations)
	{
		this.ldaConfigurations = ldaConfigurations;
	}

	public boolean isAppendToDistanceMatrix()
	{
		return appendToDistanceMatrix;
	}

	public void setAppendToDistanceMatrix(boolean appendToDistanceMatrix)
	{
		this.appendToDistanceMatrix = appendToDistanceMatrix;
	}

	public boolean isAppendToMDSCoordinateMatrix()
	{
		return appendToMDSCoordinateMatrix;
	}

	public void setAppendToMDSCoordinateMatrix(boolean appendToMDSCoordinateMatrix)
	{
		this.appendToMDSCoordinateMatrix = appendToMDSCoordinateMatrix;
	}

	public double[][] getDistances()
	{
		return distances;
	}

	public void setDistances(double[][] distances)
	{
		this.distances = distances;
	}

	public boolean isRawDataLoaded()
	{
		return isRawDataLoaded;
	}

	public void setRawDataLoaded(boolean isRawDataLoaded)
	{
		this.isRawDataLoaded = isRawDataLoaded;
	}

	public boolean isDistanceDataLoaded()
	{
		return isDistanceDataLoaded;
	}

	public void setDistanceDataLoaded(boolean isDistanceDataLoaded)
	{
		this.isDistanceDataLoaded = isDistanceDataLoaded;
	}

	public boolean isMDSDataLoaded()
	{
		return isMDSDataLoaded;
	}

	public void setMDSDataLoaded(boolean isMDSDataLoaded)
	{
		this.isMDSDataLoaded = isMDSDataLoaded;
	}

	public int getNumberOfDatasetsInDISFile()
	{
		return numberOfDatasetsInDISFile;
	}

	public void setNumberOfDatasetsInDISFile(int numberOfDatasetsInDISFile)
	{
		this.numberOfDatasetsInDISFile = numberOfDatasetsInDISFile;
	}
}
