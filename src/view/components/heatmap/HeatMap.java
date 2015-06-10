package view.components.heatmap;

import java.util.ArrayList;
import java.util.List;

import view.components.ColorScale;
import model.LDAConfiguration;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.paint.Color;

/**
 * Implements a Canvas-based heatmap.
 * Adapted from http://stackoverflow.com/questions/25214538/draw-a-smooth-color-scale-and-assign-specific-values-to-it.
 */
public class HeatMap 
{
	/*
	 * UI elements.
	 */
	
	private Canvas canvas;
	private NumberAxis xAxis;
	private NumberAxis yAxis;
	
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
	
	public HeatMap(Canvas canvas, NumberAxis xAxis, NumberAxis yAxis)
    {
		this.canvas		= canvas;
		this.xAxis		= xAxis;
		this.yAxis		= yAxis;
		
		initAxes();
    }
	
	private void initAxes()
	{
		xAxis.setAutoRanging(false);
		yAxis.setAutoRanging(false);
		
		xAxis.setForceZeroInRange(false);
		yAxis.setForceZeroInRange(false);
	}

	public void update(final ArrayList<LDAConfiguration> selectedLDAConfigurations, final String key1, final String key2) 
    {
    	this.selectedLDAConfigurations	= selectedLDAConfigurations;
    	this.key1						= key1;
    	this.key2						= key2;
    	
    	// Set label text.
    	xAxis.setLabel(key1);
    	yAxis.setLabel(key2);
    	
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
 		
		return new BinnedOccurenceEntity(binMatrix, minOccurenceCount, maxOccurenceCount, min_key1, max_key1, min_key2, max_key2);
    }
    
    private void draw(BinnedOccurenceEntity binnedData)
    {
    	GraphicsContext gc		= canvas.getGraphicsContext2D();
    	int binMatrix[][]		= binnedData.getBinMatrix();
    	int minOccurenceCount	= binnedData.getMinOccurenceCount();
    	int maxOccurenceCount	= binnedData.getMaxOccurenceCount();
    	
    	// Set axis label values.
    	xAxis.setLowerBound(binnedData.getMin_key1());
    	xAxis.setUpperBound(binnedData.getMax_key1());
    	yAxis.setLowerBound(binnedData.getMin_key2());
    	yAxis.setUpperBound(binnedData.getMax_key2());
    	// Adjust tick width.
    	final int numberOfTicks = binMatrix.length;
    	xAxis.setTickUnit( (binnedData.getMax_key1() - binnedData.getMin_key1()) / numberOfTicks);
    	yAxis.setTickUnit( (binnedData.getMax_key2() - binnedData.getMin_key2()) / numberOfTicks);
    	xAxis.setMinorTickCount(4);
    	yAxis.setMinorTickCount(4);
    	
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
    }
}