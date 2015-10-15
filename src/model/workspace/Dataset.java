package model.workspace;

import java.util.ArrayList;

import javafx.util.Pair;
import model.LDAConfiguration;
import model.topic.Topic;

/**
 * One instance of dataset corresponds to the output of a execution of the LDA
 * algorithm with one specific set of parameters.
 * Additionally, it contains various variables useful for the interaction between
 * user and UI as well as UI and model (such as which coordinate MDS has calcu-
 * lated for this dataset).
 * @author RM
 *
 */
public class Dataset
{
	/**
	 * The name of the file this dataset was created from.
	 */
	private String filename;
	/**
	 * Holds the configuration data for this file.
	 */
	private LDAConfiguration parametrization;
	/**
	 * Holds all topics in this dataset.
	 */
	private ArrayList<Topic> topics;
	/**
	 * Calculated MDS coordinates (in the global scope
	 * scatterchart) for this dataset.
	 */
	private Pair<Double, Double> calculatedMDSCoordinates;
	
	
	public Dataset(final LDAConfiguration parametrization)
	{
		this.parametrization	= new LDAConfiguration(parametrization);
		this.topics				= new ArrayList<Topic>();
	}
	
	public Dataset(final LDAConfiguration parametrization, final ArrayList<Topic> topics)
	{
		this.parametrization	= new LDAConfiguration(parametrization);
//		this.topics				= new ArrayList<Topic>(topics);
		this.topics				= topics;
	}
	
	// -----------------------------------------------
	// 			Distance calculation methods
	// -----------------------------------------------
	
	/**
	 * Calculates symmetric distance between two datasets.
	 * @param dataset1
	 * @param dataset2
	 * @param distanceType
	 * @param test 
	 * @return
	 */
	public double calculateDatasetDistance(final Dataset dataset, DatasetDistance distanceType, double[][] topicDistances)
	{
		double distance = 0;
			
		switch (distanceType) {
			case MinimalDistance:
				distance = (calculateMinimalDatasetDistance(dataset, topicDistances, false) + dataset.calculateMinimalDatasetDistance(this, topicDistances, true)) / 2;
			break;
			
			case HausdorffDistance:
				distance = (calculateHausdorffDatasetDistance(dataset, topicDistances, false) + dataset.calculateHausdorffDatasetDistance(this, topicDistances, true)) / 2;
			break;
			
			default:
				System.out.println("Invalid dataset distance type specified: " + distanceType.toString() + " is unknown.");
		}
		
		return distance;
	}
	
	/**
	 * @todo Add relaxed/strict enum parameter to distuingish whether the same topic may be used for
	 * multiple other topics.
	 * Calculates distance between two datasets using the average of all minimal distances between
	 * one topic of one dataset and all topics of the other Workspace.
	 * @param dataset
	 * @param topicDistances
	 * @param reversedCallOrder
	 * @return
	 */
	private double calculateMinimalDatasetDistance(final Dataset dataset, double[][] topicDistances, boolean reversedCallOrder)
	{
		double minDistance	= Double.MAX_VALUE;
		Topic currentTopic	= null;
		
		ArrayList<Topic> topics1 = this.getTopics();
		ArrayList<Topic> topics2 = dataset.getTopics();
		
		// Iterate through all topics of one dataset, compare each of them with all topics of the other dataset, pick minimal distance, sum up distances.
		for (int i = 0; i < topics1.size(); i++) {
			currentTopic = topics1.get(i);
			
			// Unrolled loop to avoid calculation of distance i to i without using an if.
			for (int j = 0; j < topics2.size(); j++) {
				double distance	= currentTopic.calculateBhattacharyyaDistance(topics2.get(j)); 
				minDistance		= minDistance > distance ? distance : minDistance;
				
				// Store topic distance in topic distance matrix.
				if (!reversedCallOrder)
					topicDistances[i][j] += distance / 2;
				else 
					topicDistances[j][i] += distance / 2;
			}	
		}
		
		// Return normalized distance.
		return minDistance / (topics1.size() * topics2.size());
	}
	
	/**
	 * @todo Add relaxed/strict enum parameter to distuingish whether the same topic may be used for
	 * multiple other topics.
	 * Calculates distance between two datasets using the Hausdorff distance.
	 * @param dataset
	 * @param topicDistances
	 * @param reversedCallOrder
	 * @return
	 */
	private double calculateHausdorffDatasetDistance(final Dataset dataset, double[][] topicDistances, boolean reversedCallOrder)
	{
		double minDistance		= Double.MAX_VALUE;
		double maxMinDistance	= 0;
		Topic currentTopic		= null;
		
		ArrayList<Topic> topics1 = this.getTopics();
		ArrayList<Topic> topics2 = dataset.getTopics();
		
		// Iterate through all topics of one dataset, compare each of them with all topics of the other dataset, pick minimal distance, sum up distances.
		for (int i = 0; i < topics1.size(); i++) {
			currentTopic 	= topics1.get(i);
			minDistance		= Double.MAX_VALUE;
			
			// Unrolled loop to avoid calculation of distance i to i without using an if.
			for (int j = 0; j < topics2.size(); j++) {
				double distance = currentTopic.calculateBhattacharyyaDistance(topics2.get(j)); 
				minDistance		= minDistance > distance ? distance : minDistance;
				
				// Store topic distance in topic distance matrix.
				if (!reversedCallOrder)
					topicDistances[i][j] += distance / 2;
				else 
					topicDistances[j][i] += distance / 2;				
			}
			
			maxMinDistance = maxMinDistance < minDistance ? minDistance : maxMinDistance;
		}
		
		// Return normalized distance.
		return maxMinDistance;
	}
	
	// ######################################
	// 			Getter and Setter
	// ######################################
	
	/**
	 * Sets {@link Dataset#topics}.
	 * @param topics
	 */
	public void setTopics(final ArrayList<Topic> topics)
	{
		this.topics.clear();
		this.topics.addAll(topics);
	}
	
	/**
	 * @return Returns {@link Dataset#topics}
	 */
	public ArrayList<Topic> getTopics()
	{
		return topics;
	}

	/**
	 * @return Returns {@link Dataset#calculatedMDSCoordinates}.
	 */
	public Pair<Double, Double> getCalculatedMDSCoordinates()
	{
		return calculatedMDSCoordinates;
	}

	/**
	 * @param Set {@link Dataset#calculatedMDSCoordinates}.
	 */
	public void setCalculatedMDSCoordinates(Pair<Double, Double> calculatedMDSCoordinates)
	{
		this.calculatedMDSCoordinates = calculatedMDSCoordinates;
	}

	/**
	 * @return Returns {@link Dataset#filename}.
	 */
	public String getFilename()
	{
		return filename;
	}

	/**
	 * @param Sets {@link Dataset#filename}.
	 */	
	public void setFilename(String filename)
	{
		this.filename = filename;
	}
}
