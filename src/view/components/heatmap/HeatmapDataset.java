package view.components.heatmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import view.components.VisualizationComponentDataset;
import javafx.util.Pair;
import mdsj.Data;
import model.LDAConfiguration;

/**
 * Provides the entire collection of data needed to draw a heatmap.
 * @author RM
 *
 */
public class HeatmapDataset extends VisualizationComponentDataset
{
	/*
	 * Data collections.
	 */
	
	/**
	 * Store reference to chosen (may be filtered or selected) LDAConfigurations.
	 * These are examined, binned and then plotted. 
	 */
	private ArrayList<LDAConfiguration> chosenLDAConfigurations;
	
	/*
	 * Metadata.
	 */
	
	/**
	 * Stores index of an cell and the IDs of the corresponding/contained LDA configurations.
	 */
	private Map<Pair<Integer, Integer>, Set<Integer>> cellsToConfigurationIDs;
	/**
	 * Stores index of an cell and the IDs of the corresponding/contained LDA and topic configuration.
	 */
	private Map<Pair<Integer, Integer>, Set<Pair<Integer, Integer>>> cellsToTopicConfigurationIDs;
	
	/*
	 * Binned data.
	 */
	
	private double[][] binMatrix;
	private double minOccurenceCount;
	private double maxOccurenceCount;
	private double max_key1;
	private double min_key1;
	private double max_key2;
	private double min_key2;
	
	
	// --------------------------------------
	//				Methods
	// --------------------------------------
	
	/**
	 * Generate new dataset from list(s) of selected LDA configurations.
	 * Used to transform data for plot of densities of two parameters vs. each other.
	 * @param allLDAConfigurations
	 * @param chosenLDAConfigurations
	 * @param options
	 */
	public HeatmapDataset(	ArrayList<LDAConfiguration> allLDAConfigurations, ArrayList<LDAConfiguration> chosenLDAConfigurations, 
							HeatmapOptionset options)
	{
		/*
		 * Initialize values.
		 */
		 
		// If in relative view mode: allLDAConfigurations -> selectedLDAConfigurations.
		this.allLDAConfigurations		= options.getRelativeMode() ? chosenLDAConfigurations : allLDAConfigurations;
    	this.chosenLDAConfigurations	= chosenLDAConfigurations;
    	this.cellsToConfigurationIDs 	= new HashMap<Pair<Integer,Integer>, Set<Integer>>();
		
    	this.minOccurenceCount			= Double.MAX_VALUE;
		this.maxOccurenceCount			= Double.MIN_VALUE;
		max_key1						= Double.MIN_VALUE;
		min_key1						= Double.MAX_VALUE;
		max_key2						= Double.MIN_VALUE;
		min_key2						= Double.MAX_VALUE;
		
		/*
		 * Examine data.
		 */
		
    	// 1. Get maximum and minimum for defined keys (alias parameter values).
		
		for (LDAConfiguration ldaConfig : this.allLDAConfigurations) {
			max_key1 = max_key1 >= ldaConfig.getParameter(options.getKey1()) ? max_key1 : ldaConfig.getParameter(options.getKey1());
			max_key2 = max_key2 >= ldaConfig.getParameter(options.getKey2()) ? max_key2 : ldaConfig.getParameter(options.getKey2());
			
			min_key1 = min_key1 <= ldaConfig.getParameter(options.getKey1()) ? min_key1 : ldaConfig.getParameter(options.getKey1());
			min_key2 = min_key2 <= ldaConfig.getParameter(options.getKey2()) ? min_key2 : ldaConfig.getParameter(options.getKey2());
		}
		
		// 2. Bin data based on found minima and maxima.
		
		final int numberOfBins	= options.isGranularityDynamic() ? (int) Math.sqrt(this.allLDAConfigurations.size()) : options.getGranularity();
		binMatrix				= new double[numberOfBins][numberOfBins];
		double binInterval_key1	= (max_key1 - min_key1) / numberOfBins;
		double binInterval_key2	= (max_key2 - min_key2) / numberOfBins;
		
		for (LDAConfiguration ldaConfig : this.chosenLDAConfigurations) {
			// Calculate bin index.
			int index_key1 = (int) ( (ldaConfig.getParameter(options.getKey1()) - min_key1) / binInterval_key1);
			int index_key2 = (int) ( (ldaConfig.getParameter(options.getKey2()) - min_key2) / binInterval_key2);
			
			// Check if value is maximum. If so, it should be binned in the last bin nonetheless.
			index_key1 = index_key1 < numberOfBins ? index_key1 : numberOfBins - 1;
			index_key2 = index_key2 < numberOfBins ? index_key2 : numberOfBins - 1;
			
			// Increment content of corresponding bin.
			binMatrix[index_key1][index_key2]++;
			
			// Store references from cells to actual data.
			final Pair<Integer, Integer> mapCellKey = new Pair<Integer, Integer>(index_key1, index_key2);
			// 	Add entry in map, if it doesn't exist already.
			if (!cellsToConfigurationIDs.containsKey(mapCellKey)) {
				cellsToConfigurationIDs.put(mapCellKey, new HashSet<Integer>());
			}
			// 	Add to collection of datasets in this cell.
			cellsToConfigurationIDs.get(mapCellKey).add(ldaConfig.getConfigurationID());
		}
		
		// 3. Determine minimal and maximal occurence count.
		
		for (int i = 0; i < binMatrix.length; i++) {
			for (int j = 0; j < binMatrix[i].length; j++) {
				maxOccurenceCount = binMatrix[i][j] > maxOccurenceCount ? binMatrix[i][j] : maxOccurenceCount;
				minOccurenceCount = binMatrix[i][j] < minOccurenceCount ? binMatrix[i][j] : minOccurenceCount;
			}	
		}
	}
	
	/**
	 * Generate new dataset from coordinates of list of LDA configurations.
	 * Used to transform data for heatmap of occurence densities on global scatterplot.
	 * @param allLDAConfigurations
	 * @param coordinates
	 * @param coordinateExtrema
	 * @param options
	 */
	public HeatmapDataset(	ArrayList<LDAConfiguration> allLDAConfigurations, 
							double coordinates[][], double coordinateExtrema[], 
							HeatmapOptionset options)
	{
		/*
		* Initialize values.
		*/
		
		// If in relative view mode: allLDAConfigurations -> selectedLDAConfigurations.
		this.allLDAConfigurations		= options.getRelativeMode() ? chosenLDAConfigurations : allLDAConfigurations;
		this.chosenLDAConfigurations	= null;
		
		this.cellsToConfigurationIDs 	= new HashMap<Pair<Integer,Integer>, Set<Integer>>();
		
		this.minOccurenceCount			= Integer.MAX_VALUE;
		this.maxOccurenceCount			= Integer.MIN_VALUE;
		
		/*
		 * Examine data.
		 */
		
    	// 1. Get maximum and minimum for defined keys.
    	
    	double minX	= coordinateExtrema[0] * 1.0;
    	double maxX	= coordinateExtrema[1] * 1.0;
    	double minY	= coordinateExtrema[2] * 1.0;
		double maxY	= coordinateExtrema[3] * 1.0;
		
		// 2. Bin data based on found minima and maxima.
		
		final int numberOfBins	= options.isGranularityDynamic() ? (int) Math.sqrt(coordinates[0].length) * 2 : options.getGranularity();
		binMatrix				= new double[numberOfBins][numberOfBins];
		double binIntervalX		= (maxX - minX) / numberOfBins;
		double binIntervalY		= (maxY - minY) / numberOfBins;
		
		for (int i = 0; i < coordinates[0].length; i++) {
			// Calculate bin index.
			int indexX = (int) ( (coordinates[0][i] - minX) / binIntervalX);
			int indexY = (int) ( (coordinates[1][i] - minY) / binIntervalY);
			
			// Check if value is maximum. If so, it should be binned in the last bin nonetheless.
			indexX = indexX < numberOfBins ? indexX : numberOfBins - 1;
			indexY = indexY < numberOfBins ? indexY : numberOfBins - 1;
			
			// Increment content of corresponding bin.
			binMatrix[indexX][indexY]++;			
		}
		
		for (int i = 0; i < numberOfBins; i++) {
			for (int j = 0; j < numberOfBins; j++) {
				binMatrix[i][j] = (int) (Math.sqrt(binMatrix[i][j])  *  100);
			}	
		}
		
		// 3. Determine minimal and maximal occurence count.
		for (int i = 0; i < binMatrix.length; i++) {
			for (int j = 0; j < binMatrix[i].length; j++) {
				maxOccurenceCount = binMatrix[i][j] > maxOccurenceCount ? binMatrix[i][j] : maxOccurenceCount;
				minOccurenceCount = (binMatrix[i][j] < minOccurenceCount) && (binMatrix[i][j] > 0)  
																		? binMatrix[i][j] : minOccurenceCount;
			}	
		}
		
		// Proofcheck minOccurenceCount (in case no LDA configurations are filtered).
		minOccurenceCount 	= minOccurenceCount != Integer.MAX_VALUE ? minOccurenceCount : 0;
		// Update extrema.
		min_key1			= minX;
		max_key1			= maxX;
		min_key2			= minY;
		max_key2			= maxY;
	}
	
	/**
	 * Generate new dataset from distance matrix of LDA configurations.
	 * Used to transform data for heatmap of similiarities between topics.
	 * @param allLDAConfigurations
	 * @param spatialIDs
	 * @param distances
	 * @param options
	 */
	public HeatmapDataset(	ArrayList<LDAConfiguration> allLDAConfigurations, 
							Map<Pair<Integer, Integer>, Integer> spatialIDs,
							double distances[][], HeatmapOptionset options)
	{
		this.allLDAConfigurations		= allLDAConfigurations;
    	this.chosenLDAConfigurations	= null;
    	this.cellsToConfigurationIDs 	= new HashMap<Pair<Integer,Integer>, Set<Integer>>();
		this.cellsToTopicConfigurationIDs			= new HashMap<Pair<Integer,Integer>, Set<Pair<Integer,Integer>>>();
		
    	this.minOccurenceCount			= Integer.MAX_VALUE;
		this.maxOccurenceCount			= Integer.MIN_VALUE;
		max_key1						= Double.MIN_VALUE;
		min_key1						= Double.MAX_VALUE;
		max_key2						= Double.MIN_VALUE;
		min_key2						= Double.MAX_VALUE;
		
		
		// 1. Accept distance matrix as bin matrix.
		binMatrix = distances;
		
		// 2. Determine minimal and maximal occurence count.
		for (int i = 0; i < binMatrix.length; i++) {
			for (int j = 0; j < binMatrix[i].length; j++) {
				maxOccurenceCount = binMatrix[i][j] > maxOccurenceCount ? binMatrix[i][j] : maxOccurenceCount;
				minOccurenceCount = (binMatrix[i][j] < minOccurenceCount) && (binMatrix[i][j] > 0)  
																		? binMatrix[i][j] : minOccurenceCount;
			}	
		}
		
		// 3. Assign LDA and topic configuration IDs to cells.
		
		//	a. Initialize translation map.
		for (int i = 0; i < binMatrix.length; i++) {
			for (int j = 0; j < binMatrix.length; j++) {
				cellsToTopicConfigurationIDs.put(new Pair<Integer, Integer>(i, j), new HashSet<Pair<Integer,Integer>>());
				cellsToConfigurationIDs.put(new Pair<Integer, Integer>(i, j), new HashSet<Integer>());
			}
		}
		
		// 	b. Map topic configurations to cells..
		for (Pair<Integer, Integer> topicConfig : spatialIDs.keySet()) {
				int spatialID = spatialIDs.get(topicConfig);

				// Add configuration signature to entire row.
				for (int j = 0; j < binMatrix.length; j++) {
					final Pair<Integer, Integer> cellID = new Pair<Integer, Integer>(spatialID, j);

					// Add configuration signature/ID to cell representation.
					cellsToTopicConfigurationIDs.get(cellID).add(topicConfig);
					cellsToConfigurationIDs.get(cellID).add(topicConfig.getKey());
				}
				
				// Add configuration signature to entire column.
				for (int i = 0; i < binMatrix.length; i++) {
					final Pair<Integer, Integer> cellID = new Pair<Integer, Integer>(i, spatialID);

					// Add configuration signature/ID to cell representation.
					cellsToTopicConfigurationIDs.get(cellID).add(topicConfig);
					cellsToConfigurationIDs.get(cellID).add(topicConfig.getKey());
				}
		}
	}

	/*
	 * Getter and setter.
	 */
	
	public double[][] getBinMatrix()
	{
		return binMatrix;
	}

	public double getMinOccurenceCount()
	{
		return minOccurenceCount;
	}

	public double getMaxOccurenceCount()
	{
		return maxOccurenceCount;
	}

	public double getMaxKey1()
	{
		return max_key1;
	}

	public double getMinKey1()
	{
		return min_key1;
	}

	public double getMaxKey2()
	{
		return max_key2;
	}

	public double getMinKey2()
	{
		return min_key2;
	}

	public ArrayList<LDAConfiguration> getChosenLDAConfigurations()
	{
		return chosenLDAConfigurations;
	}

	public void setChosenLDAConfigurations(ArrayList<LDAConfiguration> chosenLDAConfigurations)
	{
		this.chosenLDAConfigurations = chosenLDAConfigurations;
	}

	public Map<Pair<Integer, Integer>, Set<Integer>> getCellsToConfigurationIDs()
	{
		return cellsToConfigurationIDs;
	}

	public Map<Pair<Integer, Integer>, Set<Pair<Integer, Integer>>> getCellsToTopicConfigurationIDs()
	{
		return cellsToTopicConfigurationIDs;
	}
}
