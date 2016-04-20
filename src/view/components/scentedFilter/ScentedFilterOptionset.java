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
	 * Major tick unit for slider.
	 */
	private double majorTickCount;
	
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
	
	/**
	 * Creates new option set for ScentedFilter.
	 * @param paramID
	 * @param useRangeSlider
	 * @param min
	 * @param max
	 * @param numberOfBins
	 * @param stepsize
	 * @param majorTickUnit
	 * @param isSelectionEnabled
	 * @param showAxes
	 * @param relativeMode
	 * @param useWholeNumbers
	 */
	public ScentedFilterOptionset(	String paramID, boolean useRangeSlider, 
									double min, double max, 
									int numberOfBins, double stepsize, double majorTickCount, 
									boolean isSelectionEnabled, boolean showAxes, boolean relativeMode, 
									boolean useWholeNumbers)
	{
		super(isSelectionEnabled, showAxes, relativeMode);
		
		this.paramID 			= paramID;
		this.useRangeSlider 	= useRangeSlider;
		this.min				= min;
		this.max				= max;
		this.numberOfBins		= numberOfBins;
		this.majorTickCount		= majorTickCount;
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

	public double getMajorTickCount()
	{
		return majorTickCount;
	}

	public boolean useWholeNumbers()
	{
		return useWholeNumbers;
	}

	public int getNumberOfBins()
	{
		return numberOfBins;
	}

	public double getStepSize() {
		return stepSize;
	}
}
