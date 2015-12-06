package view.components.scentedFilter;

public class ScentedFilterOptionset
{
	/**
	 * Number of bins to use.
	 */
	private int numberOfBins;
	
	private boolean useRangeSlider;
	private String paramID;
	
	/**
	 * Minimal value available.
	 */
	private double min;
	/**
	 * Minimal value available.
	 */
	private double max;
	
	public ScentedFilterOptionset(String paramID, boolean useRangeSlider, double min, double max, int numberOfBins)
	{
		this.paramID 		= paramID;
		this.useRangeSlider = useRangeSlider;
		this.min			= min;
		this.max			= max;
		this.numberOfBins	= numberOfBins;
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
}
