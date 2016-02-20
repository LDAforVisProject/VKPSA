package model.workspace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;





import database.DBManagement;
import javafx.beans.property.DoubleProperty;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.util.Pair;
import model.LDAConfiguration;
import model.workspace.tasks.ITaskListener;
import model.workspace.tasks.Task_CalculateDistances;
import model.workspace.tasks.Task_CalculateMDSCoordinates;
import model.workspace.tasks.Task_CollectMetadata;
import model.workspace.tasks.Task_GenerateData;
import model.workspace.tasks.Task_GenerateParameterList;
import model.workspace.tasks.Task_LoadDistanceData;
import model.workspace.tasks.Task_LoadMDSCoordinates;
import model.workspace.tasks.Task_LoadRawData;
import model.workspace.tasks.Task_LoadSpecificDatasets;
import model.workspace.tasks.Task_LoadTopicDistancesForSelection;
import model.workspace.tasks.WorkspaceTask;

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
public class Workspace implements ITaskListener
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
	public static final String FILENAME_MDSCOORDINATES	= "workspace.mds";
	/**
	 * Name of file containing parameters that have yet to be calculated.
	 */
	public static final String FILENAME_TOGENERATE		= "toGenerate.lda";
	/**
	 * Name of file containing diverse options (preferences, paths to executables etc.).
	 */
	public static final String FILENAME_CONFIGURATION	= "workspace.opt";
	/**
	 * Name of file database.
	 */
	public static final String DBNAME					= "vkpsa.db";
	
	// -----------------------------------------------
	// 		Variables storing configuration data
	// -----------------------------------------------
	
	Map<String, String> configurationOptions;
	
	// -----------------------------------------------
	// 		Actual (raw and (pre-)processed) data
	// -----------------------------------------------
	
	/**
	 * Contains newly generated parameter values that yet have to generated.
	 * After the generation is done, this collection is emptied.
	 */
	private ArrayList<LDAConfiguration> configurationsToGenerate;
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
	private int numberOfDatasetsInDISTable;
	
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
	/**
	 * Indicates whether metadata from raw topic files were loaded and processed or not.
	 */
	private boolean isMetadataLoaded;
	
	// -----------------------------------------------
	// 			Logging utilities.
	// -----------------------------------------------	
	
	/**
	 * Protocol pane's ProgressIndicator.
	 */
	protected ProgressIndicator log_protocol_progressindicator;
	
	/**
	 * Protocol pane's TextArea.
	 */
	protected TextArea log_protocol_textarea;
	
	// -----------------------------------------------
	// 			Database management.
	// -----------------------------------------------	
	
	/**
	 * Database management.
	 */
	private DBManagement db;
	
	// -----------------------------------------------	
	// -----------------------------------------------
	// 					Methods
	// -----------------------------------------------
	// -----------------------------------------------	
	
	
	public Workspace(String directory)
	{
		this.directory					= directory;
		this.datasetMap					= new HashMap<LDAConfiguration, Dataset>();
		this.ldaConfigurations			= new ArrayList<LDAConfiguration>();
		this.configurationsToGenerate	= new ArrayList<LDAConfiguration>();
		this.configurationOptions		= new HashMap<String, String>();
		
		// Set initial values for various flags.
		
		appendToDistanceMatrix			= false;
		appendToMDSCoordinateMatrix		= false;
		
		isMetadataLoaded				= false;
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
	 * @param optionSet Provides additional options, if needed. Is optional, may be null.
	 * @return Task processing the request.
	 */
	public WorkspaceTask executeWorkspaceAction(TaskType workspaceAction, DoubleProperty progressProperty, 
												ITaskListener listener, final Map<String, Integer> optionSet)
	{
		WorkspaceTask task	= null;
		
		switch (workspaceAction) {
			// -----------------------------------------------
			// 				Loading operations
			// -----------------------------------------------
			
			case COLLECT_METADATA:
				isMetadataLoaded		= false;
				isRawDataLoaded			= false;
				isDistanceDataLoaded	= false;
				isMDSDataLoaded			= false;
				
				// Collect metadata from raw topic files.
				task					= new Task_CollectMetadata(this, TaskType.COLLECT_METADATA, optionSet);
			break;
			
			case LOAD_RAW_DATA:
				// Load datasets.
				isRawDataLoaded 		= false;
				task					= new Task_LoadRawData(this, TaskType.LOAD_RAW_DATA, optionSet);
			break;
			
			case LOAD_DISTANCES:
				isDistanceDataLoaded	= false;
				task					= new Task_LoadDistanceData(this, TaskType.LOAD_DISTANCES, optionSet);
			break;
			
			case LOAD_MDS_COORDINATES:
				// Load MDS coordinates from file.
				isMDSDataLoaded 		= false;
				task					= new Task_LoadMDSCoordinates(this, TaskType.LOAD_MDS_COORDINATES, optionSet);
			break;
			
			case LOAD_SPECIFIC_DATASETS:
				// Plan: Queue instruction in frontend to backend.
				// Execute data reading task in workspace.
				task					= new Task_LoadSpecificDatasets(this, TaskType.LOAD_SPECIFIC_DATASETS, optionSet);
			break;
			
			// -----------------------------------------------
			// 		Calculation / writing operations
			// -----------------------------------------------
			
			case CALCULATE_DISTANCES:
				task = new Task_CalculateDistances(this, TaskType.CALCULATE_DISTANCES, optionSet);
			break;
			
			case CALCULATE_MDS_COORDINATES:
				task = new Task_CalculateMDSCoordinates(this, TaskType.CALCULATE_MDS_COORDINATES, optionSet);
			break;
			
			case GENERATE_PARAMETER_LIST:
				task = new Task_GenerateParameterList(this, TaskType.GENERATE_PARAMETER_LIST, optionSet);
			break;
			
			case GENERATE_DATA:
				task = new Task_GenerateData(this, TaskType.GENERATE_DATA, optionSet);
			break;
			
			// -----------------------------------------------
			// 			Collective operations
			// -----------------------------------------------
			
			// -----------------------------------------------
			// 			Maintainance operations
			// -----------------------------------------------
			
			case SWITCH:
				// Reset directory path.
				directory = "";
				// Reset other data.
				resetData();
			break;
			
			case RESET:
				resetData();
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
	
	@Override
	public void notifyOfTaskCompleted(TaskType workspaceAction)
	{
		switch (workspaceAction) 
		{
			case COLLECT_METADATA:
				// Load raw data.
				System.out.println("[Post-generation] Collected metadata.");
				//workspace.executeWorkspaceAction(WorkspaceAction.LOAD_RAW_DATA, generate_progressIndicator.progressProperty(), this);
			break;
			
			// After raw data was loaded: Calculate distances.
			case LOAD_RAW_DATA:
				//System.out.println("[Post-generation] Loading raw data.");
			break;
		}
	}
	
	private void resetData()
	{
		// Clear containers.
		datasetMap.clear();
		ldaConfigurations.clear();
		
		// Clear arrays.
		distances					= null;
		mdsCoordinates				= null;
		
		// Reset counters.
		numberOfDatasetsInDISTable	= -1;
		numberOfDatasetsInMDSFile	= -1;
		numberOfDatasetsInWS		= -1;
		
		// Reset flags.
		isMetadataLoaded			= false;
		isRawDataLoaded				= false;
		isDistanceDataLoaded		= false;
		isMDSDataLoaded				= false;
	}
	
	/**
	 * Returns number of .csv files in this directory.
	 * Does not check whether this .csv is actually an appropriate file.
	 * @return Number of .csv files in this directory.
	 */
	public int readNumberOfDatasets()
	{
		// Open connection to database.
		numberOfDatasetsInWS = db.readNumberOfLDAConfigurations();
		
		return numberOfDatasetsInWS;
	}
	
	/**
	 * Reads number of datasets in dataset distance table.
	 * @return
	 */
	public int readNumberOfDatasetsInDISTable()
	{
		numberOfDatasetsInDISTable = db.readNumberOfDatasetsInDISTable();
		
		return numberOfDatasetsInDISTable;
	}
	
	/**
	 * Returns number of .csv files in this directory modified later than the specified timestamp. 
	 * Does not check whether this .csv is actually an appropriate file.
	 * @param minCreationTimestamp
	 * @deprecated
	 * @return Number of .csv files in this directory modified later than the given timestamp value.
	 */
	public int readNumberOfDatasets(long minCreationTimestamp)
	{
		numberOfDatasetsInWS = 0;
		
		for(File f : new File(directory).listFiles()) {
			String filePath			= f.getAbsolutePath();
			String fileExtension	= filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length());
			
			// Consider file only if it was modified after the specified timestamp.
			if (f.lastModified() >= minCreationTimestamp)
				numberOfDatasetsInWS = "csv".equals(fileExtension) ? numberOfDatasetsInWS + 1 : numberOfDatasetsInWS;
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
	 * Checks whether or not the .dis and .mds files are consistent with the data files contained in the workspace.
	 * Executed after all available metadata was loaded.
	 * Does check for integrity, does not check for completeness.
	 * @return Flag indicating integrity of metadata/consistency of metadata and data in this workspace. True if workspace is consistent.
	 */
	public boolean checkIntegrity()
	{
		boolean isIntegrous			= true;
		int numberOfDatasetsInWS	= getNumberOfDatasetsInWS();
		
		// Check for integrity of pre-calculated distance data.
		if (numberOfDatasetsInWS != numberOfDatasetsInDISTable) {
			isIntegrous = false;
		}

		// Check for integrity of pre-calculated MDS coordinate data.
		if (isIntegrous && isMDSDataLoaded) {
			if (numberOfDatasetsInWS != numberOfDatasetsInMDSFile) {
				isIntegrous = false;
			}
			
			// Check if configuration in line in .mds file is consistent with configuration line
			// parsed from raw topic data files.
			else {
				isIntegrous = compareToLDAConfiguration(Paths.get(directory, FILENAME_MDSCOORDINATES), " "); 
			}
		}
	
		// Check if number of datasets in DIS table and MDS file match each other.
		if (numberOfDatasetsInDISTable != numberOfDatasetsInMDSFile)
			isIntegrous = false;
		
		return isIntegrous;
	}
	
	/**
	 * Compares LDA configuration string in first line of given line (LDA configurations 
	 * are separated by spaces) to the current workspace.ldaConfigurations.
	 * @deprecated
	 * @param path
	 * @param delimiter
	 * @return
	 */
	private boolean compareToLDAConfiguration(Path path, String delimiter)
	{
		try {
			// Open file reader.
			InputStream fis			= new FileInputStream(path.toString());
		    InputStreamReader isr	= new InputStreamReader(fis, Charset.forName("UTF-8"));
		    BufferedReader reader	= new BufferedReader(isr);
		    
			// Read first line in dataset (contains metadata).
		    String line				= reader.readLine();
		    
			// Close reader.
			reader.close();
			
		    int i = 0;
		    // Process and compare LDA configuration data.
		    for (String ldaConfigString : line.split(delimiter)) {
		    	if (!ldaConfigString.isEmpty()) {
		    		// Compare with already loaded LDA configuration collection.
		    		// Read LDA configuration string has to match in (1) content and (2) order.
		    		if (!LDAConfiguration.generateLDAConfiguration(ldaConfigString).equals(ldaConfigurations.get(i))) {
		    			return false;
		    		}
		    		
		    		i++;
		    	}
		    }
		}
		
		catch (Exception exception) {
			exception.printStackTrace();
		}
		
		return true;
	}
	
	/**
	 * Parses .config file in working directory; stores specified options.
	 */
	private void parseConfigurationFile()
	{
		// Check if configuration file exists.
		if (Files.exists(Paths.get(directory, FILENAME_CONFIGURATION))) {
			Path path		= Paths.get(directory, Workspace.FILENAME_CONFIGURATION);
		    Charset charset	= Charset.forName("UTF-8");
		    
		    try {
				List<String> lines = Files.readAllLines(path, charset);
				
				for (String line : lines) {
					String[] optionParts = line.split("=");
					
					if (optionParts.length == 2) {
						configurationOptions.put(optionParts[0], optionParts[1]);
					}
					
					else {
						System.out.println("### WARNING ### Option '" + line + "' was ignored.");
					}
				}
			} 
		    
		    catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		else {
			System.out.println("### ERROR ### Add a configuration file to the current workspace.");
		}
	}
	
	public void closeDB()
	{
		System.out.println("Closing database.");
		
		if (db != null) {
			db.close();
		}
	}
	
	public void reopenDB()
	{
		if (db != null) {
			db.reopen();
		}
	}
	
	/**
	 * Adds specified line to the protocol panel.
	 * @param additionalLogString
	 */
	public void log(String additionalLogString)
	{
		log_protocol_textarea.setText(log_protocol_textarea.getText() + "\n" + additionalLogString);
	}
	
	/**
	 * Sets indicator to busy/idle.
	 */
	public void setProgressStatus(boolean isBusy)
	{
		log_protocol_progressindicator.setVisible(isBusy);		
	}
	
	// ------------------------------
	// From here: Getter and setter.
	// ------------------------------
	
	public String getDirectory()
	{
		return directory;
	}

	/**
	 * Set references to protocol UI elements.
	 * @param log_protocol_progressindicator
	 * @param log_protocol_textarea
	 */
	public void setProtocolElements(ProgressIndicator log_protocol_progressindicator, TextArea log_protocol_textarea)
	{
		this.log_protocol_progressindicator = log_protocol_progressindicator;
		this.log_protocol_textarea			= log_protocol_textarea;
	}

	public void setDirectory(String directory)
	{
		this.directory = directory;
		
		if (db == null) {
			String dbPath	= directory + "\\" + Workspace.DBNAME;
			db				= new DBManagement(dbPath);
			log("Successfully initiated database at " + dbPath + ".");
			System.out.println("Successfully initiated database at " + dbPath + ".");
		}
		
		else {
			log("### ERROR ### Directory switch currently not supported.");
			System.out.println("### ERROR ### Directory switch currently not supported.");
		}
		
		// Parse configuration file in directory.
		parseConfigurationFile();
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

	public boolean isMetadataLoaded()
	{
		return isMetadataLoaded;
	}

	public void setMetadataLoaded(boolean isMetadataLoaded)
	{
		this.isMetadataLoaded = isMetadataLoaded;
	}

	public boolean isMDSDataLoaded()
	{
		return isMDSDataLoaded;
	}

	public void setMDSDataLoaded(boolean isMDSDataLoaded)
	{
		this.isMDSDataLoaded = isMDSDataLoaded;
	}

	public int getNumberOfDatasetsInDISTable()
	{
		if (numberOfDatasetsInDISTable < 0)
			return readNumberOfDatasetsInDISTable();
		
		return numberOfDatasetsInDISTable;
	}

	public void setNumberOfDatasetsInDISTable(int numberOfDatasetsInDISTable)
	{
		System.out.println("nod = " + numberOfDatasetsInDISTable);
		this.numberOfDatasetsInDISTable = numberOfDatasetsInDISTable;
	}

	public ArrayList<LDAConfiguration> getConfigurationsToGenerate()
	{
		return configurationsToGenerate;
	}

	public void setConfigurationsToGenerate(ArrayList<LDAConfiguration> parameterValuesToGenerate)
	{
		this.configurationsToGenerate = parameterValuesToGenerate;
	}

	public static String getFilenameMdscoordinates()
	{
		return FILENAME_MDSCOORDINATES;
	}

	public static String getFilenameTogenerate()
	{
		return FILENAME_TOGENERATE;
	}

	public static String getFilenameConfiguration()
	{
		return FILENAME_CONFIGURATION;
	}

	public Map<String, String> getConfigurationOptions()
	{
		return configurationOptions;
	}

	public DBManagement getDatabaseManagement()
	{
		return db;
	}
}
