package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javafx.concurrent.Task;
import javafx.util.Pair;
import mdsj.Data;
import model.LDAConfiguration;
import model.topic.Topic;
import model.topic.TopicKeywordAlignment;
import model.workspace.Dataset;
import model.workspace.tasks.Task_LoadRawData;
import model.workspace.tasks.WorkspaceTask;

public class DBManagement
{
	private String dbPath;
	private Connection connection;
	
	/**
	 * Is supposed to be equal for all LDA configurations and topics.
	 */
	private int numberOfKeywordsPerTopic;
	
	public DBManagement(String dbPath)
	{
		this.dbPath = dbPath;
		
		initConnection();
	}
	
	/**
	 * Init connection to database.
	 */
	private void initConnection()
	{
	    try {
	      Class.forName("org.sqlite.JDBC");
	      connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
	    } 
	    
	    catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    
	    // Get number of keywords.
	    readNumberOfKeywords(false);
	}
	
	/**
	 * Takes an examplary file and copies it into the defined database.
	 * Used for dev. purposes only, not for production.
	 * @deprecated
	 */
	public void copyKeywordsInDB()
	{
		String directory	= "D:\\Workspace\\Scientific Computing\\VKPSA\\src\\data";
		String filename		= "8de22cba364834852ea59d6c77ef34a07e9346a0.csv";
		
		String insertString = 	"insert into KEYWORDS (keyword) " +
			    				"VALUES (?)";
		
		try {
			// Disable auto-commit.
			connection.setAutoCommit(false);
			
			// Prepare statement.
			PreparedStatement insertStmt = connection.prepareStatement(insertString);
			
			// Read one dataset (in order to extract the keywords from it).
			Pair<LDAConfiguration, ArrayList<Topic>> topics = Topic.generateTopicsFromFile(directory, filename, TopicKeywordAlignment.HORIZONTAL);
			Topic topic = topics.getValue().get(0);
			
			for (String keyword : topic.getKeywordProbabilityMap().keySet()) {
				insertStmt.setString(1, keyword);
				insertStmt.addBatch();
			}
			
			// Execute batch and commit.
			insertStmt.executeBatch();
			connection.commit();
			
			// Re-enable auto-commit.
			connection.setAutoCommit(true);
		} 
		
		catch (Exception e) {
			e.printStackTrace();
		}
		
		finally {
			close();
		}
	}

	public void reopen()
	{
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
			
			connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		}
		
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} 
		
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads and returns a list of all LDA configurations in this database.
	 * @param task
	 * @return
	 */
	public ArrayList<LDAConfiguration> loadLDAConfigurations(WorkspaceTask task)
	{
		// Init auxiliary variables.
		int count			= 0;
		ResultSet rs		= null;
		int numberOfResults	= readNumberOfLDAConfigurations();
		
		// Init primary collection.
		ArrayList<LDAConfiguration> ldaConfigurations = new ArrayList<LDAConfiguration>(numberOfResults);
		
		// Define query.
		String query 		= 	"select * from ldaConfigurations lda " +
								"order by lda.ldaConfigurationID";
		
		try {
			
			
			// Prepare statement for selection of raw data and fetch results.
			PreparedStatement stmt	= connection.prepareStatement(query);
			rs						= stmt.executeQuery();
			
			// As long as row is not the last one: Process it.
			while (rs.next()) {
				ldaConfigurations.add(new LDAConfiguration(rs.getInt("ldaConfigurationID"), rs.getInt("kappa"), rs.getDouble("alpha"), rs.getDouble("eta")));
				
				// Update task progress.
				task.updateTaskProgress(count++, numberOfResults);
			}
		}
		
		catch (SQLException e) {
			System.out.println("### ERROR ### count = " + count);
			e.printStackTrace();
		}
		
		return ldaConfigurations;
	}
	
	/**
	 * Loads complete set of raw data.
	 * @param task
	 * @return
	 */
	public Map<LDAConfiguration, Dataset> loadRawData(WorkspaceTask task)
	{
		// Check if number of keywords was determined correctly. If not, try again.
		if (numberOfKeywordsPerTopic < 0) {
			readNumberOfKeywords(false);
			
			// If failing again: No data available, return.
			if (numberOfKeywordsPerTopic < 0) {
				System.out.println("### WARNING ### No keyword/topic associations in database. Returning empty collection.");
				return new HashMap<LDAConfiguration, Dataset>();
			}
		}
		
		// Init auxiliary variables.
		int count			= 0;
		ResultSet rs		= null;
		int numberOfResults	= readNumberOfKeywordInTopicDatasets();
		
		// Init primary collection.
		Map<LDAConfiguration, Dataset> datasetMap = new HashMap<LDAConfiguration, Dataset>();
		
		// Define query.
		String query 		=	"select lda.ldaConfigurationID, lda.alpha, lda.kappa, lda.eta, topicID, keyword, probability from keywordInTopic kit " +
								"join keywords kw on kw.keywordID = kit.keywordID " +
								"join ldaConfigurations lda on lda.ldaConfigurationID = kit.ldaConfigurationID " +
								"order by lda.ldaConfigurationID, topicID";
		
		try {
			// Store all created topics for each dataset/LDA configuration in here.
			Map<LDAConfiguration, ArrayList<Topic>> ldaConfigTopics	= new HashMap<LDAConfiguration, ArrayList<Topic>>();
			Map<String, Double> keywordProbabilityMap				= new HashMap<String, Double>(numberOfKeywordsPerTopic);
			
			// Prepare statement for selection of raw data and fetch results.
			PreparedStatement stmt	= connection.prepareStatement(query);
			rs						= stmt.executeQuery();
			
			/*
			 * Create collection of datasets over collection of topics over collection of keyword/probability pairs.
			 */
			
			// Init reference values.
			LDAConfiguration currLDAConfig 	= new LDAConfiguration(rs.getInt("ldaConfigurationID"), rs.getInt("kappa"), rs.getDouble("alpha"), rs.getDouble("eta"));//new LDAConfiguration(-1, 0, 0);
			int currTopicID					= rs.getInt("topicID"); //-1;
			
			// Init collection for topics per LDA configuration.
			ldaConfigTopics.put(new LDAConfiguration(currLDAConfig), new ArrayList<Topic>());
			
			// As long as row is not the last one: Process it.
			while (rs.next()) {
				final LDAConfiguration ldaConfig 	= new LDAConfiguration(rs.getInt("ldaConfigurationID"), rs.getInt("kappa"), rs.getDouble("alpha"), rs.getDouble("eta"));
				int topicID 						= rs.getInt("topicID");
				boolean isNewLDAConfig				= !ldaConfig.equals(currLDAConfig); 

				// Current row contains first entry of new LDA configuration: Commit LDA configuration processed so far.
				if (isNewLDAConfig) {
		        	// 1. Create new topic out of collected keyword/probability pairs.
		        	ldaConfigTopics.get(currLDAConfig).add(new Topic(currTopicID, keywordProbabilityMap));
		        	
		        	// 2. Create new dataset out of collected topics.
		        	datasetMap.put( new LDAConfiguration(currLDAConfig), new Dataset(new LDAConfiguration(ldaConfig), ldaConfigTopics.get(currLDAConfig)));
		        	
		        	// Update currLDAConfig.
		        	currLDAConfig.copy(ldaConfig);
		        	
		        	// Update currTopicID.
		        	currTopicID = topicID;
		        	
		        	// Clear topic collection.
		        	ldaConfigTopics.put(ldaConfig, new ArrayList<Topic>());
		        }
		        
				// Current row contains first entry of new topic: Commit topic processed so far.
				else if (topicID != currTopicID) {
		        	// Create new topic out of collected keyword/probability pairs so far.
		        	ldaConfigTopics.get(currLDAConfig).add(new Topic(currTopicID, keywordProbabilityMap));
		        	
		        	// Update currTopicID.
		        	currTopicID = topicID;
		        }
		        
		        // Add keyword/probability pair to hash map.
				keywordProbabilityMap.put(rs.getString("keyword"), rs.getDouble("probability"));

				// Update task progress.
				task.updateTaskProgress(count++, numberOfResults);
			}
			
			// For last dataset: Flush data, create last dataset.
        	// 	1. Create new topic out of collected keyword/probability pairs.
        	ldaConfigTopics.get(currLDAConfig).add(new Topic(currTopicID, keywordProbabilityMap));
        	// 	2. Create new dataset out of collected topics.
        	Dataset test = new Dataset(currLDAConfig, ldaConfigTopics.get(currLDAConfig));
        	datasetMap.put( currLDAConfig, test );
        	
//			System.out.println(count + " rows.");
		}
		
		catch (SQLException e) {
			System.out.println("### ERROR ### count = " + count);
			e.printStackTrace();
		}
		
		return datasetMap;
	}
	
	public int readNumberOfLDAConfigurations()
	{
		String query = 	"select count(*) resCount from ldaConfigurations";
		
		try {
			// Parse statement.
			PreparedStatement stmt	= connection.prepareStatement(query);
			
			// Fetch results.
			ResultSet rs			= stmt.executeQuery();
			
			// Return result.
			return rs.getInt("resCount");
		}
		
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	private int readNumberOfKeywordInTopicDatasets()
	{
		String query = 	"select count(*) as resCount from keywordInTopic kit " +
						"join keywords kw on kw.keywordID = kit.keywordID " +
						"join ldaConfigurations lda on lda.ldaConfigurationID = kit.ldaConfigurationID " +
						"order by lda.ldaConfigurationID, topicID";
		
		try {
			// Parse statement.
			PreparedStatement stmt	= connection.prepareStatement(query);
			
			// Fetch results.
			ResultSet rs			= stmt.executeQuery();
			
			// Return result.
			return rs.getInt("resCount");
		}
		
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	private void readNumberOfKeywords(boolean useDedicatedTable)
	{
		try {
			if (useDedicatedTable) {
				// Get number of keywords.
				PreparedStatement numKeywordsStmt	= connection.prepareStatement("select count(*) as numKeywords from keywords");
				ResultSet rs						= numKeywordsStmt.executeQuery();
				numberOfKeywordsPerTopic			= rs.getInt("numKeywords");
			}
			
			else {
				// Complete request:
//				select lda.alpha, lda.kappa, lda.eta, topicID, count(*) from keywordInTopic kit
//				join ldaConfigurations lda on lda.ldaConfigurationID = kit.ldaConfigurationID
//				group by lda.alpha, lda.kappa, lda.eta, topicID
//				order by lda.ldaConfigurationID, topicID;
				
				// Simplified request:
				String stmtString					= 	"select count(*) as actualKWCount from keywordInTopic kit " +
														"join ldaConfigurations lda on lda.ldaConfigurationID = kit.ldaConfigurationID " +
														"group by lda.ldaConfigurationID, topicID " +
														"order by lda.ldaConfigurationID, topicID";
				
				PreparedStatement numKeywordsStmt	= connection.prepareStatement(stmtString);
				ResultSet rs						= numKeywordsStmt.executeQuery();
				// Grab first result (they should all amount to the same number).
				numberOfKeywordsPerTopic			= rs.next() ? rs.getInt("actualKWCount") : -1; 
			}
		} 
		
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load keyword in topic data from database.
	 * @param ldaConfiguration
	 * @param maxNumberOfKeywords
	 * @return
	 */
	public ArrayList<ArrayList<Pair<String, Double>>> getKITData(LDAConfiguration ldaConfiguration, int maxNumberOfKeywords)
	{
		ArrayList<ArrayList<Pair<String, Double>>> data = null;
		
		// Get number of topics for this LDA configuration.
		String query = 	"select count(*) topicCount from topics t " + 
						"where t.ldaConfigurationID = " + ldaConfiguration.getConfigurationID() + ";";

		try {
			/*
			 * 1. Get number of topics for this LDA configuration.
			 */
			
			// @todo Get number of topics info from corresponding Dataset instance.
			PreparedStatement numKeywordsStmt	= connection.prepareStatement(query);
			ResultSet rs						= numKeywordsStmt.executeQuery();
			int numberOfTopics					= rs.getInt("topicCount");
			
			/*
			 * 2. Init collections.
			 */
			
			// Init collection holding data.
			data = new ArrayList<ArrayList<Pair<String, Double>>>(numberOfTopics);
			for (int i = 0; i < numberOfTopics; i++) {
				// Add list of keyword/probability pairs for this topic to collection for all topics.
				data.add(new ArrayList<Pair<String, Double>>(maxNumberOfKeywords));
			}
			
			// Init collection for checks if collection is complete.
			ArrayList<Integer> topicKeywordCount = new ArrayList<Integer>(numberOfTopics);
			for (int i = 0; i < numberOfTopics; i++) {
				topicKeywordCount.add(0);
			}
			
			int totalNumberOfKeywords = numberOfTopics * maxNumberOfKeywords;
			
			/*
			 * 3. Get data for all topics.
			 */
			
			query = "select lda.ldaConfigurationID, lda.alpha, lda.kappa, lda.eta, topicID, keyword, probability from keywordInTopic kit " +
					"join keywords kw on kw.keywordID = kit.keywordID " +
					"join ldaConfigurations lda on lda.ldaConfigurationID = kit.ldaConfigurationID " +
					"where " +
					"	lda.ldaConfigurationID = " + ldaConfiguration.getConfigurationID() + " and " +
					"	topicID between 0 and " + (numberOfTopics - 1) + " " + 
					"order by probability desc, topicID";
			
			PreparedStatement topicKeywordDataStmt	= connection.prepareStatement(query);
			rs										= topicKeywordDataStmt.executeQuery();
			
			boolean allRelevantRowsProcessed 	= false;
			int processedRowCount				= 0;
			while (rs.next() && !allRelevantRowsProcessed) {
				int topicID = rs.getInt("topicID");
				
				// Check if numberOfKeywords most relevant keywords for this topic have already been stored. 
				int topicKeywordCountForTopic = topicKeywordCount.get(topicID); 
				if (topicKeywordCountForTopic < maxNumberOfKeywords) {
					// Get data from row and add it to list.
					Pair<String, Double> keywordProbabilityPair = new Pair<String, Double>(rs.getString("keyword"), rs.getDouble("probability"));
					//Pair<String, Double> keywordProbabilityPair = new Pair<String, Double>(rs.getString("keyword"), Math.sqrt(Math.random()));
					data.get(topicID).add(keywordProbabilityPair);
					
					// Increment counter.
					topicKeywordCount.set(topicID, topicKeywordCountForTopic + 1);
					processedRowCount++;
					
					// Check if all relevant data rows were processed.
					allRelevantRowsProcessed = processedRowCount == totalNumberOfKeywords;
				}
			}
		}
		
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return data;
	}

	/**
	 * Writes dataset distances to DB.
	 * @param ldaConfigurations
	 * @param distances
	 * @param storeAllDistances
	 * @param listOfLDAConfigsWithoutDistances
	 * @param task
	 */
	public void saveDatasetDistances(	final ArrayList<LDAConfiguration> ldaConfigurations,
										final double[][] distances,
										boolean storeAllDistances, Set<Integer> listOfLDAConfigsWithoutDistances,
										WorkspaceTask task)
	{
		// Keep track of processed rows.
		int processedRowCount = 0;
		
		try {
			// Init prepared statement with query template.
			PreparedStatement statement = connection.prepareStatement("INSERT INTO datasetDistances(ldaConfigurationID_1, ldaConfigurationID_2, distance) VALUES(?, ?, ?)");
			
			// Set auto-commit to false.
			connection.setAutoCommit(false);
			
			// Iterate through distance matrix, create query, attach to batch.
			for (int i = 0; i < ldaConfigurations.size(); i++) {
				for (int j = i + 1; j < ldaConfigurations.size(); j++) {
					if (storeAllDistances || 
						listOfLDAConfigsWithoutDistances.contains(ldaConfigurations.get(i).getConfigurationID()) || 
						listOfLDAConfigsWithoutDistances.contains(ldaConfigurations.get(j).getConfigurationID()) ) {
						
						// Set values for row.
						System.out.println("storing " + ldaConfigurations.get(i).getConfigurationID() + " to " + ldaConfigurations.get(j).getConfigurationID()); 
						statement.setInt(1, ldaConfigurations.get(i).getConfigurationID());
						statement.setInt(2, ldaConfigurations.get(j).getConfigurationID());
						statement.setDouble(3, distances[i][j]);
	
						// Add row to batch.
						statement.addBatch();
					}
				}
				
				// Update loading task, if provided.
				if (task != null)
					task.updateTaskProgress(processedRowCount, ldaConfigurations.size());
			}
			
			// Execute batch.
			statement.executeBatch();
			
			// Commit transaction.
			connection.commit();
			
			// Re-enable auto-commit.
			connection.setAutoCommit(true);	
		} 
		
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("finished");
	}
	
	/**
	 * Stores distances between topics in different LDA datsets.
	 * @param topicDistances
	 * @param listOfLDAConfigsWithoutDistances 
	 * @param overwriteExistingValues
	 * @param task
	 */
	public void saveTopicDistances(	final Map<Pair<LDAConfiguration, LDAConfiguration>, double[][]> topicDistances,
									final boolean overwriteExistingValues,
									WorkspaceTask task)
	{
		// Keep track of processed rows.		
		int processedRowCount = 0;
		
		try {
			// Init prepepard statement with query template.
			PreparedStatement statement = connection.prepareStatement("INSERT INTO topicDistances(ldaConfigurationID_1, ldaConfigurationID_2, topicID_1, topicID_2, distance) VALUES(?, ?, ?, ?, ?)");
			
			// Set auto-commit to false.
			connection.setAutoCommit(false);
			
			// Iterate through pairs of LDA configurations, store respective distances.
			for (Pair<LDAConfiguration, LDAConfiguration> ldaConfigurationPair : topicDistances.keySet()) {
				// Topic distance matrix.
				double[][] topicDistanceMatrix	= topicDistances.get(ldaConfigurationPair);
				// LDA configuration 1.
				int ldaConfigID_1				= ldaConfigurationPair.getKey().getConfigurationID();
				// LDA configuration 2.
				int ldaConfigID_2				= ldaConfigurationPair.getValue().getConfigurationID();
				
				// Iterate through topic distances for this pair of LDA configurations.
				for (int i = 0; i < topicDistanceMatrix.length; i++) {
					for (int j = 0; j < topicDistanceMatrix[i].length; j++) {
						// Set values for row.
						statement.setInt(1, ldaConfigID_1);
						statement.setInt(2, ldaConfigID_2);
						statement.setInt(3, i);
						statement.setInt(4, j);
						statement.setDouble(5, topicDistanceMatrix[i][j]);

						// Add row to batch.
						statement.addBatch();
					}	
				}
				
				// Update loading task, if provided.
				if (task != null)
					task.updateTaskProgress(processedRowCount, topicDistances.size());
			}
			
			
			// Execute batch.
			statement.executeBatch();
			
			// Commit transaction.
			connection.commit();
			
			// Re-enable auto-commit.
			connection.setAutoCommit(true);	
		} 
		
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates a map translating a LDA configuration ID in an array to the corresponding array index.
	 * @param ldaConfigurations
	 * @return
	 */
	private Map<Integer, Integer> generateLDAConfigIDToArrayEntryMap(ArrayList<LDAConfiguration> ldaConfigurations)
	{
		// Create a configID-to-row/-column association map.
		Map<Integer, Integer> ldaConfigIDToArrayEntry = new HashMap<Integer, Integer>(ldaConfigurations.size());  
		
		// Fill association map.
		for (int i = 0; i < ldaConfigurations.size(); i++) {
			// Get current LDA configuration.
			LDAConfiguration ldaConfig = ldaConfigurations.get(i);
			// Add config ID as key to association map.
			ldaConfigIDToArrayEntry.put(ldaConfig.getConfigurationID(), i);
		}
		
		return ldaConfigIDToArrayEntry;
	}
	
	/**
	 * Loads distance matrix. 
	 * @param ldaConfigurations
	 * @param task Assigning task. Optional, may be null.
	 * @return
	 */
	public double[][] loadDistances(ArrayList<LDAConfiguration> ldaConfigurations, WorkspaceTask task)
	{
		double[][] distances			= new double[ldaConfigurations.size()][ldaConfigurations.size()];
		final int totalNumberOfItems	= (ldaConfigurations.size() * ldaConfigurations.size() - ldaConfigurations.size()) / 2;
		
		// Create a configID-to-row/-column association map.
		Map<Integer, Integer> ldaConfigIDToDistanceCell = generateLDAConfigIDToArrayEntryMap(ldaConfigurations);
		
		// Init prepared statement with query template.
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM datasetDistances order by ldaConfigurationID_1, ldaConfigurationID_2");
			ResultSet rs				= statement.executeQuery();
			
			// Process distance data rows.
			int processedRowCount		= 0;
			while (rs.next()) {
				final int ldaConfigID1 = rs.getInt("ldaConfigurationID_1");
				final int ldaConfigID2 = rs.getInt("ldaConfigurationID_2");
				
				if (!ldaConfigIDToDistanceCell.containsKey(ldaConfigID1))
					System.out.println("~~~~~~~~~~ ERROR - " + ldaConfigID1 + " not in loaded metadata.");
				if (!ldaConfigIDToDistanceCell.containsKey(ldaConfigID1))
					System.out.println("~~~~~~~~~~ ERROR - " + ldaConfigID2 + " not in loaded metadata.");
				
				int row		= ldaConfigIDToDistanceCell.get(ldaConfigID1);//processedRowCount / ldaConfigurations.size();
				int column	= ldaConfigIDToDistanceCell.get(ldaConfigID2);//processedRowCount % ldaConfigurations.size();
				
				// Symmetric distance matrix - [row][column] = [column][row].
				distances[row][column] = rs.getDouble("distance");
				distances[column][row] = distances[row][column];
				
				// Update loading task, if provided.
				if (task != null)
					task.updateTaskProgress(processedRowCount, totalNumberOfItems);
				
				// Keep track of processed rows.
				processedRowCount++;
			}
			
//			System.out.println(Data.format(distances));
//			if (totalNumberOfItems != processedRowCount)
//				System.out.println("mismatch");
		}
		
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return distances;
	}

	/**
	 * Reads number of datasets in dataset distance table.
	 * @return
	 */
	public int readNumberOfDatasetsInDISTable()
	{
		int numberOfDatasets = -1;
		
		try {
			PreparedStatement statement = connection.prepareStatement("	SELECT count(distinct ldaConfigID) datasetCount FROM (" + 
																		    "SELECT distinct ldaConfigurationID_1 ldaConfigID from datasetDistances " +
																		    "union " +
																		    "SELECT distinct ldaConfigurationID_2 ldaConfigID  from datasetDistances" +
																		")"
																	);

			ResultSet rs				= statement.executeQuery();
			numberOfDatasets			= rs.getInt("datasetCount");
		} 
		
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		return numberOfDatasets;
	}
	
	/**
	 * Loads configuration IDs of all LDA configurations for which distances have not been calculated yet.
	 * @return
	 */
	public Set<Integer> loadLDAConfigIDsWithoutDistanceMatrixEntries()
	{
		int numberOfDatasets							= -1;
		Set<Integer> ldaConfigIDsWithoutDistances		= null;
		
		try {
			/*
			 * 1. Get number of datasets for which distances have not been calculated yet. 
			 */
			PreparedStatement statement = connection.prepareStatement(	"	select count(distinct ldaConfigurationID) ldaConfigCount " +
																		"	from ldaConfigurations ldac " +
																		"	where " +
																		"    	ldac.ldaConfigurationID not in ( " +
																		"    		select distinct ldaConfigurationID_1 from datasetDistances " +
																		"    		union " +
																		"    		select distinct ldaConfigurationID_2 from datasetDistances " +
																		"    	)"
																	);
			ResultSet rs					= statement.executeQuery();
			numberOfDatasets				= rs.getInt("ldaConfigCount");
			
			/*
			 *  2. Query for which datasets distances haven't been calculated yet.
			 */
			
			// Init list of LDA config IDs.
			ldaConfigIDsWithoutDistances	= new HashSet<Integer>(numberOfDatasets);

			// Prepare statement.
			statement = connection.prepareStatement(	"	select distinct ldaConfigurationID ldaConfigCount " +
														"	from ldaConfigurations ldac " +
														"	where " +
														"    	ldac.ldaConfigurationID not in ( " +
														"    		select distinct ldaConfigurationID_1 from datasetDistances " +
														"    		union " +
														"    		select distinct ldaConfigurationID_2 from datasetDistances " +
														"    	)"
													);
			// Execute statement.
			rs		= statement.executeQuery();
			
			// Process loaded data.
			while (rs.next()) {
				ldaConfigIDsWithoutDistances.add(rs.getInt("ldaConfigCount"));
			}
		} 
		
		catch (SQLException e) {
			e.printStackTrace();
		}
	
		return ldaConfigIDsWithoutDistances;
	}
	
	/**
	 * Loads topic distances for selected set of LDA configurations.
	 * @param selectedLDAConfigurations
	 * @return Map translating LDA config ID and topic ID to to the corresponding spatial ID (i.e. row/topic number).
	 */
	public Pair<Map<Pair<Integer, Integer>, Integer>, double[][]> loadTopicDistances(ArrayList<LDAConfiguration> selectedLDAConfigurations)
	{
		// Distances of each topic to every other topic. 
		double[][] topicDistances												= null;

		// Map containing the topic counts for each LDA configuration.
		LinkedHashMap<Integer, Integer> topicCountsByLDAConfiguration			= new LinkedHashMap<Integer, Integer>();
		
		// Map translating the LDA configuration ID / topic ID combination to a corresponding row number.
		Map<Pair<Integer, Integer>, Integer> spatialIDsForLDATopicConfiguration = null;
		
		try {
			// Prepare config IDs for query.
			String configIDsForQuery = "";
			// Add configuration IDs to query.
			for (LDAConfiguration ldaConfig : selectedLDAConfigurations) {
				configIDsForQuery += ldaConfig.getConfigurationID() + ",";
			}
			configIDsForQuery = configIDsForQuery.substring(0, configIDsForQuery.length() - 1);
			
			/*
			 * 1. Get number of topics involved.
			 */
			
			final int numberOfTopics 			= getNumberOfTopics(configIDsForQuery, topicCountsByLDAConfiguration);
			
			// 1. a. Associate LDA config ID/topic ID combinations with row numbers; i.e.: Assign one row/column number to each topic.
			spatialIDsForLDATopicConfiguration 	= generateLDATopicConfigToSpatialIDMap(topicCountsByLDAConfiguration);
			
			// 1. b. Allocate memory for topic distance matrix.
			topicDistances						= new double[numberOfTopics][numberOfTopics];
			
			/*
			 * 2. Read topic distances.
			 */
			
			PreparedStatement statement	= connection.prepareStatement(	"select * from topicDistances " + 
																		"where " +  
																		"ldaConfigurationID_1 in (" + configIDsForQuery + ") and " + 
																		"ldaConfigurationID_2 in (" + configIDsForQuery + ") " +  
																		"order by ldaConfigurationID_1, ldaConfigurationID_2, topicID_1, topicID_2;) "
																	);
			
			// Execute statement.
			ResultSet rs				= statement.executeQuery();

			// Process topic distances.
			while (rs.next()) {
				// Fetch data.
				final int ldaConfigID1 	= rs.getInt("ldaConfigurationID_1");
				final int ldaConfigID2 	= rs.getInt("ldaConfigurationID_2");
				final int topicID1		= rs.getInt("topicID_1");
				final int topicID2 		= rs.getInt("topicID_2");
				final double distance	= rs.getDouble("distance");
				
				// Get spatial IDs for the current LDA config ID/topic ID combinations.
				int spatialID_topic1	= spatialIDsForLDATopicConfiguration.get(new Pair<Integer, Integer>(ldaConfigID1, topicID1));
				int spatialID_topic2	= spatialIDsForLDATopicConfiguration.get(new Pair<Integer, Integer>(ldaConfigID2, topicID2));
				
				// Store topic distance in matrix (symmetrically).
				if (topicID1 != topicID2 || ldaConfigID1 != ldaConfigID2) {
					topicDistances[spatialID_topic1][spatialID_topic2] = distance;
					topicDistances[spatialID_topic2][spatialID_topic1] = distance;
				}
				else {
					topicDistances[spatialID_topic1][spatialID_topic2] = -1;
					topicDistances[spatialID_topic2][spatialID_topic1] = -1;
				}
				
//				@todo Continue here:
//					x. Place topic distance values in matrix (write beginning from diagonale, then copy to swapped row/column (symmetric!).).
//					x. Construct metadata (LDA config ID -> row numbers).
//					x. Jsonify data.
//					x. Write jsonified data into d3.js code.
//					5. Insert callbacks from javascript to Java (-> selection of a pair of topics is to update PTC).
//				
			}
		} 
    	
    	catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Return results.
		return new Pair<Map<Pair<Integer, Integer>, Integer>, double[][]>(spatialIDsForLDATopicConfiguration, topicDistances);
	}
	

	/**
	 * Generates spatial IDs (i.e., row/column numbers) for each combination of LDA config ID and topic ID (i.e.: for each individual topic).
	 * @param topicCountsByLDAConfiguration
	 * @return
	 */
	private LinkedHashMap<Pair<Integer, Integer>, Integer> generateLDATopicConfigToSpatialIDMap(LinkedHashMap<Integer, Integer> topicCountsByLDAConfiguration)
	{
		LinkedHashMap<Pair<Integer, Integer>, Integer> spatialIDsForLDATopicConfiguration = new LinkedHashMap<Pair<Integer,Integer>, Integer>();
		
		int rowNumber = 0;
		for (Map.Entry<Integer, Integer> entry : topicCountsByLDAConfiguration.entrySet()) {
			// Get total number of topics for this LDA configuration (the latter is to be found in entry.getKey()).
			final int numberOfTopics = entry.getValue();

			// Add one entry per topic.
			for (int i = 0; i < numberOfTopics; i++) {
				spatialIDsForLDATopicConfiguration.put(new Pair<Integer, Integer>(entry.getKey(), i), rowNumber++);
			}
		}
		
		return spatialIDsForLDATopicConfiguration;	
	}
	
	/**
	 * Determines the number of topics for each LDA configuration. 
	 * @param ldaConfigIDString
	 * @param numberOfTopicsByLDAConfiguration
	 * @return The total number of topics.
	 * @throws SQLException 
	 */
	private int getNumberOfTopics(String ldaConfigIDsString, Map<Integer, Integer> topicCountsByLDAConfiguration) throws SQLException
	{
		int numberOfTopics = 0;
		
		String statementString 		= 	"select ldaConfigurationID, count(*) topicCount from topics " +
										"where " + 
										"ldaConfigurationID in (" + ldaConfigIDsString + ") " + 
										"group by ldaConfigurationID " + 
										"order by ldaConfigurationID";

		// Prepare statement.
		PreparedStatement statement = connection.prepareStatement(statementString);
		
		// Execute statement.
		ResultSet rs				= statement.executeQuery();
		// Read total and grouped number of topics.
		while (rs.next()) {
			final int ldaConfigID 	= rs.getInt("ldaConfigurationID");
			final int topicCount	= rs.getInt("topicCount");
			
			topicCountsByLDAConfiguration.put(ldaConfigID, topicCount);
			
			// Keep track of total number of topics.
			numberOfTopics += topicCount;
		}

		return numberOfTopics;
	}
}
