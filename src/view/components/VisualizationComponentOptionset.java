package view.components;

public abstract class VisualizationComponentOptionset
{
	/**
	 * Indicates whether selection is enabled or not.
	 */
	protected boolean isSelectionEnabled;
	
	/**
	 * Indicates whether axes should be displayed.
	 */
	protected boolean showAxes;
	
	/**
	 * Indicates whether heatmap should use relative view mode.
	 */
	private boolean relativeMode;

	
	// -------------------------------
	//			Methods
	// -------------------------------
	
	public VisualizationComponentOptionset(boolean isSelectionEnabled, boolean showAxes, boolean relativeMode)
	{
		this.isSelectionEnabled = isSelectionEnabled;
		this.showAxes			= showAxes;
		this.relativeMode		= relativeMode;
	}
	
	/*
	 * Getter and setter.
	 */
	
	public boolean isSelectionEnabled()
	{
		return isSelectionEnabled;
	}

	public void setSelectionEnabled(boolean isSelectionEnabled)
	{
		this.isSelectionEnabled = isSelectionEnabled;
	}

	public boolean isShowAxes()
	{
		return showAxes;
	}

	public void setShowAxes(boolean showAxes)
	{
		this.showAxes = showAxes;
	}
	
	public boolean getRelativeMode()
	{
		return relativeMode;
	}

}
