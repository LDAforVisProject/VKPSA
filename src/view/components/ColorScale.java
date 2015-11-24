package view.components;

import javafx.geometry.Orientation;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ColorScale extends ImageView
{
	private final static double DEFAULT_COLOR_HUE	= Color.DARKGREEN.getHue(); 
    
    public ColorScale(WritableImage image)
	{
		super(image);
	}

	/**
     * Create ImageView of a colorscale.
     * Used in HeatMap and the filter pane of the analysis view.
     * @param min
     * @param max
     * @param width
     * @param height
     * @param orientation
     * @return
     */
    public static ImageView createColorScaleImageView(double min, double max, int width, int height, Orientation orientation) 
    {
    	// Image colorScale = createColorScaleImage(600, 120, Orientation.HORIZONTAL);
        WritableImage image		= new WritableImage(width, height);
        PixelWriter pixelWriter	= image.getPixelWriter();
        
        if (orientation == Orientation.HORIZONTAL) {
            for (int x = 0; x < width; x++) {
                double value 	= min + (max - min) * x / width;
                Color color		= getColorForValue(value, min, max);
                
                for (int y = 0; y < height; y++) {
                    pixelWriter.setColor(x, y, color);
                }
            }
        } 
        
        else {
            for (int y = 0; y < height; y++) {
                double value 	= max - (max - min) * y / height;
                Color color		= getColorForValue(value, min, max);
                for (int x = 0; x < width; x++) {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        
        return new ColorScale(image);
    }
    
    /**
     * Calculate value on a blue to red hue scale, dependent on the given minimum and maximum values.
     * @deprecated
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static Color getColorForValuex(double value, double min, double max) 
    {
        if (value != 0 && (value < min || value > max) ) {
            return Color.BLACK ;
        }
        
        else if (value == 0) {
        	return Color.TRANSPARENT;
        }
        
        else {
        	return Color.hsb(Color.LIGHTBLUE.getHue() + (Color.DARKBLUE.getHue() - Color.LIGHTBLUE.getHue()) * (value - min) / (max - min), 1.0, 1.0);
        }
    }
    
    /**
     * Returns default color with saturation chosen depending on the relation of the specified argument to the
     * chosen maximal value.
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static Color getColorForValue(double value, double min, double max) 
    {
        if (value != 0 && (value < min || value > max) ) {
            return Color.BLACK ;
        }
        
        else if (value == 0) 
        	return Color.TRANSPARENT;
        
        else {
        	final double hue = Color.LIGHTGREEN.getHue() + (Color.DARKOLIVEGREEN.getHue() - Color.LIGHTGREEN.getHue()) * (value - min) / (max - min);
        	return Color.hsb(hue, (value - min) / (max - min), 1.0);
        }
    }
    
    /**
     * Calculate value on arbitrary color scale, dependent on the given minimum and maximum values.
     * @param value
     * @param min
     * @param max
     * @param minColor
     * @param maxColor
     * @return
     */
    public static Color getColorForValue(double value, double min, double max, Color minColor, Color maxColor) 
    {
    	if (value != 0 && value != -1 && (value < min || value > max) ) {
            return Color.BLACK ;
        }
        
        else if (value == 0 || value == -1) 
        	return Color.TRANSPARENT;
        
        else {
	        final double hue = minColor.getHue() + (maxColor.getHue() - minColor.getHue()) * (value - min) / (max - min);
	        return Color.hsb(hue, (value - min) / (max - min), 1.0);
        }
    }
    
    public static Color getDefaultColor()
    {
    	return Color.hsb(ColorScale.DEFAULT_COLOR_HUE, 1.0, 1.0);
    }
}
