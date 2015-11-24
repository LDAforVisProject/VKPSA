package view.components.heatmap;

import view.components.VisualizationComponentOptionset;
import javafx.scene.paint.Color;

public class HeatmapOptionset extends VisualizationComponentOptionset
{
	/**
	 * Indicates if granularity is to be adjusted dynamically.
	 */
	private boolean isGranularityDynamic;
	/**
	 * Granularity (number of squares/elements on on axis) of heatmap.
	 * Only used when adjustGranularityDynamically == false.
	 */
	private int granularity;
	
	/**
	 * Color used to represent low values. 
	 */
	private Color minColor;
	/**
	 * Color used to represent high values. 
	 */
	private Color maxColor;
	
	/**
	 * Color cells are highlighted in when they ought to be removed from the current selection.
	 */
	private Color subtractiveSelectionColor;
	/**
	 * Color cells are highlighted in when they ought to be added to the current selection.
	 */
	private Color additiveSelectionColor;
	
	/**
	 * Name of x-axis parameter.
	 */
	private String key1;
	/**
	 * Name of y-axis parameter.
	 */
	private String key2;
	
	
	// -------------------------------
	//			Methods	
	// -------------------------------
	
	public HeatmapOptionset(boolean isGranularityDynamic, int granularity, 
							Color minColor, Color maxColor, Color subtractiveSelectionColor, Color additiveSelectionColor,
							String key1, String key2, boolean isSelectionEnabled, boolean relativeMode, boolean showAxes)
	{
		super(isSelectionEnabled, showAxes, relativeMode);
		
		this.isGranularityDynamic		= isGranularityDynamic;
		this.granularity				= granularity;
		this.minColor					= minColor;
		this.maxColor					= maxColor;
		this.key1						= key1;
		this.key2						= key2;
		this.subtractiveSelectionColor	= subtractiveSelectionColor;
		this.additiveSelectionColor		= additiveSelectionColor;
	}

	/*
	 * Getter and setter.
	 */

	public boolean isGranularityDynamic()
	{
		return isGranularityDynamic;
	}

	public int getGranularity()
	{
		return granularity;
	}

	public Color getMinColor()
	{
		return minColor;
	}

	public Color getMaxColor()
	{
		return maxColor;
	}

	public String getKey1()
	{
		return key1;
	}
	
	public String getKey2()
	{
		return key2;
	}
		
	public void setGranularityDynamic(boolean isGranularityDynamic)
	{
		this.isGranularityDynamic = isGranularityDynamic;
	}

	public void setGranularity(int granularity)
	{
		this.granularity = granularity;
	}

	public boolean getShowAxes()
	{
		return showAxes;
	}

	public Color getSubtractiveSelectionColor()
	{
		return subtractiveSelectionColor;
	}

	public Color getAdditiveSelectionColor()
	{
		return additiveSelectionColor;
	}
}
