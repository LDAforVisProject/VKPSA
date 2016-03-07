package view.components.controls.colorLegend;

import view.components.heatmap.HeatmapDataset;
import model.LDAConfiguration;
import javafx.scene.paint.Color;

public class ColorLegendDataset
{
	private double min;
	private double max;
	
	private Color minColor;
	private Color maxColor;
	
	/**
	 * Matrix of values to examine.
	 */
	private double[][] valueMatrix;
	
	/**
	 * Values to display as histogram in array shape.
	 */
	private double[] binList;
	
	private static final int NUMBER_OF_BINS = 50;
	
	
	/**
	 * 
	 * @param min
	 * @param max
	 * @param minColor
	 * @param maxColor
	 */
	public ColorLegendDataset(double min, double max, Color minColor, Color maxColor, double[][] valueMatrix)
	{
		this.min 			= min;
		this.max			= max;
		this.minColor		= minColor;
		this.maxColor		= maxColor;
		this.valueMatrix	= valueMatrix;
		this.binList		= new double[ColorLegendDataset.NUMBER_OF_BINS];
		
		// Re-bin data to density
		binData(this.valueMatrix);
	}
	
	/**
	 * Transforms value of matrix into collection of bins used for the histogram. 
	 * @param valueMatrix
	 */
	private void binData(double[][] valueMatrix)
	{
		// Calculate bin interval.
		final double binInterval			= (max - min) / ColorLegendDataset.NUMBER_OF_BINS;
		
		// Calculate bin content.
		for (int i = 0; i < valueMatrix.length; i++) {
			for (int j = 0; j < valueMatrix[i].length; j++) {
				// Calculate index of bin in which to store the current value.
				int index_key		= (int) ( (valueMatrix[i][j] - this.min) / binInterval);

				// Check if element is highest allowed entry.
				index_key			= index_key < ColorLegendDataset.NUMBER_OF_BINS ? index_key : ColorLegendDataset.NUMBER_OF_BINS - 1;
				// Check if element is lowest allowed entry.
				index_key			= index_key >= 0 ? index_key : 0;
				
				// Increment counter in corresponding bin.
				binList[index_key]++;
			}
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		ColorLegendDataset other = (ColorLegendDataset) obj;
		
		if (Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min))
			return false;
		
		if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max))
			return false;
		
		if (!minColor.equals(other.minColor))
			return false;

		if (!maxColor.equals(other.maxColor))
			return false;
		
		return true;
	}

	public double getMin()
	{
		return min;
	}

	public double getMax()
	{
		return max;
	}

	public Color getMinColor()
	{
		return minColor;
	}

	public Color getMaxColor()
	{
		return maxColor;
	}

	public double[] getBinList()
	{
		return binList;
	}
}
