package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javafx.util.Pair;
import model.LDAConfiguration;
import model.topic.Topic;
import model.topic.TopicKeywordAlignment;

public class DBManagement
{
	private String dbPath;
	private Connection db;
	
	public DBManagement(String dbPath)
	{
		this.dbPath = dbPath;
		System.out.println("path = " + dbPath);
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
	    
	    System.out.println("Opened database successfully");
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
			
			System.out.println("finished");
		} 
		
		catch (Exception e) {
			e.printStackTrace();
		}
		
		finally {
			closeConnection();
		}
	}

	public void closeConnection()
	{
		try {
			db.close();
		} 
		
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void loadRawData()
	{
		System.out.println("on it");
		String insertString = 	"select lda.alpha, lda.kappa, lda.eta, topicID, keyword, probability from keywordInTopic kit " +
								"join keywords kw on kw.keywordID = kit.keywordID " +
								"join ldaConfigurations lda on lda.ldaConfigurationID = kit.ldaConfigurationID " +
								"order by lda.ldaConfigurationID, topicID";
		
		try {
			System.out.println("on it 2");
			// Prepare statement.
			PreparedStatement stmt	= db.prepareStatement(insertString);
			// Fetch results.
			ResultSet rs			= stmt.executeQuery();

			/*
			 * Create collection of datasets over collection of topics over collection of keyword/probability pairs.
			 */
			
			// Get values of first line.
			LDAConfiguration currentLDAConfig = new LDAConfiguration(rs.getInt("kappa"), rs.getDouble("alpha"), rs.getDouble("eta"));
			rs.next();
			
			while (rs.next()) {
				double alpha 		= rs.getDouble("alpha");
				int kappa			= rs.getInt("kappa");
				double eta 			= rs.getDouble("eta");
				int topicID 		= rs.getInt("topicID");
		        String keyword		= rs.getString("keyword");
		        double probability	= rs.getDouble("probability");
			}
		}
		
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("finished with raw data load");
	}
}
