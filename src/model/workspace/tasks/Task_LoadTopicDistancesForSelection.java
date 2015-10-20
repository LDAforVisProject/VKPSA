package model.workspace.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javafx.util.Pair;
import database.DBManagement;
import mdsj.Data;
import model.LDAConfiguration;
import model.workspace.Workspace;
import model.workspace.WorkspaceAction;

public class Task_LoadTopicDistancesForSelection extends WorkspaceTask
{
	/*
	 * --------------------------------------------------------------
	 * 							Data
	 * --------------------------------------------------------------
	 */
	
	/**
	 * Stores loaded topic distances.
	 */
	private double[][] topicDistances;
	
	/**
	 * Map translating row/column number to the corresponding LDA configuration; the actual topic distance matrix.
	 */
	private Map<Integer, LDAConfiguration> topicToLDAConfigurationIDMap;
	
	/**
	 * Map translating the combination of LDA configuration ID/topic ID to spatial IDs (e.g. row and column number in topic distance matrix).
	 */
	private Map<Pair<Integer, Integer>, Integer> spatialIDsForLDATopicConfiguration;
	
	/**
	 * Set of LDA configurations to load.
	 */
	private ArrayList<LDAConfiguration> ldaConfigurationsToLoad;
	
	/*
	 * Here: Strings directly used for chord diagram JS.
	 */
	
	/**
	 * Contains JSONified metadata concerning topics (e.g. name and color).
	 */
	private String json_topicsMMap;
	/**
	 * JSONified version of the topic distance matrix.
	 */
	private String json_topicDistancesString;
	
	/*
	 * --------------------------------------------------------------
	 * 							Methods
	 * --------------------------------------------------------------
	 */
	
	public Task_LoadTopicDistancesForSelection(	Workspace workspace, WorkspaceAction workspaceAction, 
												final Map<String, Integer> additionalOptionSet,
												ArrayList<LDAConfiguration> ldaConfigurationsToLoad)
	{
		super(workspace, workspaceAction, additionalOptionSet);
		
		this.ldaConfigurationsToLoad = ldaConfigurationsToLoad;
	}

	@Override
	protected Integer call() throws Exception
	{
		// Get DB.
		DBManagement db = workspace.getDatabaseManagement();
		
		/*
		 * 1. Load topic distances. 
		 */
		
		Pair<Map<Pair<Integer, Integer>, Integer>, double[][]> topicDistancesDataset 	= db.loadTopicDistances(ldaConfigurationsToLoad);
		
		// Fetch translation LDA config ID/topic ID to spatial ID.
		spatialIDsForLDATopicConfiguration		= topicDistancesDataset.getKey(); 
		// Fetch topic distance matrix.
		this.topicDistances						= topicDistancesDataset.getValue();

		// Generate translation from rowID
		
		// Prepare one color for each topic.
		Map<Integer, String> ldaConfigColors	= generateRandomColorsForLDAConfigurations(this.ldaConfigurationsToLoad); 
		
		/*
		 * 2. Adapt and jsonify data.
		 */
		
		// Start JSON object for topic metadata.
		json_topicsMMap 			= "[";
		// Start JSON object for topic distances.
		json_topicDistancesString	= "[";
//		
//		System.out.println("topicData = ");
//		System.out.println(Data.format(topicDistances));
		
		// Iterate over all topics.
		for (Map.Entry<Pair<Integer, Integer>, Integer> entry : spatialIDsForLDATopicConfiguration.entrySet()) {
			// Get relevant data.
			final int ldaConfigurationID 	= entry.getKey().getKey();
			final int topicID				= entry.getKey().getValue();
			final int spatialID				= entry.getValue();
			final String color				= ldaConfigColors.get(ldaConfigurationID);
			
			// Append to distance data JSON.
			double distanceSum = 0;
			json_topicDistancesString += "[";
			for (int i = 0; i < topicDistances.length; i++) {
				// Grab and append distance from this topic to all other topics - to be found in row spatialID / column i.
				double adaptedDistanceValue 	 = topicDistances[spatialID][i] < 0 ? 0 : 1 / topicDistances[spatialID][i];
				json_topicDistancesString 			+= adaptedDistanceValue + ",";
				distanceSum 						+= adaptedDistanceValue;
			}
			// Remove last comma, add closure bracket.
			json_topicDistancesString = json_topicDistancesString.substring(0, json_topicDistancesString.length() - 1) + "],";
			
			// Append to topic metadata JSON.
			json_topicsMMap += "{";
			json_topicsMMap += "\"name\": \"" + String.valueOf(ldaConfigurationID) + "#" + String.valueOf(topicID) + "\",";
			json_topicsMMap += "\"color\": \"" + color + "\",";
			json_topicsMMap += "\"disSum\": \"" + distanceSum + "\"";
			json_topicsMMap += "},";
		}
		
		
		// Remove last comma, add final closure bracket.
		json_topicsMMap				= json_topicsMMap.substring(0, json_topicsMMap.length() - 1) + "]";
		// Remove last comma, add final closure bracket.
//		json_topicDistancesString 	= json_topicDistancesString.substring(0, json_topicDistancesString.length() - 1) + "\n]";
		json_topicDistancesString 	= json_topicDistancesString.substring(0, json_topicDistancesString.length() - 1) + "]";
		
		return 1;
	}

	/**
	 * Generates one color hex string for each LDA configuration.
	 * @param ldaConfigurations
	 * @return
	 */
	private Map<Integer, String> generateRandomColorsForLDAConfigurations(final ArrayList<LDAConfiguration> ldaConfigurations)
	{
		// Create map.
		Map<Integer, String> colorsForLDAConfigurations = new HashMap<Integer, String>(ldaConfigurations.size());
		
		// Initialize random number generator.
		Random rand = new Random();

		// Generate random color for each LDA configuration.
		for (LDAConfiguration ldaConfig : ldaConfigurations) {
			int r 		= (int) (rand.nextDouble() * 255);
			int g 		= (int) (rand.nextDouble() * 255);
			int b 		= (int) (rand.nextDouble() * 255);
			String hex 	= String.format("#%02x%02x%02x", r, g, b);
			
			// Add hex string for this LDA configuration.
			colorsForLDAConfigurations.put(ldaConfig.getConfigurationID(), hex);
		}
		
		return colorsForLDAConfigurations;
	}

	public String getJSONTopicsMMap()
	{
		return json_topicsMMap;
	}

	public String gettJSONTopicDistancesString()
	{
		return json_topicDistancesString;
	}
}