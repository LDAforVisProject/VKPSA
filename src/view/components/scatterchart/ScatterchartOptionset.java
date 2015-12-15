package view.components.scatterchart;

import javafx.scene.paint.Color;
import view.components.VisualizationComponentOptionset;

public class ScatterchartOptionset extends VisualizationComponentOptionset
{
	/**
	 * Determines whether density heatmap is shown.
	 */
	private boolean showDensityHeatmap;
	
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
	 * Describes which categories (active, inactive, discarded) are to be used for 
	 */
	private int categoriesValue;
	
	/**
	 * Lower end of density heatmap's color spectrum.
	 */
	private Color dhmMinColor;
	/**
	 * Upper end of density heatmap's color spectrum.
	 */	
	private Color dhmMaxColor;
	
	public ScatterchartOptionset(	boolean isSelectionEnabled, boolean showAxes, boolean relativeMode,
									boolean showDensityHeatmap, boolean isGranularityDynamic, int granularity,
									int categoriesValue,
									Color dhmMinColor, Color dhmMaxColor)
	{
		super(isSelectionEnabled, showAxes, relativeMode);
	
		this.showDensityHeatmap 	= showDensityHeatmap;
		this.isGranularityDynamic	= isGranularityDynamic;
		this.granularity			= granularity;
		this.categoriesValue		= categoriesValue;
		this.dhmMinColor			= dhmMinColor;
		this.dhmMaxColor			= dhmMaxColor;
	}

	public boolean showDensityHeatmap()
	{
		return showDensityHeatmap;
	}

	public boolean isGranularityDynamic()
	{
		return isGranularityDynamic;
	}

	public int getGranularity()
	{
		return granularity;
	}

	public int getCategoriesValue()
	{
		return categoriesValue;
	}

	public Color getDHMMinColor()
	{
		return dhmMinColor;
	}

	public Color getDHMMaxColor()
	{
		return dhmMaxColor;
	}

}
