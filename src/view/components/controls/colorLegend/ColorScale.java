package view.components.controls.colorLegend;

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
     * @param min
     * @param max
     * @param minColor
     * @param maxColor
     * @param width
     * @param height
     * @param orientation
     * @return
     */
    public static ImageView createColorScaleImageView(double min, double max, Color minColor, Color maxColor, int width, int height, Orientation orientation) 
    {
    	// Image colorScale = createColorScaleImage(600, 120, Orientation.HORIZONTAL);
        WritableImage image		= new WritableImage(width, height);
        PixelWriter pixelWriter	= image.getPixelWriter();
        
        if (orientation == Orientation.HORIZONTAL) {
            for (int x = 0; x < width; x++) {
                double value 	= min + (max - min) * x / width;
                Color color		= getColorForValue(value, min, max, minColor, maxColor);
                
                for (int y = 0; y < height; y++) {
                    pixelWriter.setColor(x, y, color);
                }
            }
        } 
        
        else {
            for (int y = 0; y < height; y++) {
                double value 	= max - (max - min) * y / height;
                Color color		= getColorForValue(value, min, max, minColor, maxColor);
                for (int x = 0; x < width; x++) {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        
        return new ColorScale(image);
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
    	if (value == 0 || value == -1) {
    		return Color.TRANSPARENT;
        }
    	
    	else if (value < min || value > max) {
            return Color.BLACK ;
        }
    	
        else {
        	final double hue		= minColor.getHue() + (maxColor.getHue() - minColor.getHue()) * (value - min) / (max - min);
        	//final double saturation	= (value - min) / (max - min);
        	final double saturation	= minColor.getSaturation() + (maxColor.getSaturation() - minColor.getSaturation()) * (value - min) / (max - min);
        	
        	// Workaround: No hue difference calculation when both colors are grey - doesn't work (switches to red - why?).
        	if (minColor != Color.GRAY && maxColor != Color.GREY) {
		        return Color.hsb(hue, saturation + 0.1 < 1 ? saturation + 0.1 : 1, 1.0);
        	}
        	
        	else {
        		return Color.gray(hue, saturation);
        	}
        }
    }
    
    public static Color getDefaultColor()
    {
    	return Color.hsb(ColorScale.DEFAULT_COLOR_HUE, 1.0, 1.0);
    }
}
