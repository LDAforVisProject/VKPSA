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
	private Connection db;
	
	/**
	 * Is supposed to be equal for all LDA configurations and topics.
	 */
	private int numberOfKeywordsPerTopic;
	
	public DBManagement(String dbPath)
	{
		this.dbPath = dbPath;
		
		initConnection();
	}
	
	void initConnection()
	{
	    try {
	      Class.forName("org.sqlite.JDBC");
	      db = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
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
			db.setAutoCommit(false);
			
			// Prepare statement.
			PreparedStatement insertStmt = db.prepareStatement(insertString);
			
			// Read one dataset (in order to extract the keywords from it).
			Pair<LDAConfiguration, ArrayList<Topic>> topics = Topic.generateTopicsFromFile(directory, filename, TopicKeywordAlignment.HORIZONTAL);
			Topic topic = topics.getValue().get(0);
			
			for (String keyword : topic.getKeywordProbabilityMap().keySet()) {
				insertStmt.setString(1, keyword);
				insertStmt.addBatch();
			}
			
			// Execute batch and commit.
			insertStmt.executeBatch();
			db.commit();
			
			// Re-enable auto-commit.
			db.setAutoCommit(true);
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
			if (db != null && !db.isClosed()) {
				db.close();
			}
			
			db = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		}
		
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		try {
			if (db != null && !db.isClosed()) {
				db.close();
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
			PreparedStatement stmt	= db.prepareStatement(query);
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
		System.out.println("number of results = " + numberOfResults);
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
			PreparedStatement stmt	= db.prepareStatement(query);
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
		      
				// Current row contains first entry of new LDA configuration.
				if (!ldaConfig.equals(currLDAConfig)) {
//		        	System.out.println("### LDA_CONFIG = " + currLDAConfigID + ", TOPICID = " + currTopicID);
//		        	System.out.println("\tat count = " + count);
		        	
		        	// 1. Create new topic out of collected keyword/probability pairs.
		        	ldaConfigTopics.get(currLDAConfig).add(new Topic(currTopicID, keywordProbabilityMap));
		        	
		        	// 2. Create new dataset out of collected topics.
		        	datasetMap.put( new LDAConfiguration(currLDAConfig), new Dataset(new LDAConfiguration(ldaConfig), ldaConfigTopics.get(currLDAConfig)));
		        	
		        	// Update currLDAConfig.
		        	currLDAConfig.copy(ldaConfig);
		        	
		        	// Clear topic collection.
		        	ldaConfigTopics.put(ldaConfig, new ArrayList<Topic>());
		        }
		        
				// Current row contains first entry of new topic.
		        if (topicID != currTopicID) {
//		        	System.out.println("\tOld: " + currLDAConfigID + "; TOPICID = " + currTopicID);
//		        	System.out.println("\tNew: " + rs.getInt("ldaConfigurationID") + "; TOPICID = " + topicID);

		        	// Create new topic out of collected keyword/probability pairs.
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
        	
			System.out.println(count + " rows.");
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
			PreparedStatement stmt	= db.prepareStatement(query);
			
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
			PreparedStatement stmt	= db.prepareStatement(query);
			
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
				PreparedStatement numKeywordsStmt	= db.prepareStatement("select count(*) as numKeywords from keywords");
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
				
				PreparedStatement numKeywordsStmt	= db.prepareStatement(stmtString);
				ResultSet rs						= numKeywordsStmt.executeQuery();
				// Grab first result (they should all amount to the same number).
				numberOfKeywordsPerTopic			= rs.next() ? rs.getInt("actualKWCount") : -1; 
			}
		} 
		
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void getLimitedKITData(ArrayList<LDAConfiguration> ldaConfigurations)
	{
		for (LDAConfiguration ldaConfig : ldaConfigurations) {
			System.out.println(ldaConfig.getConfigurationID());
			// 1. Get ID for this configuration.
		}
	}
}
