package database;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.concurrent.Task;
import javafx.util.Pair;
import mdsj.Data;
import model.LDAConfiguration;
import model.documents.Document;
import model.documents.KeywordContext;
import model.misc.KeywordRankObject;
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
		
		// Init DB connection.
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
	    //readNumberOfKeywords(false);
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

	/**
	 * Imports reference topic model data from .csv file.
	 * Keyword probabilities are assigned uniformly.
	 * @param filepath
	 */
	public void importReferenceTopicModel(String filepath)
	{
		// Map of all keywords in DB.
		Map<String, Double> keywordProbabilities	= readKeywordsAsMap();
		// Allocate collection for reference topic model. 
		ArrayList<Map<String, Double>> topicModel	= new ArrayList<Map<String,Double>>();
		
		// Remember original size of keyword probability map.
		final int originalKPMapSize					= keywordProbabilities.size();
		
		try {
			// Load reference topic model data.
			List<String> lines = Files.readAllLines(new File(filepath).toPath());
			
			/*
			 * 1. Adjust keyword probabilities. 
			 */
			
			// One line <-> one manually defined topic.
			for (String line : lines) {
				// Allocate memory for this topic.
				Map<String, Double> topic = new HashMap<String, Double>(keywordProbabilities);
				
				// Replace whitespace with _.
				line = line.replace(" ", "_");
				
				// Extract keywords.
				final String[] keywords 	= line.split(",");
				// Calculate probability assigned to each keyword.
				final double probPerKeyword	= 1.0 / keywords.length;
				double adjustedProb			= 0;
				
				for (String kw : keywords) {
					// Keyword not found: Assign manually.
					if(!keywordProbabilities.containsKey(kw)) {
						// Manual assignemnts:
						switch (kw) 
						{
							case "isosurfaces":
								adjustedProb = probPerKeyword / 2;
								
								topic.put("isosurface", adjustedProb);
								topic.put("isosurfacing", adjustedProb);
							break;
							
							case "time-varying_data":
								adjustedProb = probPerKeyword / 3;
								
								topic.put("time-varying", adjustedProb);
								topic.put("time-varying_dataset", adjustedProb);
								topic.put("time-varying_datum", adjustedProb);
							break;
							
							case "focus+context_techniques":
								adjustedProb = probPerKeyword / 5;
								
								topic.put("focus_and_context", adjustedProb);
								topic.put("focus-and-context", adjustedProb);
								topic.put("focus+context_visualization", adjustedProb);
								topic.put("focus+context_visualization_technique", adjustedProb);
								topic.put("focus+context", adjustedProb);
							break;
							
							case "visualization_systems":
								adjustedProb = probPerKeyword / 1;
								
								topic.put("visualization_system", adjustedProb);
							break;
							
							case "unstructured_grids":
								adjustedProb = probPerKeyword / 1;
								
								topic.put("unstructured_grid", adjustedProb);
							break;
							
							case "coordinated_&_multiple_views":
								adjustedProb = probPerKeyword / 2;
								
								topic.put("multiple_coordinated_view", adjustedProb);
								topic.put("multiple-coordinated_view", adjustedProb);
							break;							
							
							case "interactive_visual_analysis":
								adjustedProb = probPerKeyword / 2;
								
								topic.put("interactive_visual_analysi", adjustedProb);
								topic.put("interactive_visual_exploration_and_analysi", adjustedProb);
							break;
							
							case "multiple_views":
								adjustedProb = probPerKeyword / 8;
								
								topic.put("multiple-view", adjustedProb);
								topic.put("multi-linked_view", topic.get("multi-linked_view") + adjustedProb);
								topic.put("multiple_view", adjustedProb);
								topic.put("multiple-view_technique", adjustedProb);
								topic.put("multiview", adjustedProb);
								topic.put("multi-view", adjustedProb);
								topic.put("multiple_coordinated_view", adjustedProb);
								topic.put("multiple-coordinated_view", adjustedProb);
							break;
							
							case "time_series_data":
								adjustedProb = probPerKeyword / 7;
								
								topic.put("timeseries", adjustedProb);
								topic.put("time_series_datum", adjustedProb);
								topic.put("time_series_dataset", adjustedProb);
								topic.put("time_series", adjustedProb);
								topic.put("time-series_datum", adjustedProb);
								topic.put("time-series_dataset", adjustedProb);
								topic.put("time-series", adjustedProb);
							break;
							
							case "social_networks":
								adjustedProb = probPerKeyword / 4;
								
								topic.put("social_network_analysi", adjustedProb);
								topic.put("social_networks_visualization", adjustedProb);
								topic.put("social_network", adjustedProb);
								topic.put("social-network", adjustedProb);
							break;
							
							case "geovisualization":
								adjustedProb = probPerKeyword / 4;
								
								topic.put("geographic_visualization", adjustedProb);
								topic.put("geo-temporal_visualization", adjustedProb);
								topic.put("geographic/geospatial_visualization", adjustedProb);
								topic.put("geospatial-temporal_visualization", adjustedProb);
							break;
							
							case "spatio-temporal_data":
								adjustedProb = probPerKeyword / 5;
								
								topic.put("spatiotemporal_datum", adjustedProb);
								topic.put("geospatial-temporal_datum", adjustedProb);
								topic.put("spatiotemporal_dataset", adjustedProb);
								topic.put("spatio-temporal_dataset", adjustedProb);
								topic.put("spatio-temporal_datum", adjustedProb);
							break;
							
							case "node-link_diagrams":
								adjustedProb = probPerKeyword / 2;
								
								topic.put("node-link", adjustedProb);
								topic.put("node-link_diagram", adjustedProb);
							break;
							
							case "treemaps":
								adjustedProb = probPerKeyword / 5;
								
								topic.put("tree-map", adjustedProb);
								topic.put("treemap", adjustedProb);
								topic.put("treemap-like", adjustedProb);
								topic.put("zoomable_treemap", adjustedProb);
								topic.put("voronoi_treemap", adjustedProb);
							break;
							
							case "parallel_coordinates":
								adjustedProb = probPerKeyword / 1;
								
								topic.put("parallel_coordinate", adjustedProb);
							break;
							
							case "hierarchies":
								adjustedProb = probPerKeyword / 1;
								
								topic.put("hierarchy", adjustedProb);
							break;
							
							case "multi-variate_data":
								adjustedProb = probPerKeyword / 6;
								
								topic.put("multi-variate", adjustedProb);
								topic.put("multi-variate_datum", adjustedProb);
								topic.put("multi-variate_visualization", adjustedProb);
								topic.put("multi-variate_visualization_technique", adjustedProb);
								topic.put("multivariate", adjustedProb);
								topic.put("statistics—multivariate", adjustedProb);
							break;
							
							case "user_interfaces":
								adjustedProb = probPerKeyword / 7;
								
								topic.put("user_interfaces—gui", adjustedProb);
								topic.put("user_interfaces,", adjustedProb);
								topic.put("user_interfaces—graphical", adjustedProb);
								topic.put("user_interfaces-graphical", adjustedProb);
								topic.put("techniques-user_interface", adjustedProb);
								topic.put("user-interaction", adjustedProb);
								topic.put("user-interface", adjustedProb);
							break;
							
							case "high-dimensional_data":
								adjustedProb = probPerKeyword / 5;
								
								topic.put("high-dimensional_data_visualization", adjustedProb);
								topic.put("high-dimensional_dataset", adjustedProb);
								topic.put("high-dimensional_data_analysi", adjustedProb);
								topic.put("high-dimensional_datum", adjustedProb);
								topic.put("high-dimensional_data-focusing", adjustedProb);
							break;
							
							case "scatterplots":
								adjustedProb = probPerKeyword / 3;
								
								topic.put("scatterplot_matrix", adjustedProb);
								topic.put("scatter-plot", adjustedProb);
								topic.put("scatterplot", adjustedProb);
							break;
							
							case "visual_analysis":
								adjustedProb = probPerKeyword / 10;
								
								topic.put("visual_data_analysi", adjustedProb);
								topic.put("visual_exploratory_data_analysi", adjustedProb);
								topic.put("visual_analysi", adjustedProb);
								topic.put("visual_analytics_query", adjustedProb);
								topic.put("visual_analytics-enabled", adjustedProb);
								topic.put("visualization/analysi", adjustedProb);
								topic.put("interactive_visual_analysi", adjustedProb);
								topic.put("linked_view_visual_analytic", adjustedProb);
								topic.put("interactive_visual_exploration_and_analysi", adjustedProb);
								topic.put("visual_analytic", adjustedProb);
							break;
							
							case "principal_component_analysis":
								adjustedProb = probPerKeyword / 1;
								
								topic.put("principal_component_analysi", adjustedProb);
							break;							
							
							case "medical_visualization":
								adjustedProb = probPerKeyword / 2;
								
								topic.put("visualization_in_medicine", adjustedProb);
								topic.put("biomedical_visualization", adjustedProb);
							break;
							
							case "linked_views":
								adjustedProb = probPerKeyword / 6;
								
								topic.put("linked_related_view", adjustedProb);
								topic.put("coordinated_linked_view", adjustedProb);
								topic.put("multi-linked_view", topic.get("multi-linked_view") + adjustedProb);
								topic.put("linked_view_visual_analytic", adjustedProb);
								topic.put("linked_view", adjustedProb);
								topic.put("linked-view", adjustedProb);
							break;
							
							case "tiled_displays":
								adjustedProb = probPerKeyword / 2;
								
								topic.put("tiled-display", adjustedProb);
								topic.put("tiled_display", adjustedProb);
							break;
							
							case "social_data_analysis":
								adjustedProb = probPerKeyword / 1;
								
								topic.put("social_data_analysi", adjustedProb);
							break;
							
							case "bioinformatics":
								adjustedProb = probPerKeyword / 2;
								
								topic.put("bioinformatics_visualization", adjustedProb);
								topic.put("bioinformatic", adjustedProb);
							break;
							
							case "applications_of_visualizations":
								adjustedProb = probPerKeyword / 10;
								
								topic.put("applications_of_visualization_technique", adjustedProb);
								topic.put("remote_visualization_application", adjustedProb);
								topic.put("interactive_visualization_application", adjustedProb);
								topic.put("applications_of_visualization", adjustedProb);
								topic.put("large_dataset_visualization_applications_of_infovi", adjustedProb);
								topic.put("scientific_visualization_application", adjustedProb);
								topic.put("information_visualization_application", adjustedProb);
								topic.put("applications¿visual_analytic", adjustedProb);
								topic.put("visualization_applications-topic", adjustedProb);
								topic.put("visualization_application", adjustedProb);
							break;
							
							case "glyphs":
								adjustedProb = probPerKeyword / 14;
								
								topic.put("glyph_rendering", adjustedProb);
								topic.put("tensor_glyph", adjustedProb);
								topic.put("glyph", adjustedProb);
								topic.put("glyphbased", adjustedProb);
								topic.put("glyph-based_technique", adjustedProb);
								topic.put("“glyphs”", adjustedProb);
								topic.put("glyph_design", adjustedProb);
								topic.put("glyph-based", adjustedProb);
								topic.put("vesselglyph", adjustedProb);
								topic.put("3d_glyph", adjustedProb);
								topic.put("star_glyph", adjustedProb);
								topic.put("glyph-based_visualization", adjustedProb);
								topic.put("glyph_packing", adjustedProb);
								topic.put("glyph_generation", adjustedProb);
							break;
							
							case "intelligence_analysis":
								adjustedProb = probPerKeyword / 1;
								
								topic.put("intelligence_analysi", adjustedProb);
							break;							
							
							case "geographic_visualiziation":
								adjustedProb = probPerKeyword / 5;
								
								topic.put("geographic_visualization", adjustedProb);
								topic.put("geo-temporal_visualization", adjustedProb);
								topic.put("geovistum", adjustedProb);
								topic.put("geographic/geospatial_visualization", adjustedProb);
								topic.put("geospatial-temporal_visualization", adjustedProb);
							break;				
							
							case "quality_evaluation":
								adjustedProb = probPerKeyword / 1;
								
								topic.put("qualitative_evaluation", adjustedProb);
							break;							
							
							case "vector_fields":
								adjustedProb = probPerKeyword / 7;
								
								topic.put("multivector_field", adjustedProb);
								topic.put("vector_field_datum", adjustedProb);
								topic.put("unsteady_vector_field", adjustedProb);
								topic.put("vector_field_visualization", adjustedProb);
								topic.put("time-dependent_vector_field", adjustedProb);
								topic.put("vector_field_topology", adjustedProb);
								topic.put("vector_field", adjustedProb);
							break;
							
							case "streamlines":
								adjustedProb = probPerKeyword / 11;
								
								topic.put("streamline_datum", adjustedProb);
								topic.put("streamline-like", adjustedProb);
								topic.put("streamline_generation", adjustedProb);
								topic.put("closed_streamline", adjustedProb);
								topic.put("adaptive_streamline", adjustedProb);
								topic.put("streamline_visualization", adjustedProb);
								topic.put("hyperstreamline_placement", adjustedProb);
								topic.put("streamline", adjustedProb);
								topic.put("hyperstreamline", adjustedProb);
								topic.put("streamline_placement", adjustedProb);
								topic.put("streamlines/streamtubes/tuboid", adjustedProb);
							break;		
							
							case "3d_vector_field_visualization":
								adjustedProb = probPerKeyword / 5;
								
								topic.put("vector_field_visualization", topic.get("vector_field_visualization") + adjustedProb);
								topic.put("vector_field", topic.get("vector_field") + adjustedProb);
								topic.put("vector_field_topology", topic.get("vector_field_topology") + adjustedProb);
								topic.put("vector_field_datum", topic.get("vector_field_datum") + adjustedProb);
								topic.put("multivector_field", topic.get("multivector_field") + adjustedProb);
							break;								
						}
										
					}
					
					// Keyword found: Assign probability, store in map.
					else {
						topic.put(kw, probPerKeyword);
					}
				}
				
				if (originalKPMapSize != topic.size()) {
					System.out.println("### ERROR ### Non-existent keyword used in manual replacement.");
				}
				
				// Add to topic collection.
				topicModel.add(topic);
			}
			
			/*
			 * 2. Insert new LDA configuration and topics.
			 */
			
			System.out.println("Inserting new LDA configuration and respective topics.");
			
			// Define ID for reference LDA configuration.
			final int referenceModelConfigID 	= 2222;
			// Number of topics in reference model.
			final int numberOfTopics			= 16; 
			
			// Prepare statements for data insertion.
			PreparedStatement ldaConfigInsertStatement 	= connection.prepareStatement(	"insert into ldaConfigurations (ldaConfigurationID, alpha, kappa, eta) " + 
																						"values (" + referenceModelConfigID + ", 0, " + numberOfTopics +", 0);");
			PreparedStatement topicInsertStatement 		= connection.prepareStatement(	"insert into topics (topicID, ldaConfigurationID) values (?, ?);");
			
			// Set auto-commit to false.
			connection.setAutoCommit(false);
			
			// Insert LDA configuration.
			ldaConfigInsertStatement.execute();
			
			// Add topic insert statements.
			for (int i = 0; i < numberOfTopics; i++) {
				topicInsertStatement.setInt(1, i);
				topicInsertStatement.setInt(2, referenceModelConfigID);
				topicInsertStatement.addBatch();
			}
			// Insert new topics.
			topicInsertStatement.executeBatch();
			
			/*
			 * 3. Map keywords to keyword IDs, insert KIT data. 
			 */
			
			System.out.println("Inserting new keyword/probability data.");
			
			PreparedStatement kitInsertStatement 	= connection.prepareStatement(	"insert into keywordInTopic (topicID, keywordID, probability, ldaConfigurationID) " + 
																					"values (?, ?, ?, " + referenceModelConfigID + ");");
			// Fetch keywords and their corresponding IDs.
			Map<String, Integer> kitData			= readKeywordsAsIndexedMap();
			
			// Iterate over topics.
			int currTopicID = 0;
			for (Map<String, Double> topicData : topicModel) {
				// Iterate over keywords in topic.
				for (Map.Entry<String, Double> keywordProbability : topicData.entrySet()) {
					// Add insert statetement (with keyword resolved to keyword ID).
					kitInsertStatement.setInt(1, currTopicID);
					kitInsertStatement.setInt(2, kitData.get(keywordProbability.getKey()));
					kitInsertStatement.setDouble(3, keywordProbability.getValue());
					// Add to batch.
					kitInsertStatement.addBatch();
				}
				
				currTopicID++;
			}
			// Execute KIT data batch.
			kitInsertStatement.executeBatch();
			
			// Commit transaction.
			connection.commit();
			
			// Re-enable auto-commit.
			connection.setAutoCommit(true);	
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Import finished.");
	}
	
	/**
	 * Reopen database.
	 */
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
		if (numberOfKeywordsPerTopic <= 0) {
			readNumberOfKeywords(true);

			// If failing again: No data available, return.
			if (numberOfKeywordsPerTopic < 0) {
				System.out.println("### WARNING ### No keyword/topic associations in database. Returning empty collection.");
				return new HashMap<LDAConfiguration, Dataset>();
			}
		}

		// Init auxiliary variables.
		int count					= 0;
		ResultSet rs				= null;
		final int numberOfResults	= readNumberOfKeywordInTopicDatasets();
		
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
			// Set cursor.
			rs.next();
			
			/*
			 * Create collection of datasets over collection of topics over collection of keyword/probability pairs.
			 */
			
			// Init reference values.
			LDAConfiguration currLDAConfig 	= new LDAConfiguration(rs.getInt("ldaConfigurationID"), rs.getInt("kappa"), rs.getDouble("alpha"), rs.getDouble("eta"));//new LDAConfiguration(-1, 0, 0);
			int currTopicID					= rs.getInt("topicID"); //-1;
			
			// Init collection for topics per LDA configuration.
			ldaConfigTopics.put(new LDAConfiguration(currLDAConfig), new ArrayList<Topic>());
			
			// As long as row is not the last one: Process it.
			do {
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
			} while (rs.next());
			
			System.out.println("4");
			// For last dataset: Flush data, create last dataset.
        	// 	1. Create new topic out of collected keyword/probability pairs.
        	ldaConfigTopics.get(currLDAConfig).add(new Topic(currTopicID, keywordProbabilityMap));
        	// 	2. Create new dataset out of collected topics.
        	Dataset test = new Dataset(currLDAConfig, ldaConfigTopics.get(currLDAConfig));
        	datasetMap.put( currLDAConfig, test );
        	
        	System.out.println("5");
        	
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
	
	/**
	 * Read number of keywords in all topics.
	 * @return
	 */
	private int readNumberOfKeywordInTopicDatasets()
	{
		// Original, exact query:
//		String query = 	"select count(*) as resCount from keywordInTopic kit " +
//						"join keywords kw on kw.keywordID = kit.keywordID " +
//						"join ldaConfigurations lda on lda.ldaConfigurationID = kit.ldaConfigurationID " +
//						"order by lda.ldaConfigurationID, topicID";
		// Approximate, faster query:
		String query = "select count(*) * " + numberOfKeywordsPerTopic + " resCount from topics";
		
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
	
	/**
	 * Read number of keywords in table "keywords".
	 * @param useDedicatedTable
	 * @return Number of keywords.
	 */
	public int readNumberOfKeywords(boolean useDedicatedTable)
	{
		try {
			if (useDedicatedTable) {
				// Get number of keywords.
				PreparedStatement numKeywordsStmt	= connection.prepareStatement("select count(*) as numKeywords from keywords");
				ResultSet rs						= numKeywordsStmt.executeQuery();
				numberOfKeywordsPerTopic			= rs.getInt("numKeywords");
			}
			
			else {
				// Simplified request:
				String stmtString					= 	"select count(*) as actualKWCount " + 
														"from keywordInTopic kit " +
														"join ldaConfigurations lda on " +
														"	lda.ldaConfigurationID = kit.ldaConfigurationID " +
														"group by " +
														"lda.ldaConfigurationID, topicID";
				
				PreparedStatement numKeywordsStmt	= connection.prepareStatement(stmtString);
				ResultSet rs						= numKeywordsStmt.executeQuery();
				// Grab first result (they should all amount to the same number).
				numberOfKeywordsPerTopic			= rs.next() ? rs.getInt("actualKWCount") : -1; 
			}
		} 
		
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return numberOfKeywordsPerTopic;
	}

	/**
	 * Load raw data for this topic. Limited to the specified number of keywords. 
	 * @param ldaConfigID
	 * @param topicID
	 * @param maxNumberOfKeywords
	 * @return
	 */
	public ArrayList<Pair<String, Double>> getRawDataForTopic(final int ldaConfigID, final int topicID, final int maxNumberOfKeywords)
	{
		// Allocate memory for raw data.
		ArrayList<Pair<String, Double>> data = null;

		try {
			/*
			 * 1. Init collections.
			 */
			
			// Init collection holding data.
			data = new ArrayList<Pair<String, Double>>(maxNumberOfKeywords);

			// Calculate total number of keywords.
			final int totalNumberOfKeywords = maxNumberOfKeywords;
			
			/*
			 * 2. Get data for all topics.
			 */
			
			String query = 	"select keyword, probability " + 
							"from keywordInTopic kit " +
							"join keywords kw on kw.keywordID 						= kit.keywordID " +
							"join ldaConfigurations lda on lda.ldaConfigurationID 	= kit.ldaConfigurationID " +
							"where " +
							"	lda.ldaConfigurationID 	= " + ldaConfigID + " and " +
							"	topicID 				= " + topicID + " " + 
							"order by probability desc";
			
			PreparedStatement topicKeywordDataStmt	= connection.prepareStatement(query);
			ResultSet rs							= topicKeywordDataStmt.executeQuery();
			
			boolean allRelevantRowsProcessed 	= false;
			int processedRowCount				= 0;
			while (rs.next() && !allRelevantRowsProcessed) {
				// Check if the numberOfKeywords most relevant keywords for this topic have already been stored. 
				if (processedRowCount < maxNumberOfKeywords) {
					// Get data from row and add it to list.
					Pair<String, Double> keywordProbabilityPair = new Pair<String, Double>(rs.getString("keyword"), rs.getDouble("probability"));
					data.add(keywordProbabilityPair);
					
					// Increment counter.
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
	 * Reads and returns map of keywords (and a respective probability of 0) in dedicated keyword table.
	 * @return
	 */
	private Map<String, Double> readKeywordsAsMap()
	{
		// Read number of keywords.
		if (numberOfKeywordsPerTopic <= 0) {
			readNumberOfKeywords(true);
		}
		
		// Allocate memory.
		Map<String, Double> keywords = new HashMap<String, Double>(numberOfKeywordsPerTopic);
		
		// Get keywords.
		try {
			PreparedStatement pstmt = connection.prepareStatement("select keyword from keywords");
			ResultSet rs			= pstmt.executeQuery();
			
			// Loop through result set.
			while (rs.next()) {
				keywords.put(rs.getString("keyword"), 0.0);
			}
		}
		
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return keywords;
	}
	
	/**
	 * Reads and returns map of keywords (and the respective keywordID) in dedicated keyword table.
	 * @return
	 */
	private Map<String, Integer> readKeywordsAsIndexedMap()
	{
		// Read number of keywords.
		if (numberOfKeywordsPerTopic <= 0) {
			readNumberOfKeywords(true);
		}
		
		// Allocate memory.
		Map<String, Integer> keywords = new HashMap<String, Integer>(numberOfKeywordsPerTopic);
		
		// Get keywords.
		try {
			PreparedStatement pstmt = connection.prepareStatement("select keyword, keywordID from keywords");
			ResultSet rs			= pstmt.executeQuery();
			
			// Loop through result set.
			while (rs.next()) {
				keywords.put(rs.getString("keyword"), rs.getInt("keywordID"));
			}
		}
		
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return keywords;
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
		int processedLDAConfigurationCount = 0;
		
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
						statement.setInt(1, ldaConfigurations.get(i).getConfigurationID());
						statement.setInt(2, ldaConfigurations.get(j).getConfigurationID());
						statement.setDouble(3, distances[i][j]);
	
						// Add row to batch.
						statement.addBatch();
					}
				}
				
				// Update loading task, if provided.
				if (task != null)
					task.updateTaskProgress(processedLDAConfigurationCount++, ldaConfigurations.size());
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
		int processedLDAConfigurationCount	= 0;
		// Define how many statements should be packed into one batch (statement).
		final int statementsPerBatch		= 10000;
		
		try {
			// Init prepepard statement with query template.
			PreparedStatement statement = connection.prepareStatement("INSERT INTO topicDistances(ldaConfigurationID_1, ldaConfigurationID_2, topicID_1, topicID_2, distance) VALUES(?, ?, ?, ?, ?)");
			
			// Set auto-commit to false.
			connection.setAutoCommit(false);
			
			// Count how many statements are in this batch.
			int statementsInBatch = 0;
			
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
						statement.setInt(1, ldaConfigID_1);
						statement.setInt(2, ldaConfigID_2);
						statement.setInt(3, i);
						statement.setInt(4, j);
						
						// Set values for row, if distance is not between the same topic.
						if ( !( (ldaConfigID_1 == ldaConfigID_2) && (i == j) ) ) {
							statement.setDouble(5, topicDistanceMatrix[i][j]);
						}
						// Else: Use 0 as distance of a topic to itself.
						else {
							statement.setDouble(5, 0);
						}

						// Add row to batch.
						statement.addBatch();
						
						// Keep track of how many statements are in this batch.
						statementsInBatch++;
					}	
				}
				
				// Execute batch, if more than 10000 statements are in it.
				if (statementsInBatch > statementsPerBatch) {
					// Reset counter.
					statementsInBatch = 0;
					
					// Execute and clear batch.
					statement.executeBatch();
					statement.clearBatch();
				}
				
				// Update loading task, if provided.
				if (task != null)
					task.updateTaskProgress(processedLDAConfigurationCount++, topicDistances.size());
			}
			
			// Execute batch with remaining statements.
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
	 * Loads topic distance maximum and minimum.
	 * @return Pair of extrema. Ordering: Minimum, maximum.
	 */
	public Pair<Double, Double> loadTopicDistanceExtrema()
	{
		double max = -1;
		double min = -1;
		
		try {
			// Avoid infinity values introduced by reference topic model(s) when looking for maximum.
			PreparedStatement statement	= connection.prepareStatement(	"select max(distance) maxDist from topicDistances " + 
																		"where cast(distance as string) not like '%Inf%';");
			// Execute statement.
			ResultSet rs				= statement.executeQuery();
			// Process value.
			max							= rs.getDouble("maxDist");
			
			statement					= connection.prepareStatement("select min(distance) minDist from topicDistances;");
			// Execute statement.
			rs							= statement.executeQuery();
			// Process value.
			min							= rs.getDouble("minDist");
		}
		
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return new Pair<Double, Double>(min, max);
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
	
	/**
	 * Returns list of suggestions used for auto-complete in control for new keyword filter.
	 * @param userText Text entered by user.
	 * @return
	 */
	public Collection<Object> getKeywordSuggestions(String userText) throws SQLException
	{
		Collection<Object> suggestions	= new ArrayList<Object>();
		
		String statementString 			= 	"select keyword " +
											"from keywords " +
											"where " +
											"   keyword like '%" + userText + "' or " +
											"	keyword like '" + userText + "%';";

		// Prepare statement.
		PreparedStatement statement = connection.prepareStatement(statementString);
		
		// Execute statement.
		ResultSet rs				= statement.executeQuery();
		// Read all similar keywords.
		while (rs.next()) {
			suggestions.add(rs.getString("keyword"));
		}

		return suggestions;
	}
		
	/**
	 * Checks if a given keyword exists.
	 * @param keyword
	 * @return True if keyword exists, false otherwise.
	 * @throws SQLException
	 */
	public boolean doesKeywordExist(String keyword) throws SQLException
	{
		boolean doesExist		= false;
		
		String statementString 	= 	"select exists (" +
										"select * " +
										"from keywords " +
										"where " +
										"    keyword = '" + keyword + "'" +
									") doesExist;";

		// Prepare statement.
		PreparedStatement statement = connection.prepareStatement(statementString);
		
		// Execute statement.
		ResultSet rs				= statement.executeQuery();
		// Reads result.
		while (rs.next()) {
			doesExist = rs.getBoolean("doesExist");
		}

		return doesExist;
	}

	/**
	 * Loads information about rank of specified keyword in topics.
	 * @param keyword
	 * @return
	 * @throws SQLException
	 */
	public ArrayList<KeywordRankObject> loadKeywordRankInformation(String keyword) throws SQLException
	{
		ArrayList<KeywordRankObject> results = new ArrayList<KeywordRankObject>(numberOfKeywordsPerTopic);
		
		/*
		 * 1. Fetch data.	
		 */
		
		String statementString 	= 		"select kit.topicID, kit.ldaConfigurationID, kit.rank " +
										"from keywords k " +
										"inner join keywordInTopic kit on " +
										"    kit.keywordID = k.keywordID " +
										"where " +
										"    k.keyword = '" + keyword + "' " +
										"order by " +
										"    rank;";

		// Prepare statement.
		PreparedStatement statement = connection.prepareStatement(statementString);
		
		// Execute statement.
		ResultSet rs				= statement.executeQuery();
		
		/*
		 * 2. Process data.
		 * 
		 */
		
		// Read results, store in collection.
		while (rs.next()) {
			results.add( new KeywordRankObject(rs.getInt("topicID"), rs.getInt("ldaConfigurationID"), rs.getInt("rank")) );
		}

		return results;
	}
	
	/**
	 * Loads all documents; sorts them by relevance for the specified topic. 
	 * @param topicID
	 * @return
	 */
	public ArrayList<Document> loadDocuments(Pair<Integer, Integer> topicID) throws SQLException
	{
		ArrayList<Document> documents = new ArrayList<Document>();
		
		/*
		 * 1. Fetch data.	
		 */
		
		String statementString 	=	"select " + 
									"	 d.id, " +
									"    tid.probability, " +
									"    d.title, " +
									"    d.authors, " +
									"    d.abstract, " +
									"    d.refinedAbstract, " +
									"    d.conference, " +
									"    d.date, " +
									"    d.keywords " +
									"from " +
									"    topics_in_documents tid " +
									"inner join documents d on " +
									"    d.id                   = tid.documentsID " +
									"where " +
									"    tid.ldaConfigurationID = " + topicID.getKey() + " and " +
									"    tid.topicID            = " + topicID.getValue() + " " +
									"order by " +
									"    tid.probability desc; "
									;
		
		// Prepare statement.
		PreparedStatement statement = connection.prepareStatement(statementString);
		
		// Execute statement.
		ResultSet rs				= statement.executeQuery();
	
		/*
		 * 2. Process data.
		 * 
		 */

		// Read results, store in collection.
		while (rs.next()) {
			documents.add( new Document(rs.getInt("id"), 
										rs.getFloat("probability"), 
										rs.getString("title"),
										rs.getString("authors"),
										rs.getString("keywords"),
										rs.getString("abstract"),
										rs.getString("refinedAbstract"),
										rs.getString("date"),
										rs.getString("conference")) );
		}
		
		return documents;
	}
	
	/**
	 * Load context for keyword: Appearances in abstracts, be it refined or un-refined.
	 * @param keyword
	 * @return
	 * @throws SQLException
	 */
	public ArrayList<KeywordContext> loadContext(String keyword) throws SQLException
	{
		ArrayList<KeywordContext> keywordContextList = new ArrayList<KeywordContext>();
		
		/*
		 * 1. Fetch data.	
		 */
		
		String statementString 	=	"select " +
									"    id, " +
									"    title, " +
									"    abstract, " +
									"    refinedAbstract " +
									"from " +
									"    documents d " +
									"where " +
									"    d.abstract like '%" + keyword + "%' or " +
									"    d.refinedAbstract like '%" + keyword + "%' " + 
									"order by date desc, authors asc; "
									;
		
		// Prepare statement.
		PreparedStatement statement = connection.prepareStatement(statementString);
		
		// Execute statement.
		ResultSet rs				= statement.executeQuery();
	
		/*
		 * 2. Process data.
		 * 
		 */

		// Read results, store in collection.
		while (rs.next()) {
			keywordContextList.add( new KeywordContext(	rs.getInt("id"), 
														keyword,
														-1,
														rs.getString("title"), 
														rs.getString("abstract"),
														rs.getString("refinedAbstract")) );
		}
		
		return keywordContextList;
	}	
	
	/**
	 * Loads document as specified by its ID.
	 * @param documentID
	 * @return
	 * @throws SQLException
	 */
	public Document loadDocumentByID(int documentID) throws SQLException
	{	
		/*
		 * 1. Fetch data.	
		 */
		
		String statementString 	=	"select " +
									"    * " +
									"from " +
									"    documents d " +
									"where " +
									"    d.id = " + documentID
									;
		
		// Prepare statement.
		PreparedStatement statement = connection.prepareStatement(statementString);
		
		// Execute statement.
		ResultSet rs				= statement.executeQuery();
	
		/*
		 * 2. Process data.
		 * 
		 */

		// Read results, store in collection.
		if (rs.next()) {
			return new Document(rs.getInt("id"),
								1,
								rs.getString("title"), 
								rs.getString("authors"), 
								rs.getString("keywords"), 
								rs.getString("abstract"), 
								rs.getString("refinedAbstract"), 
								rs.getString("date"), 
								rs.getString("conference")); 
		}
		
		return null;
	}	
}