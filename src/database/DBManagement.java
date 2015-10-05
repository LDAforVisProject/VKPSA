package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.concurrent.Task;
import javafx.util.Pair;
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
	 * @param overwriteExistingValues
	 * 	@param task
	 * @todo Allow decision between whether or not existing values should be replaced.
	 */
	public void saveDatasetDistances(	final ArrayList<LDAConfiguration> ldaConfigurations,
										final double[][] distances,
										final boolean overwriteExistingValues,
										WorkspaceTask task)
	{
		// Keep track of processed rows.
		int processedRowCount = 0;
		
		try {
			// Init prepepard statement with query template.
			PreparedStatement statement = connection.prepareStatement("INSERT INTO datasetDistances(ldaConfigurationID_1, ldaConfigurationID_2, distance) VALUES(?, ?, ?)");
			
			// Set auto-commit to false.
			connection.setAutoCommit(false);
			
			// Iterate through distance matrix, create query, attach to batch.
			for (int i = 0; i < ldaConfigurations.size(); i++) {
				for (int j = i + 1; j < ldaConfigurations.size(); j++) {
					// Set values for row.
					statement.setInt(1, ldaConfigurations.get(i).getConfigurationID());
					statement.setInt(2, ldaConfigurations.get(j).getConfigurationID());
					statement.setDouble(3, distances[i][j]);

					// Add row to batch.
					statement.addBatch();
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
	}
	
	/**
	 * Stores distances between topics in different LDA datsets.
	 * @param topicDistances
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
	 * Loads distance matrix. 
	 * @param ldaConfigurations
	 * @param task Assigning task. Optional, may be null.
	 * @return
	 */
	public double[][] loadDistances(ArrayList<LDAConfiguration> ldaConfigurations, WorkspaceTask task)
	{
		double[][] distances			= new double[ldaConfigurations.size()][ldaConfigurations.size()];
		final int totalNumberOfItems	= (ldaConfigurations.size() * ldaConfigurations.size() - ldaConfigurations.size()) / 2;
		
//		NEXT: 
//			Test  if DB distance loading is working.
//			Then: Remove file integrity check (and all other .dis-based methods and attributes).
//			Then: Design system for topic distance data.
		
		// Init prepared statement with query template.
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM datasetDistances");
			ResultSet rs				= statement.executeQuery();
			
			// Process distance data rows.
			int processedRowCount		= 0;
			while (rs.next()) {
				int row		= processedRowCount / ldaConfigurations.size();
				int column	= processedRowCount % ldaConfigurations.size();
				
				// Symmetric distance matrix - [row][column] = [column][row].
				distances[row][column] = rs.getDouble("distance");
				distances[column][row] = distances[row][column];
				
				// Update loading task, if provided.
				if (task != null)
					task.updateTaskProgress(processedRowCount, totalNumberOfItems);
				
				// Keep track of processed rows.
				processedRowCount++;
			}
			
			if (totalNumberOfItems != processedRowCount)
				System.out.println("mismatch");
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
			PreparedStatement statement = connection.prepareStatement("	SELECT count(*) datasetCount FROM (" + 
																		    "SELECT distinct ldaConfigurationID_1 from datasetDistances " +
																		    "union " +
																		    "SELECT distinct ldaConfigurationID_2 from datasetDistances" +
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
}
