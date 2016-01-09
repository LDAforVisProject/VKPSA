package view.components.scentedFilter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import view.components.VisualizationComponentDataset;
import model.LDAConfiguration;

public class ScentedFilterDataset extends VisualizationComponentDataset
{
	/**
	 * Indicates whether dataset refers to primitive (LDA-based) or derived (calculated in postprocessing) parameter.
	 */
	private boolean isDerived;
	
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
	
	/**
	 * Optional. Holds derived data (necessary if filter accesses non-primitive/derived data). 
	 */
	private double[] derivedData;
	
	
	// -------------------------------
	//			Methods
	// -------------------------------
	
	/**
	 * Generates dataset for filter accessing an primitive parameter.
	 * @param data
	 * @param inactiveIndices
	 * @param activeIndices
	 */
	public ScentedFilterDataset(ArrayList<LDAConfiguration> data, Set<Integer> inactiveIndices, Set<Integer> activeIndices)
	{
		super(data);
	
		this.isDerived				= false;
		this.inactiveIndices 		= inactiveIndices;
		this.activeIndices 			= activeIndices;
		this.barToDataAssociations	= new LinkedHashMap<String, ArrayList<Integer>>();
	}
	
	/**
	 * Generates dataset for filter accessing an primitive parameter.
	 * @param data
	 * @param inactiveIndices
	 * @param activeIndices
	 * @param derivedData List of derived data elements (one for each LDA configuration). Has to match data in length.
	 */
	public ScentedFilterDataset(ArrayList<LDAConfiguration> data, Set<Integer> inactiveIndices, Set<Integer> activeIndices, double[] derivedData)
	{
		super(data);
	
		this.isDerived				= true;
		this.inactiveIndices 		= inactiveIndices;
		this.activeIndices 			= activeIndices;
		this.barToDataAssociations	= new LinkedHashMap<String, ArrayList<Integer>>();
		this.derivedData			= derivedData;
		
		// Check for equality in length of provided LDA configurations and provided derived data.
		if (data.size() != derivedData.length) {
			System.out.println("### ERROR ### List of LDA configurations and list of derived data elements have to match in length.");
		}
	}
	
	/**
	 * Bins data. Accesses data directly through parameters in LDA configuration objects.
	 * @param ldaConfigurations
	 * @param inactiveIndices
	 * @param activeIndices
	 * @return List of three arrays: (1) Inactive bin list, (2) active bin list, (3) discarded bin list.
	 */
	public ArrayList<double[]> binData( String param, int numberOfBins, final double min, final double max)
	{
		ArrayList<double[]> binnedData = new ArrayList<double[]>();
		
		// Init barToDataAssociations collection.
		for (int i = 0; i < numberOfBins; i++) {
			barToDataAssociations.put("active_" + i, new ArrayList<Integer>());
			barToDataAssociations.put("inactive_" + i, new ArrayList<Integer>());
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
		final double binInterval			= (max - min) / numberOfBins;
		
		// ...iterate over all LDA configurations.
		for (int i = 0; i < allLDAConfigurations.size(); i++) {
			// Check if dataset is filtered (as opposed to discarded).
			boolean isInactiveDataset 	= inactiveIndices.contains(i);
			boolean isActiveDataset		= activeIndices.contains(i);

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
			else
				parameterBinList_discarded[index_key]++;
		}
		
		// Apply log transformation.
//		for (int i = 0; i < parameterBinList_active.length; i++)
//			parameterBinList_active[i] 		= Math.log(parameterBinList_active[i]);
//		for (int i = 0; i < parameterBinList_inactive.length; i++)
//			parameterBinList_inactive[i] 	= Math.log(parameterBinList_inactive[i]);
//		for (int i = 0; i < parameterBinList_discarded.length; i++)
//			parameterBinList_discarded[i] 	= Math.log(parameterBinList_discarded[i]);
		
		binnedData.add(parameterBinList_inactive);
		binnedData.add(parameterBinList_active);
		binnedData.add(parameterBinList_discarded);
		
		return binnedData;
	}

	public ArrayList<LDAConfiguration> getLDAConfigurations()
	{
		return allLDAConfigurations;
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

	public boolean isDerived()
	{
		return isDerived;
	}

	public double[] getDerivedData()
	{
		return derivedData;
	}
}
