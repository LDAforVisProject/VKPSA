package view.components.controls.ColorLegend;

import model.LDAConfiguration;
import javafx.scene.paint.Color;

public class ColorLegendDataset
{
	private double min;
	private double max;
	
	private Color minColor;
	private Color maxColor;
	
	public ColorLegendDataset(double min, double max, Color minColor, Color maxColor)
	{
		this.min 		= min;
		this.max		= max;
		this.minColor	= minColor;
		this.maxColor	= maxColor;
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
}
