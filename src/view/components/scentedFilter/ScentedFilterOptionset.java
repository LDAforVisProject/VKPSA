package view.components.scentedFilter;

import view.components.VisualizationComponentOptionset;

public class ScentedFilterOptionset extends VisualizationComponentOptionset
{
	/**
	 * Number of bins to use.
	 */
	private int numberOfBins;
	
	private boolean useRangeSlider;
	private String paramID;
	
	/**
	 * Step size of numeric stepper.
	 */
	private double stepSize;
	
	/**
	 * Determines if whole or real numbers should be used for x-axis.
	 */
	private boolean useWholeNumbers;
	
	/**
	 * Minimal value available.
	 */
	private double min;
	/**
	 * Minimal value available.
	 */
	private double max;
	
//	next: 		extend scentendfilterdataset for keyword filter; adapt optionset (e.g. usewholenumbers);
//	then:		use dedicated .fxml for scentedkeywordfilter, including (possibly) diff. arrangement for name label and (definitely) button for removing a filter.
//	after that:	create scentedkeywordfilter using available data (bin by percentage or absolute rank?); use scrollpane to allow for greater number of filters.
//	finally:	extend filtering methods to include data from keyword filter (necessary? filtering should use configIDs anyway).
		
	public ScentedFilterOptionset(	String paramID, boolean useRangeSlider, double min, double max, int numberOfBins, double stepsize, 
									boolean isSelectionEnabled, boolean showAxes, boolean relativeMode, boolean useWholeNumbers)
	{
		super(isSelectionEnabled, showAxes, relativeMode);
		
		this.paramID 			= paramID;
		this.useRangeSlider 	= useRangeSlider;
		this.min				= min;
		this.max				= max;
		this.numberOfBins		= numberOfBins;
		this.stepSize			= stepsize;
		this.useWholeNumbers	= useWholeNumbers;
	}

	public boolean useRangeSlider()
	{
		return useRangeSlider;
	}

	public String getParamID()
	{
		return paramID;
	}

	public double getMin()
	{
		return min;
	}

	public double getMax()
	{
		return max;
	}

	public int getNumberOfBins()
	{
		return numberOfBins;
	}

	public double getStepSize() {
		return stepSize;
	}
}
