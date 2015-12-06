package view.components.scentedFilter;

import java.util.ArrayList;
import java.util.Set;

import model.LDAConfiguration;

public class ScentedFilterDataset
{
	private ArrayList<LDAConfiguration> ldaConfigurations;
	private Set<Integer> inactiveIndices;
	private Set<Integer> activeIndices;
	
	public ScentedFilterDataset(ArrayList<LDAConfiguration> data, Set<Integer> inactiveIndices, Set<Integer> activeIndices)
	{
		this.ldaConfigurations 				= data;
		this.inactiveIndices 	= inactiveIndices;
		this.activeIndices 		= activeIndices;
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
				// Filtered dataset:
				if (!isActiveDataset)
					parameterBinList_inactive[index_key]++;
				// Selected dataset:
				else 
					parameterBinList_active[index_key]++;
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
}
