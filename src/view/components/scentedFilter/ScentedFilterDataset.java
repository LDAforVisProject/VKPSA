package view.components.scentedFilter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import model.LDAConfiguration;

public class ScentedFilterDataset
{
	/**
	 * Relevant LDA configurations.
	 */
	private ArrayList<LDAConfiguration> ldaConfigurations;
	
	/**
	 * Indices of inactive datasets.
	 */
	private Set<Integer> inactiveIndices;
	/**
	 * Indices of active datasets.
	 */
	private Set<Integer> activeIndices;
	
	/**
	 * Stores which bars contain which LDA configurations.
	 */
	private Map<String, ArrayList<Integer>> barToDataAssociations;
	
	
	// -------------------------------
	//			Methods
	// -------------------------------
	
	public ScentedFilterDataset(ArrayList<LDAConfiguration> data, Set<Integer> inactiveIndices, Set<Integer> activeIndices)
	{
		this.ldaConfigurations 	= data;
		this.inactiveIndices 	= inactiveIndices;
		this.activeIndices 		= activeIndices;
		barToDataAssociations	= new LinkedHashMap<String, ArrayList<Integer>>();
	}
	
	/**
	 * Bins data.
	 * @param ldaConfigurations
	 * @param inactiveIndices
	 * @param activeIndices
	 * @return List of three arrays: (1) Inactive bin list, (2) active bin list, (3) discarded bin list.
	 */
	public ArrayList<int[]> binData( String param, int numberOfBins, final double min, final double max)
	{
		ArrayList<int[]> binnedData = new ArrayList<int[]>();
		
		// Init barToDataAssociations collection.
		for (int i = 0; i < numberOfBins; i++) {
			barToDataAssociations.put("active_" + i, new ArrayList<Integer>());
			barToDataAssociations.put("inactive_" + i, new ArrayList<Integer>());
		}
		
		// Map storing one bin list for each parameter, counting only filtered datasets.
		int[] parameterBinList_inactive		= new int[numberOfBins];
		// Map storing one bin list for each parameter, counting only selected datasets.
		int[] parameterBinList_active		= new int[numberOfBins];
		// Map storing one bin list for each parameter, counting only discarded datasets.
		int[] parameterBinList_discarded	= new int[numberOfBins];
		
		// Bin data.
		double binInterval	= (max - min) / numberOfBins;
		
		// ...iterate over all LDA configurations.
		for (int i = 0; i < ldaConfigurations.size(); i++) {
			// Check if dataset is filtered (as opposed to discarded).
			boolean isInactiveDataset 	= inactiveIndices.contains(i);
			boolean isActiveDataset		= activeIndices.contains(i);

			// Calculate index of bin in which to store the current value.
			int index_key		= (int) ( (ldaConfigurations.get(i).getParameter(param) - min) / binInterval);
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
			else
				parameterBinList_discarded[index_key]++;
		}
		
		binnedData.add(parameterBinList_inactive);
		binnedData.add(parameterBinList_active);
		binnedData.add(parameterBinList_discarded);
		
		return binnedData;
	}

	public ArrayList<LDAConfiguration> getLDAConfigurations()
	{
		return ldaConfigurations;
	}

	public Set<Integer> getInactiveIndices()
	{
		return inactiveIndices;
	}

	public Set<Integer> getActiveIndices()
	{
		return activeIndices;
	}

	public Map<String, ArrayList<Integer>> getBarToDataAssociations()
	{
		return this.barToDataAssociations;
	}

	public void clearBarToDataAssociations()
	{
		this.barToDataAssociations.clear();
	}
}
