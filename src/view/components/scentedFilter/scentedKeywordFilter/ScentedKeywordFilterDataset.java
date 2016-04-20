package view.components.scentedFilter.scentedKeywordFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.util.Pair;
import model.LDAConfiguration;
import model.misc.KeywordRankObject;
import view.components.scentedFilter.ScentedFilterDataset;

public class ScentedKeywordFilterDataset extends ScentedFilterDataset
{
	/**
	 * Examined keyword.
	 */
	private String keyword;
	
	/**
	 * Number of keywords (and implicitly ranks).
	 */
	private int numberOfKeywords;
	
	/**
	 * Stores which bars contain which LDA and topic configurations.
	 */
	protected Map<String, ArrayList<Pair<Integer, Integer>>> barToTopicDataAssociations;
	
	/**
	 * Association between state (active, inactive, discarded) of a LDA configuration
	 * and its ID.
	 * Used for more performant and convenient data binning.
	 */
	private Map<Integer, String> ldaConfigIDStates;
	
	/**
	 * Collection of keyword rank objects for the specified keyword.
	 */
	private ArrayList<KeywordRankObject> keywordRankObjects;
	
	/**
	 * Minimum in set of values.
	 */
	private double min;
	/**
	 * Maximum in set of values.
	 */
	private double max;
	
	/**
	 * Default constructor. Not used for ScentedKeywordFilterDataset.
	 * @param data
	 * @param inactiveIndices
	 * @param activeIndices
	 */
	public ScentedKeywordFilterDataset(	ArrayList<LDAConfiguration> data, Set<Integer> inactiveIndices, Set<Integer> activeIndices,
										ArrayList<KeywordRankObject> keywordRankObjects, final String keyword, final int numberOfKeywords)
	{
		super(data, inactiveIndices, activeIndices);
		
		this.isDerived					= true;
		this.barToTopicDataAssociations = new HashMap<String, ArrayList<Pair<Integer,Integer>>>();
		this.keywordRankObjects			= keywordRankObjects;
		this.keyword					= keyword;
		this.numberOfKeywords			= numberOfKeywords;
		// Prepare map for association between state and ID of a LDA configuration.
		this.ldaConfigIDStates			= new HashMap<Integer, String>();
	}

	/**
	 * Determines state of LDA configurations.
	 */
	private void determineLDAConfigurationStates()
	{
		// Clear collection.
		ldaConfigIDStates.clear();
		
		// Associate configuration ID with state.
		// Remark: Would have been cleaner to use LDAConfiguration instances "live", i.e. let them represent their state
		// and share these objects across all visualizations. Next time.
		for (int i = 0; i < allLDAConfigurations.size(); i++) {
			// Get current LDA config. ID.
			int ldaConfigID = allLDAConfigurations.get(i).getConfigurationID();
			
			// Associate LDA configuration with state.
			if (activeIndices.contains(i))
				ldaConfigIDStates.put(ldaConfigID, "active");
			else if (inactiveIndices.contains(i))
				ldaConfigIDStates.put(ldaConfigID, "inactive");
			else
				ldaConfigIDStates.put(ldaConfigID, "discarded");
		}
	}
	
	/**
	 * Bins data. Accesses data through previously provided collection of KeywordRankObjects. 
	 * @param param
	 * @param numberOfBins
	 * @param min
	 * @param max
	 * @return List of three arrays: (1) Inactive bin list, (2) active bin list, (3) discarded bin list.
	 */
	@Override
	public ArrayList<double[]> binData(String param, int numberOfBins, final double min, final double max)
	{
		// Determine state of LDA configurations.
		determineLDAConfigurationStates();
		
		// Update minimum and maximum in set of values.
		this.min = min;
		this.max = max;
		
		// Prepare collection for binned data.
		ArrayList<double[]> binnedData = new ArrayList<double[]>();
		
		// Init barToDataAssociations collection.
		for (int i = 0; i < numberOfBins; i++) {
			// Bar to LDA config. associations.
			barToDataAssociations.put("active_" + i, new ArrayList<Integer>());
			barToDataAssociations.put("inactive_" + i, new ArrayList<Integer>());
			barToDataAssociations.put("discarded_" + i, new ArrayList<Integer>());
			// Bar to topic/LDA config. associations.
			barToTopicDataAssociations.put("active_" + i, new ArrayList<Pair<Integer, Integer>>());
			barToTopicDataAssociations.put("inactive_" + i, new ArrayList<Pair<Integer, Integer>>());
			barToTopicDataAssociations.put("discarded_" + i, new ArrayList<Pair<Integer, Integer>>());			
		}
		
		// Map storing one bin list for each parameter, counting only filtered datasets.
		double[] parameterBinList_inactive		= new double[numberOfBins];
		// Map storing one bin list for each parameter, counting only selected datasets.
		double[] parameterBinList_active		= new double[numberOfBins];
		// Map storing one bin list for each parameter, counting only discarded datasets.
		double[] parameterBinList_discarded		= new double[numberOfBins];
		
		/*
		 *  Bin data.
		 */
		
		// Calculate bin interval.
		final double binInterval				= (max - min) / numberOfBins;
		
		// ...iterate over all KeywordRankObjects.
		for (int i = 0; i < keywordRankObjects.size(); i++) {
			// Get LDA config. ID of current KeywordRankObject.
			int ldaConfigIDOfKRO	= keywordRankObjects.get(i).getLDAConfigID();
			int topicIDOfKRO		= keywordRankObjects.get(i).getTopicID();
			
			// Calculate index of bin in which to store the current value.
			int index_key		= (int)( (keywordRankObjects.get(i).getRank() - min) / binInterval);
			// Check if element is highest allowed entry.
			index_key			= index_key < numberOfBins ? index_key : numberOfBins - 1;
			// Check if element is lowest allowed entry.
			index_key			= index_key >= 0 ? index_key : 0;
			
			/*
			 * Check if this dataset fits all boundaries / is inactive, active or discarded - then increment content of corresponding bin.
			 */
			
			// Inactive dataset:
			if (ldaConfigIDStates.get(ldaConfigIDOfKRO).equals("inactive")) {
				parameterBinList_inactive[index_key]++;
			}
			// Active dataset:
			else if (ldaConfigIDStates.get(ldaConfigIDOfKRO).equals("active")) { 
				parameterBinList_active[index_key]++;
			}
			// Discarded dataset:
			else if (ldaConfigIDStates.get(ldaConfigIDOfKRO).equals("discarded")) {
				parameterBinList_discarded[index_key]++;
			}
			// An inactive dataset may be selected later on, so we add it's
			// index to the collection of bar to data associations.
			// An inactive dataset may be de-selected later on, so we add
			//  it's index to the collection of bar to data associations.
			// A discarded dataset may be hovered over later on, so we add
			// it's index to the collection of bar to data associations.
			barToDataAssociations.get(ldaConfigIDStates.get(ldaConfigIDOfKRO) + "_" + index_key).add(ldaConfigIDOfKRO);
			barToTopicDataAssociations.get(ldaConfigIDStates.get(ldaConfigIDOfKRO) + "_" + index_key).add(new Pair<Integer, Integer>(ldaConfigIDOfKRO, topicIDOfKRO));
		}
		
		binnedData.add(parameterBinList_inactive);
		binnedData.add(parameterBinList_active);
		binnedData.add(parameterBinList_discarded);
		
		return binnedData;
	}
	
	public Map<String, ArrayList<Pair<Integer, Integer>>> getBarToTopicDataAssociations()
	{
		return barToTopicDataAssociations;
	}

	@Override
	public void clearBarToDataAssociations()
	{
		super.clearBarToDataAssociations();
		this.barToTopicDataAssociations.clear();
	}

	public double getMin()
	{
		return min;
	}

	public double getMax()
	{
		return max;
	}

	public String getKeyword()
	{
		return keyword;
	}
}
