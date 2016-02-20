package view.components.parallelTagCloud;

import view.components.VisualizationComponentOptionset;

public class ParallelTagCloudOptionset extends VisualizationComponentOptionset
{
	/**
	 * Number of keywords to display.
	 */
	private int numberOfKeywordsToDisplay;
	
	public ParallelTagCloudOptionset(	boolean isSelectionEnabled, boolean showAxes, boolean relativeMode,
										int numberOfKeywordsToDisplay)
	{
		super(isSelectionEnabled, showAxes, relativeMode);
		
		this.numberOfKeywordsToDisplay = numberOfKeywordsToDisplay;
	}

	public int getNumberOfKeywordsToDisplay()
	{
		return numberOfKeywordsToDisplay;
	}

}
