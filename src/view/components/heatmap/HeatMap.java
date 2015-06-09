package view.components.heatmap;

import java.util.ArrayList;

import view.components.ColorScale;
import model.LDAConfiguration;
import model.workspace.Workspace;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * Implements a Canvas-based heatmap.
 * Adapted from http://stackoverflow.com/questions/25214538/draw-a-smooth-color-scale-and-assign-specific-values-to-it.
 */
public class HeatMap 
{
	/*
	 * UI elements.
	 */
	
	Canvas canvas;
	Label label_key1;
	Label label_key2;
	
	/**
	 * Store reference to selected LDAConfigurations.
	 * These are examined, binned and then plotted. 
	 */
	private ArrayList<LDAConfiguration> selectedLDAConfigurations;
	private String key1;
	private String key2;
	
	/*
	 * Binned data.
	 */
	private BinnedOccurenceEntity binnedData;
	
	public HeatMap(Canvas canvas, Label label_key1, Label label_key2)
    {
		this.canvas		= canvas;
		this.label_key1 = label_key1;
		this.label_key2	= label_key2;
    }
    
    public void update(final ArrayList<LDAConfiguration> selectedLDAConfigurations, final String key1, final String key2) 
    {
    	this.selectedLDAConfigurations	= selectedLDAConfigurations;
    	this.key1						= key1;
    	this.key2						= key2;
    	
    	// Set label text.
    	label_key1.setText(key1);
    	label_key2.setText(key2);
    	
    	// Bin LDA configuration parameter frequency data and draw it.
    	binnedData = examineLDAConfigurations();
    	draw(binnedData);
    }
    
    public void update()
    {
    	draw(binnedData);
    }
    
    /**
     * Bins LDA configuration parameter data.
     * @param binMatrix Matrix in which data is binned. Delivered matrix is modified in this function.
     * @return Minimal and maximal occurence count in bin matrix.
     */
    private BinnedOccurenceEntity examineLDAConfigurations()
    {
    	// 1. Get maximum and minimum for defined keys (alias parameter values).
    	
    	double max_key1	= Double.MIN_VALUE;
		double min_key1	= Double.MAX_VALUE;
		double max_key2	= Double.MIN_VALUE;
		double min_key2	= Double.MAX_VALUE;
		
		for (LDAConfiguration ldaConfig : selectedLDAConfigurations) {
			max_key1 = max_key1 >= ldaConfig.getParameter(key1) ? max_key1 : ldaConfig.getParameter(key1);
			max_key2 = max_key2 >= ldaConfig.getParameter(key2) ? max_key2 : ldaConfig.getParameter(key2);
			
			min_key1 = min_key1 <= ldaConfig.getParameter(key1) ? min_key1 : ldaConfig.getParameter(key1);
			min_key2 = min_key2 <= ldaConfig.getParameter(key2) ? min_key2 : ldaConfig.getParameter(key2);
		}
		
		System.out.println("Key 1: " + min_key1 + " to " + max_key1);
		System.out.println("Key 2: " + min_key2 + " to " + max_key2);
		
		// 2. Bin data based on found minima and maxima.
		
		final int numberOfBins	= (int) Math.sqrt(selectedLDAConfigurations.size());
		int[][] binMatrix		= new int[numberOfBins][numberOfBins];
		double binInterval_key1	= (max_key1 - min_key1) / numberOfBins;
		double binInterval_key2	= (max_key2 - min_key2) / numberOfBins;
		
		for (LDAConfiguration ldaConfig : selectedLDAConfigurations) {
			// Calculate bin index.
			int index_key1 = (int) ( (ldaConfig.getParameter(key1) - min_key1) / binInterval_key1);
			int index_key2 = (int) ( (ldaConfig.getParameter(key2) - min_key2) / binInterval_key2);
			
			// Check if value is maximum. If so, it should be binned in the last bin nonetheless.
			// @todo If necessary (is it?): Introduce same check in binning in GenerationController and AnalysisController.
			index_key1 = index_key1 < numberOfBins ? index_key1 : numberOfBins - 1;
			index_key2 = index_key2 < numberOfBins ? index_key2 : numberOfBins - 1;
			
			// Increment content of corresponding bin.
			binMatrix[index_key1][index_key2]++;
		}
		
		// 3. Determine minimal and maximal occurence count and return it.
		int maxOccurenceCount = Integer.MIN_VALUE;
		int minOccurenceCount = Integer.MAX_VALUE;
		
		for (int i = 0; i < binMatrix.length; i++) {
			for (int j = 0; j < binMatrix[i].length; j++) {
				maxOccurenceCount = binMatrix[i][j] > maxOccurenceCount ? binMatrix[i][j] : maxOccurenceCount;
				minOccurenceCount = binMatrix[i][j] < minOccurenceCount ? binMatrix[i][j] : minOccurenceCount;
			}	
		}
 		
		return new BinnedOccurenceEntity(binMatrix, minOccurenceCount, maxOccurenceCount);
    }
    
    private void draw(BinnedOccurenceEntity binnedData)
    {
    	GraphicsContext gc		= canvas.getGraphicsContext2D();
    	int binMatrix[][]		= binnedData.getBinMatrix();
    	int minOccurenceCount	= binnedData.getMinOccurenceCount();
    	int maxOccurenceCount	= binnedData.getMaxOccurenceCount();
    	
    	// Clear canvas.
    	gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    	
		// Calculate cell width and height (a quadratic matrix is assumed).
		final double cellWidth	= canvas.getWidth() / binMatrix.length; 
		final double cellHeight	= canvas.getHeight() / binMatrix.length;
		
		// Draw each cell in its corresponding place.
		for (int i = 0; i < binMatrix.length; i++) {
			for (int j = 0; j < binMatrix[i].length; j++) {
				// Set color for this cell.
				Color cellColor = ColorScale.getColorForValue(binMatrix[i][j], minOccurenceCount, maxOccurenceCount); 
				gc.setStroke(cellColor);
				gc.setFill(cellColor);
				
				// Draw cell.
				gc.fillRect(cellWidth * i, cellHeight * j, cellWidth, cellHeight);
			}	
		}
		
//        gc.setFill(Color.GREEN);
//        gc.setStroke(Color.BLUE);
//        gc.setLineWidth(5);
//        gc.strokeLine(40, 10, 10, 40);
//        gc.fillOval(10, 60, 30, 30);
//        gc.strokeOval(60, 60, 30, 30);
//        gc.fillRoundRect(110, 60, 30, 30, 10, 10);
//        gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
//        gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
//        gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
//        gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
//        gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
//        gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
//        gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
//        gc.fillPolygon(new double[]{10, 40, 10, 40},
//                       new double[]{210, 210, 240, 240}, 4);
//        gc.strokePolygon(new double[]{60, 90, 60, 90},
//                         new double[]{210, 210, 240, 240}, 4);
//        gc.strokePolyline(new double[]{110, 140, 110, 140},
//                          new double[]{210, 210, 240, 240}, 4);
    }
}