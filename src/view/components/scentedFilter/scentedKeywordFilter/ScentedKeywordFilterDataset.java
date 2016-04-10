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
	private Collection<KeywordRankObject> keywordRankObjects;
	
	/**
	 * Default constructor. Not used for ScentedKeywordFilterDataset.
	 * @param data
	 * @param inactiveIndices
	 * @param activeIndices
	 */
	public ScentedKeywordFilterDataset(	ArrayList<LDAConfiguration> data, Set<Integer> inactiveIndices, Set<Integer> activeIndices,
										Collection<KeywordRankObject> keywordRankObjects, final String keyword)
	{
		super(data, inactiveIndices, activeIndices);
		
		this.isDerived					= true;
		this.barToTopicDataAssociations = new HashMap<String, ArrayList<Pair<Integer,Integer>>>();
		this.keywordRankObjects			= keywordRankObjects;
		this.keyword					= keyword;
		// Prepare map for association between state and ID of a LDA configuration.
		this.ldaConfigIDStates			= new HashMap<Integer, String>();
		
		// Associate configuration ID with state.
		for (int i = 0; i < allLDAConfigurations.size(); i++) {
			if (activeIndices.contains(i))
				ldaConfigIDStates.put(i, "active");
			else if (inactiveIndices.contains(i))
				ldaConfigIDStates.put(i, "inactive");
			else
				ldaConfigIDStates.put(i, "discarded");
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
		ArrayList<double[]> binnedData = new ArrayList<double[]>();
		
		// Init barToDataAssociations collection.
		for (int i = 0; i < numberOfBins; i++) {
			barToDataAssociations.put("active_" + i, new ArrayList<Integer>());
			barToDataAssociations.put("inactive_" + i, new ArrayList<Integer>());
			barToDataAssociations.put("discarded_" + i, new ArrayList<Integer>());
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
			todo: 	get maximum (number of keywords), adapt binning mechanism.
					goal: produce working keyword filter.
					afterwards:
							implement dynamic generation/scrollpane etc.
			// Check if dataset is filtered (as opposed to discarded).
			boolean isInactiveDataset 	= inactiveIndices.contains(i);
			boolean isActiveDataset		= activeIndices.contains(i);
			boolean isDiscardedDataset	= !isInactiveDataset && !isActiveDataset;

			// Calculate index of bin in which to store the current value.
			int index_key		= -1;
			// If primitive parameter is accessed: Get value directly from LDA configuration instance.
			if (!isDerived)
				index_key		= (int) ( (allLDAConfigurations.get(i).getParameter(param) - min) / binInterval);
			else
				index_key		= (int) ( (derivedData[i] - min) / binInterval);
			
			// Check if element is highest allowed entry.
			index_key			= index_key < numberOfBins ? index_key : numberOfBins - 1;
			// Check if element is lowest allowed entry.
			index_key			= index_key >= 0 ? index_key : 0;
			
			// Check if this dataset fits all boundaries / is inactive, active or discarded - then increment content of corresponding bin.
			if (isInactiveDataset) {
				// Inactive dataset:
				if (!isActiveDataset) {
					parameterBinList_inactive[index_key]++;
					// An inactive dataset may be selected later on, so we add it's
					// index to the collection of bar to data associations:
					barToDataAssociations.get("inactive_" + index_key).add(i);
				}
				// Active dataset:
				else { 
					parameterBinList_active[index_key]++;
					// An inactive dataset may be de-selected later on, so we add
					//  it's index to the collection of bar to data associations:
					barToDataAssociations.get("active_" + index_key).add(i);
				}
			}
			else {
				parameterBinList_discarded[index_key]++;
				// A discarded dataset may be hovered over later on, so we add
				// it's index to the collection of bar to data associations:
				barToDataAssociations.get("discarded_" + index_key).add(i);
			}
		}
		
		// Apply log transformation.
		if (param == "distance") {
			for (int i = 0; i < parameterBinList_active.length; i++) {
				if (parameterBinList_active[i] == 1)
					parameterBinList_active[i] = 1;
				
				else if (parameterBinList_active[i] > 1)
					parameterBinList_active[i] = Math.log10(parameterBinList_active[i]) + 1;
			}
			for (int i = 0; i < parameterBinList_inactive.length; i++) {
				if (parameterBinList_inactive[i] == 1)
					parameterBinList_inactive[i] = 1;
				
				else if (parameterBinList_inactive[i] > 1)
					parameterBinList_inactive[i] = Math.log10(parameterBinList_inactive[i]) + 1;
			}
			for (int i = 0; i < parameterBinList_discarded.length; i++) {
				if (parameterBinList_discarded[i] == 1)
					parameterBinList_discarded[i] = 1;
				
				else if (parameterBinList_discarded[i] > 1)
					parameterBinList_discarded[i] = Math.log10(parameterBinList_discarded[i]) + 1;				
			}
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
}
