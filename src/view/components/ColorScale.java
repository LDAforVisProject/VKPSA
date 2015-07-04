package view.components;

import javafx.geometry.Orientation;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ColorScale extends ImageView
{
	private final static double YELLOW_HUE	= Color.YELLOW.getHue();
    private final static double BLUE_HUE	= Color.BLUE.getHue();
    private final static double RED_HUE		= Color.RED.getHue();
    
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
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static Color getColorForValue(double value, double min, double max) 
    {
        if (value < min || value > max) {
            return Color.BLACK ;
        }
        
        double hue = YELLOW_HUE + (RED_HUE - YELLOW_HUE) * (value - min) / (max - min);
        
        return Color.hsb(hue, 1.0, 1.0);
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
        if (value < min || value > max) {
            return Color.BLACK ;
        }
        
        double hue = minColor.getHue() + (maxColor.getHue() - minColor.getHue()) * (value - min) / (max - min);
        
        return Color.hsb(hue, 1.0, 1.0);
    }
}
